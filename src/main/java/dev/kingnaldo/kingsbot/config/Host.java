package dev.kingnaldo.kingsbot.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Host(
        @JsonProperty("hostname") String hostname,
        @JsonProperty("port") int port
) {}