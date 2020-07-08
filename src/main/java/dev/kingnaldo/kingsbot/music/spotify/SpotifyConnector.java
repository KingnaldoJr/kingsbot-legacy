package dev.kingnaldo.kingsbot.music.spotify;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.music.spotify.objects.SpotifyAccessToken;
import dev.kingnaldo.kingsbot.utils.HTTPRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class SpotifyConnector {

    public static SpotifyAccessToken getAccessToken() throws IOException {
        String url = "https://accounts.spotify.com/api/token";
        List<String> bodyParameters = List.of("grant_type=client_credentials");
        Map<String, String> headers = Map.of(
                "Accept", "application/json",
                "Authorization", "Basic " +
                        Base64.getEncoder().encodeToString((
                                KingsBot.getProperties().getProperty("spotify.id") + ":" +
                                        KingsBot.getProperties().getProperty("spotify.secret"))
                                .getBytes()));
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(HTTPRequest.POST(url, bodyParameters, headers)));

        SpotifyAccessToken accessToken = new ObjectMapper().readValue(reader, SpotifyAccessToken.class);

        reader.close();
        return accessToken;
    }
}
