package dev.kingnaldo.kingsbot.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Config(
        @JsonProperty("token") String token,
        @JsonProperty("client_id") String clientId,
        @JsonProperty("owner_id") String ownerId,
        @JsonProperty("prefix") String prefix,
        @JsonProperty("verbose") String verbose,
        @JsonProperty("db") DB db,
        @JsonProperty("lavalink") List<Lavalink> lavalink,
        @JsonProperty("youtube_key") String youtubeKey,
        @JsonProperty("spotify") Spotify spotify,
        @JsonProperty("ksoft_token") String KSoftToken
) {}