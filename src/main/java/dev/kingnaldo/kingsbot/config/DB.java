package dev.kingnaldo.kingsbot.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DB(
        @JsonProperty("hosts") List<Host> hosts,
        @JsonProperty("database") String database,
        @JsonProperty("username") String username,
        @JsonProperty("password") String password
) {}