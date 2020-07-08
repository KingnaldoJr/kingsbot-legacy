package dev.kingnaldo.kingsbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final MusicPlayerHandler musicPlayer;
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;

    public TrackScheduler(MusicPlayerHandler musicPlayer, AudioPlayer player) {
        this.musicPlayer = musicPlayer;
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if(!this.player.startTrack(track, true))
            this.queue.offer(track);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        this.musicPlayer.onTrackStart();
    }

    public void skipTrack() {
        this.player.stopTrack();
        this.musicPlayer.onTrackEnd();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(endReason.mayStartNext) {
            if(this.musicPlayer.getRepeatMode().equals(RepeatMode.TRACK)) {
                player.startTrack(track.makeClone(), false);
                musicPlayer.onTrackEnd();
            }else this.skipTrack();
        }
    }

    public boolean queueIsEmpty() {
        return queue.isEmpty();
    }
}
