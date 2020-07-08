package dev.kingnaldo.kingsbot.music.spotify;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.music.spotify.objects.*;
import dev.kingnaldo.kingsbot.utils.HTTPRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SpotifyUtils {
    private static final Pattern SPOTIFY_ALBUM = Pattern.compile("^(?:(?:(?:https://)?(?:open\\.spotify\\.com)(?:/embed)?)|spotify)(?:/album/|:album:)([a-zA-Z0-9]{22})(?:.*)");
    private static final Pattern SPOTIFY_ARTIST = Pattern.compile("^(?:(?:(?:https://)?(?:open\\.spotify\\.com)(?:/embed)?)|spotify)(?:/artist/|:artist:)([a-zA-Z0-9]{22})(?:.*)");
    private static final Pattern SPOTIFY_EPISODE = Pattern.compile("^(?:(?:(?:https://)?(?:open\\.spotify\\.com)(?:/embed)?)|spotify)(?:/episode/|:episode:)([a-zA-Z0-9]{22})(?:.*)");
    private static final Pattern SPOTIFY_PLAYLIST = Pattern.compile("^(?:(?:(?:https://)?(?:open\\.spotify\\.com)(?:/embed)?)|spotify)(?:/playlist/|:playlist:)([a-zA-Z0-9]{22})(?:.*)");
    private static final Pattern SPOTIFY_TRACK = Pattern.compile("^(?:(?:(?:https://)?(?:open\\.spotify\\.com)(?:/embed)?)|spotify)(?:/track/|:track:)([a-zA-Z0-9]{22})(?:.*)");
    private static final Pattern SPOTIFY_SHOW = Pattern.compile("^(?:(?:(?:https://)?(?:open\\.spotify\\.com)(?:/embed)?)|spotify)(?:/show/|:show:)([a-zA-Z0-9]{22})(?:.*)");

    private static final String API_BASE_URL = "https://api.spotify.com/v1/";

    public static boolean isSpotify(String url) {
        return SPOTIFY_ALBUM.matcher(url).matches() ||
                SPOTIFY_ARTIST.matcher(url).matches() ||
                SPOTIFY_EPISODE.matcher(url).matches() ||
                SPOTIFY_PLAYLIST.matcher(url).matches() ||
                SPOTIFY_TRACK.matcher(url).matches() ||
                SPOTIFY_SHOW.matcher(url).matches();
    }

    public static SpotifyType getType(String url) {
        if(SPOTIFY_ALBUM.matcher(url).find()) return SpotifyType.ALBUM;
        else if(SPOTIFY_ARTIST.matcher(url).find()) return SpotifyType.ARTIST;
        else if(SPOTIFY_EPISODE.matcher(url).find()) return SpotifyType.EPISODE;
        else if(SPOTIFY_PLAYLIST.matcher(url).find()) return SpotifyType.PLAYLIST;
        else if(SPOTIFY_TRACK.matcher(url).find()) return SpotifyType.TRACK;
        else if(SPOTIFY_SHOW.matcher(url).find()) return SpotifyType.SHOW;
        else return null;
    }

    public static List<SpotifySimplifiedTrack> getSpotifyObject(String url) throws IOException {
        SpotifyType type = getType(url);
        if(type == null) return null;

        List<SpotifySimplifiedTrack> tracks = new ArrayList<>();
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, String> headers = Map.of(
                "Accept", "application/json",
                "Content-Type", "application/json",
                "Authorization", "Bearer " +
                        KingsBot.getSpotifyAccessToken().accessToken());

        switch(type) {
            case ALBUM -> {
                int offset = 0, total;
                do {
                    SpotifyAlbumTracks albumTracks = mapper.readValue(
                            HTTPRequest.GET(API_BASE_URL + "albums/" +
                                    SPOTIFY_ALBUM.matcher(url).results().findFirst().get().group(1) +
                                    "/tracks?limit=50&offset=" + offset, headers),
                            SpotifyAlbumTracks.class);

                    tracks.addAll(albumTracks.items());

                    offset += albumTracks.limit();
                    total = albumTracks.total();
                }while(offset <= total);
            }
            case ARTIST -> {
                SpotifyArtistTopTracks topTracks = mapper.readValue(
                        HTTPRequest.GET(API_BASE_URL + "artists/" +
                                SPOTIFY_ARTIST.matcher(url).results().findFirst().get().group(1) +
                                "/top-tracks?market=BR", headers),
                        SpotifyArtistTopTracks.class);
                tracks.addAll(topTracks.tracks());
            }
            case PLAYLIST -> {
                int offset = 0, total;
                do {
                    SpotifyPlaylistPaging playlistTracks = mapper.readValue(
                            HTTPRequest.GET(API_BASE_URL + "playlists/" +
                                    SPOTIFY_PLAYLIST.matcher(url).results().findFirst().get().group(1)
                                    + "/tracks?fields=items(track(artists(name,uri)," +
                                    "duration_ms,name,uri,is_local)),limit,offset,total&offset=" +
                                    offset, headers),
                            SpotifyPlaylistPaging.class);

                    playlistTracks.items().forEach(playlistTrack ->
                            tracks.add(new SpotifySimplifiedTrack(playlistTrack.track().artists(),
                                playlistTrack.track().name(),
                                playlistTrack.track().uri())));

                    offset += playlistTracks.limit();
                    total = playlistTracks.total();
                }while(offset <= total);
            }
            case TRACK -> {
                SpotifyTrack track = mapper.readValue(
                        HTTPRequest.GET(API_BASE_URL + "tracks/" +
                                SPOTIFY_TRACK.matcher(url).results().findFirst().get().group(1),
                                headers),
                        SpotifyTrack.class);
                tracks.add(new SpotifySimplifiedTrack(
                        track.artists(), track.name(), track.uri()));
            }
            default -> {}
        }

        return tracks;
    }
}
