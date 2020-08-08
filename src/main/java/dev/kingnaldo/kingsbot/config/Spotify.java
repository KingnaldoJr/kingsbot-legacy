package dev.kingnaldo.kingsbot.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Spotify(
        @JsonProperty("client_id") String clientId,
        @JsonProperty("client_secret") String clientSecret
) {}