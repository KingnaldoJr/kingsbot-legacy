package dev.kingnaldo.kingsbot.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Lavalink(
        @JsonProperty("address") String address,
        @JsonProperty("password") String password
) {}
