package dev.kingnaldo.kingsbot.music.spotify.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyTrack(
        @JsonProperty("artists") List<SpotifySimplifiedArtist> artists,
        @JsonProperty("name") String name,
        @JsonProperty("uri") String uri,
        @JsonProperty("is_local") boolean isLocal
) {}