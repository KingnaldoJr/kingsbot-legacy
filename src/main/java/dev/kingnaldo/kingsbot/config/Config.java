package dev.kingnaldo.kingsbot.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Config(
        @JsonProperty("token") String token,
        @JsonProperty("owner_id") String ownerId,
        @JsonProperty("prefix") String prefix,
        @JsonProperty("verbose") String verbose,
        @JsonProperty("db") DB db,
        @JsonProperty("youtube_key") String youtubeKey,
        @JsonProperty("spotify") Spotify spotify
) {}