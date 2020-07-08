package dev.kingnaldo.kingsbot.music.spotify.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyAccessToken(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") int expiresIn
) {}