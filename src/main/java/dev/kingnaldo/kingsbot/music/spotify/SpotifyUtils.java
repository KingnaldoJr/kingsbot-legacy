package dev.kingnaldo.kingsbot.music.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.enums.ModelObjectType;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import dev.kingnaldo.kingsbot.KingsBot;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SpotifyUtils {
    private static final Pattern SPOTIFY_ALBUM = Pattern.compile("^(?:(?:(?:https://)?(?:open\\.spotify\\.com)(?:/embed)?)|spotify)(?:/album/|:album:)([a-zA-Z0-9]{22})(?:.*)");
    private static final Pattern SPOTIFY_ARTIST = Pattern.compile("^(?:(?:(?:https://)?(?:open\\.spotify\\.com)(?:/embed)?)|spotify)(?:/artist/|:artist:)([a-zA-Z0-9]{22})(?:.*)");
    private static final Pattern SPOTIFY_EPISODE = Pattern.compile("^(?:(?:(?:https://)?(?:open\\.spotify\\.com)(?:/embed)?)|spotify)(?:/episode/|:episode:)([a-zA-Z0-9]{22})(?:.*)");
    private static final Pattern SPOTIFY_PLAYLIST = Pattern.compile("^(?:(?:(?:https://)?(?:open\\.spotify\\.com)(?:/embed)?)|spotify)(?:/playlist/|:playlist:)([a-zA-Z0-9]{22})(?:.*)");
    private static final Pattern SPOTIFY_TRACK = Pattern.compile("^(?:(?:(?:https://)?(?:open\\.spotify\\.com)(?:/embed)?)|spotify)(?:/track/|:track:)([a-zA-Z0-9]{22})(?:.*)");
    private static final Pattern SPOTIFY_SHOW = Pattern.compile("^(?:(?:(?:https://)?(?:open\\.spotify\\.com)(?:/embed)?)|spotify)(?:/show/|:show:)([a-zA-Z0-9]{22})(?:.*)");

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

    public static List<TrackSimplified> getSpotifyObject(String url) throws IOException, ParseException, SpotifyWebApiException {
        SpotifyType type = getType(url);
        if(type == null) return null;
        List<TrackSimplified> tracks = new ArrayList<>();

        switch(type) {
            case ALBUM -> {
                int offset = 0, total;
                do {
                    Paging<TrackSimplified> albumTracks = KingsBot.getSpotifyAPI().getAlbumsTracks(
                            SPOTIFY_ALBUM.matcher(url).results().findFirst().get().group(1))
                            .offset(offset).build().execute();

                    tracks.addAll(List.of(albumTracks.getItems()));

                    offset += albumTracks.getLimit();
                    total = albumTracks.getTotal();
                }while(offset <= total);
            }
            case ARTIST -> {
                Track[] topTracks = KingsBot.getSpotifyAPI().getArtistsTopTracks(
                        SPOTIFY_ARTIST.matcher(url).results().findFirst().get().group(1), CountryCode.BR)
                        .build().execute();
                List.of(topTracks).forEach(track -> {
                    TrackSimplified trackSimplified = new TrackSimplified.Builder()
                            .setArtists(track.getArtists())
                            .setName(track.getName()).build();
                    tracks.add(trackSimplified);
                });
            }
            case PLAYLIST -> {
                int offset = 0, total;
                do {
                    Paging<PlaylistTrack> playlistTracks = KingsBot.getSpotifyAPI().getPlaylistsItems(
                            SPOTIFY_PLAYLIST.matcher(url).results().findFirst().get().group(1))
                            .offset(offset).build().execute();

                    List.of(playlistTracks.getItems()).parallelStream().forEachOrdered(playlistTrack -> {
                        if(playlistTrack.getTrack().getType().equals(ModelObjectType.TRACK)) {
                            TrackSimplified trackSimplified = new TrackSimplified.Builder()
                                    .setArtists(((Track) playlistTrack.getTrack()).getArtists())
                                    .setName(((Track) playlistTrack.getTrack()).getName()).build();

                            tracks.add(trackSimplified);
                        }
                    });

                    offset += playlistTracks.getLimit();
                    total = playlistTracks.getTotal();
                }while(offset <= total);
            }
            case TRACK -> {
                Track track = KingsBot.getSpotifyAPI().getTrack(
                        SPOTIFY_TRACK.matcher(url).results().findFirst().get().group(1)).build().execute();

                TrackSimplified trackSimplified = new TrackSimplified.Builder()
                        .setArtists(track.getArtists())
                        .setName(track.getName()).build();

                tracks.add(trackSimplified);
            }
            default -> {}
        }

        return tracks;
    }
}
