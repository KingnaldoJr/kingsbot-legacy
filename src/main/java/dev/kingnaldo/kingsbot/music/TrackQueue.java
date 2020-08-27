package dev.kingnaldo.kingsbot.music;

public record TrackQueue(
        String query,
        String name,
        TrackQueueType type
) {}
