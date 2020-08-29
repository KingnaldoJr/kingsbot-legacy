package dev.kingnaldo.kingsbot.music;

import android.util.Patterns;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.music.spotify.SpotifyUtils;
import dev.kingnaldo.kingsbot.music.youtube.YoutubeUtils;
import lavalink.client.LavalinkUtil;
import lavalink.client.io.Link;
import lavalink.client.io.jda.JdaLavalink;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.apache.hc.core5.http.ParseException;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MusicPlayerHandler {
    private static JdaLavalink LAVALINK;
    private final static AudioPlayerManager PLAYER_MANAGER =
            LavalinkUtil.getPlayerManager();
    private final static Map<Long, MusicPlayerHandler> PLAYER_INSTANCES =
            new ConcurrentHashMap<>();

    private final Guild guild;
    private final TextChannel textChannel;
    private final LavalinkPlayer player;
    private final TrackScheduler scheduler;
    private volatile RepeatMode repeatMode;
    private volatile int position;
    private volatile List<TrackQueue> queue;
    private volatile List<Integer> queueIndex;
    private volatile List<User> skipVotes;
    private volatile List<Long> nowPlayingMessage;
    private volatile AutoLeaveVoiceServer autoLeaveServer;

    private MusicPlayerHandler(Guild guild, TextChannel channel) {
        this.guild = guild;
        this.textChannel = channel;
        this.player = LAVALINK.getLink(guild).getPlayer();
        this.scheduler = new TrackScheduler(this, this.player);
        this.repeatMode = RepeatMode.NONE;
        this.position = 0;
        this.queue = new ArrayList<>();
        this.skipVotes = new ArrayList<>();
        this.nowPlayingMessage = new ArrayList<>();

        this.player.addListener(this.scheduler);
        PLAYER_INSTANCES.put(guild.getIdLong(), this);
    }

    public static JdaLavalink getLavalink() { return LAVALINK; }

    public static void init() {
        LAVALINK = new JdaLavalink(
                KingsBot.getConfig().clientId(), 1, i -> KingsBot.getBOT());
        KingsBot.getConfig().lavalink().forEach(lavalink1 ->
                LAVALINK.addNode(URI.create("ws://" + lavalink1.address()), lavalink1.password()));

        AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER);
        PLAYER_MANAGER.getConfiguration()
                .setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        PLAYER_MANAGER.getConfiguration()
                .setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
    }

    public static MusicPlayerHandler getInstance(Guild guild, TextChannel channel) {
        return PLAYER_INSTANCES.containsKey(guild.getIdLong()) ?
                PLAYER_INSTANCES.get(guild.getIdLong()) :
                new MusicPlayerHandler(guild, channel);
    }

    public static void removeGuild(Guild guild) {
        if(PLAYER_INSTANCES.get(guild.getIdLong()) != null) {
            PLAYER_INSTANCES.get(guild.getIdLong()).removeThisGuild();
        }
    }

    private void removeThisGuild() {
        player.getLink().disconnect();
        player.getLink().destroy();
        PLAYER_INSTANCES.remove(guild.getIdLong());
    }

    public boolean isConnected() {
        return player.getLink().getState() == Link.State.CONNECTED;
    }

    public void connectToVoiceChannel(VoiceChannel channel) {
        LAVALINK.getLink(channel.getGuild()).connect(channel);
    }

    public RepeatMode getRepeatMode() { return repeatMode; }

    public void setRepeatMode(RepeatMode mode) {
        repeatMode = mode;
    }

    public int getQueueSize() {
        return queue.size();
    }

    public List<TrackQueue> getQueue(int offset, int limit) {
        List<TrackQueue> returnQueue = new ArrayList<>();

        for(int i = offset; i <= offset + limit && i < queue.size(); i++) {
            if(isShuffled()) returnQueue.add(queue.get(queueIndex.get(i)));
            returnQueue.add(queue.get(i));
        }

        return returnQueue;
    }

    public synchronized void addToQueue(String identifier, boolean isSoundCloud) {
        addToQueue(queue.size(), identifier, isSoundCloud);
    }

    public synchronized void addToQueue(int position, String identifier, boolean isSoundCloud) {
        FunctionalResultHandler handler = new FunctionalResultHandler(
                track -> {
                    queue.add(new TrackQueue(track.getInfo().uri, track.getInfo().title, TrackQueueType.DIRECT_LINK));
                    if(isShuffled()) queueIndex.add(queueIndex.size(), queueIndex.size() - 1);
                    if(player.getPlayingTrack() == null && !isPaused()) playNextTrack();

                    textChannel.sendMessage(new EmbedBuilder()
                            .setTitle("Added to queue")
                            .setDescription("[" + track.getInfo().title
                                    + "](" + track.getInfo().uri + ")")
                            .build()).queue();
                }, playlist -> {
            if(playlist.isSearchResult()) {
                queue.add(new TrackQueue(
                        playlist.getTracks().get(0).getInfo().uri,
                        playlist.getTracks().get(0).getInfo().title,
                        TrackQueueType.SEARCH));
                if(isShuffled()) queueIndex.add(queueIndex.size(), queueIndex.size() - 1);
            }else{
                playlist.getTracks().parallelStream().forEachOrdered(track -> {
                    queue.add(new TrackQueue(track.getInfo().uri,
                            track.getInfo().title, TrackQueueType.DIRECT_LINK));
                    if(isShuffled()) queueIndex.add(queueIndex.size(), queueIndex.size() - 1);
                });
            }
            if(player.getPlayingTrack() == null && !isPaused()) playNextTrack();
            textChannel.sendMessage(new EmbedBuilder()
                    .setDescription("Added " + playlist.getTracks().size()
                            + " musics to queue.").build()).queue();
        }, () -> textChannel.sendMessage("No matches found.").queue(),
                exception -> textChannel.sendMessage("Something went wrong.").queue());

        if(SpotifyUtils.isSpotify(identifier)) {
            try {
                List<TrackSimplified> tracks = SpotifyUtils.getSpotifyObject(identifier);
                if(tracks.size() < 1) {
                    textChannel.sendMessage("Empty playlist.").queue();
                    return;
                }else if(tracks.size() == 1) {
                    StringBuilder builder = new StringBuilder();
                    List.of(tracks.get(0).getArtists()).forEach(artist -> builder.append(artist.getName()).append(" "));
                    builder.append("- ").append(tracks.get(0).getName());
                    PLAYER_MANAGER.loadItem("https://www.youtube.com/watch?v=" +
                            YoutubeUtils.getFirstResultId(builder.toString()), handler);
                }else{
                    textChannel.sendMessage("Added " + tracks.size() + " musics to queue.").queue();
                    tracks.forEach(track -> {
                        StringBuilder builder = new StringBuilder();
                        List.of(track.getArtists()).forEach(artist -> builder.append(artist.getName()).append(" "));
                        builder.append("- ").append(track.getName());
                        queue.add(new TrackQueue(builder.toString(), builder.toString(), TrackQueueType.SPOTIFY));
                        if(isShuffled()) queueIndex.add(queue.size() - 1);
                    });
                }
                if(player.getPlayingTrack() == null && !isPaused())
                    playNextTrack();
            }catch(IOException | ParseException | SpotifyWebApiException e) {
                textChannel.sendMessage("Something went wrong.").queue();
                LogManager.getLogger(MusicPlayerHandler.class).error(e.getMessage());
            }
            return;
        }

        if(!Patterns.WEB_URL.matcher(identifier).matches()) {
            identifier = isSoundCloud?
                    "scsearch:" + identifier :
                    "https://www.youtube.com/watch?v=" + YoutubeUtils.getFirstResultId(identifier);
        }

        PLAYER_MANAGER.loadItem(identifier, handler);
    }

    public synchronized boolean removeFromQueue(int position) {
        if(isShuffled()) queueIndex.remove(queue.indexOf(queue.get(position)));
        return queue.remove(queue.get(position));
    }

    public synchronized void changePositionOnQueue(int oldPosition, int newPosition) {
        if(oldPosition <= newPosition)
            Collections.rotate(queue.subList(oldPosition, newPosition + 1), -1);
        else Collections.rotate(queue.subList(newPosition, oldPosition + 1), 1);
    }

    public synchronized boolean isPaused() {
        return player.isPaused();
    }

    public synchronized boolean togglePause() {
        if(this.isPaused()) {
            player.setPaused(false);
            if(player.getPlayingTrack() != null)
                nowPlayingMessage.add(textChannel.sendMessage(new EmbedBuilder()
                        .setTitle("Now Playing")
                        .setDescription("[" + player.getPlayingTrack().getInfo().title + "]("
                                + player.getPlayingTrack().getInfo().uri + ")")
                        .build()).complete().getIdLong());
        }else{
            player.setPaused(true);
            nowPlayingMessage.forEach(textChannel::purgeMessagesById);
        }
        return this.isPaused();
    }

    public void shuffleQueue() {
        queueIndex = new ArrayList<>();
        for(int i = 0; i < queue.size(); i++)
            queueIndex.add(i);

        Collections.shuffle(queueIndex);
    }

    public void unshuffleQueue() {
        queueIndex.clear();
    }

    public int getVolume() {
        return player.getVolume();
    }

    public void setVolume(int volume) {
        player.setVolume(volume);
    }

    public synchronized int getVoteCounts() {
        return skipVotes.size();
    }

    public synchronized boolean addVote(User user) {
        if(skipVotes.contains(user)) return false;
        else skipVotes.add(user);
        return true;
    }

    public synchronized void removeVote(User user) {
        skipVotes.remove(user);
    }

    public synchronized boolean isShuffled() {
        return queueIndex != null && !queueIndex.isEmpty();
    }

    public synchronized void forceSkip() {
        textChannel.sendMessage("Skipped!").queue();
        scheduler.skipTrack();
    }

    public synchronized void stopQueue() {
        queue.clear();
        if(queueIndex != null) queueIndex.clear();
        if(player.getPlayingTrack() != null) player.stopTrack();

        if(autoLeaveServer == null || !autoLeaveServer.isAlive()) {
            autoLeaveServer = new AutoLeaveVoiceServer(guild);
            autoLeaveServer.start();
        }
    }

    public synchronized void onTrackStart() {
        skipVotes.clear();

        final AudioTrack track = player.getPlayingTrack();
        nowPlayingMessage.add(textChannel.sendMessage(new EmbedBuilder()
                .setTitle("Now Playing")
                .setDescription("[" + track.getInfo().title + "]("
                        + player.getPlayingTrack().getInfo().uri + ")")
                .build()).complete().getIdLong());
    }

    public synchronized void onTrackEnd() {
        nowPlayingMessage.forEach(textChannel::purgeMessagesById);
        if(repeatMode.equals(RepeatMode.TRACK)) return;

        if(position == queue.size() - 1) {
            textChannel.sendMessage("Queue ended!").queue();
            position++;
            if(autoLeaveServer == null || !autoLeaveServer.isAlive()) {
                autoLeaveServer = new AutoLeaveVoiceServer(guild);
                autoLeaveServer.start();
            }
        }else{
            playNextTrack();
            position++;
        }
    }

    public synchronized void playNextTrack() {
        if(autoLeaveServer != null && autoLeaveServer.isAlive()) {
            autoLeaveServer.cancel();
            autoLeaveServer = null;
        }

        if(position == queue.size()) {
            if(repeatMode.equals(RepeatMode.QUEUE)) {
                position = 0;
            }
        }
        if(position >= queue.size()) {
            autoLeaveServer = new AutoLeaveVoiceServer(guild);
            autoLeaveServer.start();
            return;
        }

        FunctionalResultHandler handler = new FunctionalResultHandler(
                scheduler::queue, playlist -> {
            if(playlist.isSearchResult())
                scheduler.queue(playlist.getTracks().get(0));
            else playlist.getTracks().forEach(scheduler::queue); }, () -> {
            textChannel.sendMessage("No matches found.").queue();
            playNextTrack(); }, exception -> {
            textChannel.sendMessage("Something went wrong.").queue();
            playNextTrack(); });

        TrackQueue trackQueue = isShuffled() ? this.queue.get(queueIndex.get(position)) : queue.get(position);
        PLAYER_MANAGER.loadItem(trackQueue.query(), handler);
    }
}