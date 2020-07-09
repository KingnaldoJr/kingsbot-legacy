package dev.kingnaldo.kingsbot.music;

import android.util.Patterns;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.music.spotify.SpotifyUtils;
import dev.kingnaldo.kingsbot.music.spotify.objects.SpotifySimplifiedTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MusicPlayerHandler {
    private final static DefaultAudioPlayerManager playerManager =
            new DefaultAudioPlayerManager();
    private final static Map<Long, MusicPlayerHandler> playerInstances =
            new ConcurrentHashMap<>();

    private final Guild guild;
    private final TextChannel textChannel;
    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private volatile RepeatMode repeatMode;
    private volatile int position;
    private volatile List<String> queue;
    private volatile List<Integer> queueIndex;
    private volatile List<User> skipVotes;
    private volatile List<Message> nowPlayingMessage;

    private MusicPlayerHandler(Guild guild, TextChannel channel) {
        this.guild = guild;
        this.textChannel = channel;
        this.player = playerManager.createPlayer();
        this.scheduler = new TrackScheduler(this, this.player);
        this.repeatMode = RepeatMode.NONE;
        this.position = 0;
        this.queue = new ArrayList<>();
        this.skipVotes = new ArrayList<>();
        this.nowPlayingMessage = new ArrayList<>();

        this.player.addListener(this.scheduler);
        guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(this.player));

        playerInstances.put(guild.getIdLong(), this);
    }

    public static void init() {
        AudioSourceManagers.registerRemoteSources(MusicPlayerHandler.playerManager);
        MusicPlayerHandler.playerManager.getConfiguration()
                .setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        MusicPlayerHandler.playerManager.getConfiguration()
                .setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
    }

    public static MusicPlayerHandler getInstance(Guild guild, TextChannel channel) {
        return MusicPlayerHandler.playerInstances.containsKey(guild.getIdLong()) ?
                MusicPlayerHandler.playerInstances.get(guild.getIdLong()) :
                new MusicPlayerHandler(guild, channel);
    }

    public static void removeGuild(Guild guild) {
        MusicPlayerHandler.playerInstances.remove(guild.getIdLong());
    }

    private void removeThisGuild() {
        this.player.destroy();
        MusicPlayerHandler.playerInstances.remove(this.guild.getIdLong());
    }

    public RepeatMode getRepeatMode() { return this.repeatMode; }

    public void setRepeatMode(RepeatMode mode) {
        this.repeatMode = mode;
    }

    public List<String> getQueue() {
        return new ArrayList<>(this.queue);
    }

    public synchronized void addToQueue(String identifier, boolean isSoundCloud) {
        this.addToQueue(this.queue.size(), identifier, isSoundCloud);
    }

    public synchronized void addToQueue(int position, String identifier, boolean isSoundCloud) {
        if(SpotifyUtils.isSpotify(identifier)) {
            try {
                List<SpotifySimplifiedTrack> tracks = SpotifyUtils.getSpotifyObject(identifier);
                textChannel.sendMessage("Added " + tracks.size() + " musics to queue.").queue();
                tracks.forEach(track -> {
                    StringBuilder builder = new StringBuilder();
                    track.artists().forEach(artist -> builder.append(artist.name()).append(" "));
                    builder.append("- ").append(track.name());
                    this.queue.add("ytsearch:" + builder.toString());
                    this.queueIndex.add(this.queueIndex.size(), this.queueIndex.size() - 1);
                });
                if(this.player.getPlayingTrack() == null && !isPaused())
                    this.playNextTrack();
            }catch(IOException e) {
                textChannel.sendMessage("Something went wrong.").queue();
                KingsBot.LOGGER.error(e.getMessage());
            }
            return;
        }

        if(!Patterns.WEB_URL.matcher(identifier).matches()) {
            if(isSoundCloud) identifier = "scsearch: " + identifier;
            else identifier = "ytsearch: " + identifier;
        }

        MusicPlayerHandler.playerManager.loadItem(identifier, new FunctionalResultHandler(
                track -> {
                    this.queue.add(position, track.getInfo().uri);
                    textChannel.sendMessage(new EmbedBuilder()
                            .setTitle("Added to queue")
                            .setDescription("[" + track.getInfo().title
                                    + "](" + track.getInfo().uri + ")")
                            .build()).queue();
                    if(this.player.getPlayingTrack() == null && !isPaused())
                        this.playNextTrack();
                }, playlist -> {
            if (!playlist.isSearchResult()) {
                playlist.getTracks().parallelStream().forEachOrdered(track ->
                        this.queue.add(track.getInfo().uri));
                textChannel.sendMessage(new EmbedBuilder()
                        .setDescription("Added " + playlist.getTracks().size()
                                + " musics to queue.").build()).queue();
            }else{
                AudioTrack track = playlist.getTracks().get(0);
                this.queue.add(position, track.getInfo().uri);
                textChannel.sendMessage(new EmbedBuilder()
                        .setTitle("Added to queue")
                        .setDescription("[" + track.getInfo().title
                                + "](" + track.getInfo().uri + ")")
                        .build()).queue();
            }
            if(this.player.getPlayingTrack() == null && !isPaused())
                this.playNextTrack();
        },
                () -> textChannel.sendMessage("No matches found.").queue(),
                exception -> textChannel.sendMessage("No matches found.").queue()));
    }

    public synchronized boolean removeFromQueue(int position) {
        if(isShuffled()) this.queueIndex.remove(this.queue.indexOf(this.queue.get(position)));
        return this.queue.remove(this.queue.get(position));
    }

    public synchronized void changePositionOnQueue(int oldPosition, int newPosition) {
        if(oldPosition <= newPosition)
            Collections.rotate(this.queue.subList(oldPosition, newPosition + 1), -1);
        else Collections.rotate(this.queue.subList(newPosition, oldPosition + 1), 1);
    }

    public synchronized boolean isPaused() {
        return this.player.isPaused();
    }

    public synchronized boolean togglePause() {
        if(this.isPaused()) {
            this.player.setPaused(false);
            if(this.player.getPlayingTrack() != null)
                this.nowPlayingMessage.add(this.textChannel.sendMessage(new EmbedBuilder()
                        .setTitle("Now Playing")
                        .setDescription("[" + this.player.getPlayingTrack().getInfo().title + "]("
                                + this.player.getPlayingTrack().getInfo().uri + ")")
                        .build()).complete());
        }else{
            this.player.setPaused(true);
            this.nowPlayingMessage.forEach(message -> message.getChannel().purgeMessages(message));
        }
        return this.isPaused();
    }

    public void shuffleQueue() {
        this.queueIndex = new ArrayList<>();
        for(int i = 0; i < this.queue.size(); i++)
            this.queueIndex.add(i);

        Collections.shuffle(this.queueIndex);
    }

    public void unshuffleQueue() {
        this.queueIndex.clear();
    }

    public int getVolume() {
        return this.player.getVolume();
    }

    public void setVolume(int volume) {
        this.player.setVolume(volume);
    }

    public synchronized int getVoteCounts() {
        return this.skipVotes.size();
    }

    public synchronized boolean addVote(User user) {
        if(this.skipVotes.contains(user)) return false;
        else this.skipVotes.add(user);
        return true;
    }

    public synchronized void removeVote(User user) {
        this.skipVotes.remove(user);
    }

    public synchronized boolean isShuffled() {
        return this.queueIndex == null || this.queueIndex.isEmpty();
    }

    public synchronized void forceSkip() {
        this.textChannel.sendMessage("Skipped!").queue();
        this.scheduler.skipTrack();
    }

    public synchronized void stopQueue() {
        this.player.destroy();
        this.removeThisGuild();
    }

    public synchronized void onTrackStart() {
        this.skipVotes.clear();

        final AudioTrack track = this.player.getPlayingTrack();
        this.nowPlayingMessage.add(this.textChannel.sendMessage(new EmbedBuilder()
                .setTitle("Now Playing")
                .setDescription("[" + track.getInfo().title + "]("
                        + this.player.getPlayingTrack().getInfo().uri + ")")
                .build()).complete());
    }

    public synchronized void onTrackEnd() {
        this.nowPlayingMessage.forEach(message -> message.getChannel().purgeMessages(message));
        if(this.repeatMode.equals(RepeatMode.TRACK)) return;

        this.position++;
        if(this.scheduler.queueIsEmpty()) {
            if(this.queue.isEmpty()) {
                this.textChannel.sendMessage("Queue ended!").queue();
                this.removeThisGuild();
            }else this.playNextTrack();
        }
    }

    public synchronized void playNextTrack() {
        if(this.position == this.queue.size()) {
            if(this.repeatMode.equals(RepeatMode.QUEUE)) {
                this.position = 0;
            }
        }
        if(this.position >= this.queue.size()) return;

        FunctionalResultHandler handler = new FunctionalResultHandler(
                this.scheduler::queue, playlist -> {
            if(playlist.isSearchResult())
                this.scheduler.queue(playlist.getTracks().get(0));
            else playlist.getTracks().forEach(this.scheduler::queue); }, () -> {
            textChannel.sendMessage("No matches found.").queue();
            this.playNextTrack(); }, exception -> {
            textChannel.sendMessage("No matches found.").queue();
            this.playNextTrack(); });

        if(isShuffled()) {
            MusicPlayerHandler.playerManager.loadItem(this.queue.get(this.position), handler);
        }else{
            MusicPlayerHandler.playerManager.loadItem(
                    this.queue.get(this.queueIndex.get(this.position)), handler);
        }
    }
}
