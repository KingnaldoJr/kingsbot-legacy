package dev.kingnaldo.kingsbot.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends PlayerEventListenerAdapter {
    private final MusicPlayerHandler musicPlayer;
    private final LavalinkPlayer player;
    private final BlockingQueue<AudioTrack> queue;

    public TrackScheduler(MusicPlayerHandler musicPlayer, LavalinkPlayer player) {
        this.musicPlayer = musicPlayer;
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if(player.getPlayingTrack() == null) {
            player.playTrack(track);
        }else queue.offer(track);
    }

    @Override
    public void onTrackStart(IPlayer player, AudioTrack track) {
        musicPlayer.onTrackStart();
    }

    public void skipTrack() {
        player.stopTrack();
        musicPlayer.onTrackEnd();
    }

    @Override
    public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(endReason.mayStartNext) {
            if(musicPlayer.getRepeatMode().equals(RepeatMode.TRACK)) {
                player.playTrack(track.makeClone());
                musicPlayer.onTrackEnd();
            }else this.skipTrack();
        }
    }

    public boolean queueIsEmpty() {
        return queue.isEmpty();
    }
}
