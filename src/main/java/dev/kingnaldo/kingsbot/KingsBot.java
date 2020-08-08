package dev.kingnaldo.kingsbot;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import dev.kingnaldo.kingsbot.commands.CommandManager;
import dev.kingnaldo.kingsbot.config.Config;
import dev.kingnaldo.kingsbot.config.ConfigManager;
import dev.kingnaldo.kingsbot.db.DatabaseManager;
import dev.kingnaldo.kingsbot.music.MusicPlayerHandler;
import dev.kingnaldo.kingsbot.music.spotify.SpotifyConnector;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.hc.core5.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;

public class KingsBot {
    public static final Logger LOGGER = LogManager.getLogger(KingsBot.class);

    private static JDA BOT = null;
    private static Config CONFIG;

    private static SpotifyApi SPOTIFY_API;

    public static void main(String[] args) {
        try {
            CONFIG = ConfigManager.load();
            if(CONFIG == null) {
                LOGGER.info("Shutting down, config.json missing.");
                return;
            }

            DatabaseManager.connect();
            MusicPlayerHandler.init();

            JDABuilder builder = JDABuilder.create(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));
            builder.setToken(CONFIG.token());
            builder.setActivity(Activity.listening("Duck Machine on Spotify"));
            builder.addEventListeners(new CommandManager());

            BOT = builder.build().awaitStatus(JDA.Status.CONNECTED);

            SPOTIFY_API = SpotifyConnector.getSpotifyAPI();
        }catch(IOException | InterruptedException | LoginException | NullPointerException |
                ParseException | SpotifyWebApiException | URISyntaxException e) {
            LOGGER.fatal(e.getCause().getMessage());
            DatabaseManager.disconnect();
            BOT.shutdown();
        }
    }

    public static JDA getBOT() { return KingsBot.BOT; }
    public static Config getConfig() { return KingsBot.CONFIG; }
    public static SpotifyApi getSpotifyAPI() { return  KingsBot.SPOTIFY_API; }
}
