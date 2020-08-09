package dev.kingnaldo.kingsbot.music.spotify;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import dev.kingnaldo.kingsbot.KingsBot;
import org.apache.hc.core5.http.ParseException;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class SpotifyConnector {

    public static SpotifyApi getSpotifyAPI() throws ParseException, SpotifyWebApiException, IOException {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(KingsBot.getConfig().spotify().clientId())
                .setClientSecret(KingsBot.getConfig().spotify().clientSecret())
                .build();

        spotifyApi.setAccessToken(spotifyApi.clientCredentials().build().execute().getAccessToken());
        return spotifyApi;
    }

    public static void updateAccessToken(SpotifyApi api) {
        CompletableFuture<ClientCredentials> clientCredentialsFuture =
                api.clientCredentials().build().executeAsync();

        clientCredentialsFuture.whenComplete((cc, thr) -> {
            if(thr == null) api.setAccessToken(cc.getAccessToken());
            else LogManager.getLogger(SpotifyConnector.class).error(thr.getMessage());
        });
    }
}
