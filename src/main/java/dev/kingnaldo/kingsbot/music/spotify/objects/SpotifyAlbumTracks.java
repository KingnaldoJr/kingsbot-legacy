package dev.kingnaldo.kingsbot.music.spotify.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyAlbumTracks(
        @JsonProperty("items") List<SpotifySimplifiedTrack> items,
        @JsonProperty("limit") int limit,
        @JsonProperty("offset") int offset,
        @JsonProperty("total") int total
) {}