package dev.kingnaldo.kingsbot;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import dev.kingnaldo.kingsbot.commands.CommandManager;
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
import java.io.InputStream;
import java.util.Properties;

public class KingsBot {
    public static final Logger LOGGER = LogManager.getLogger(KingsBot.class);

    private static JDA BOT = null;

    private static Properties properties;
    private static String COMMAND_PREFIX;

    private static SpotifyApi SPOTIFY_API;

    public static void main(String[] args) {
        try {
            InputStream inputStream = KingsBot.class.getClassLoader().getResourceAsStream("config.properties");
            if(inputStream == null) {
                KingsBot.LOGGER.fatal("Unable to find config.properties.");
                return;
            }

            KingsBot.properties = new Properties();
            KingsBot.properties.load(inputStream);
            inputStream.close();

            JDABuilder builder = JDABuilder.create(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));
            builder.setToken(KingsBot.getProperties().getProperty("token"));
            builder.setActivity(Activity.listening("Duck Machine on Spotify"));
            builder.addEventListeners(new CommandManager());

            KingsBot.COMMAND_PREFIX = KingsBot.getProperties().getProperty("prefix");

            MusicPlayerHandler.init();

            KingsBot.BOT = builder.build().awaitStatus(JDA.Status.CONNECTED);

            KingsBot.SPOTIFY_API = SpotifyConnector.getSpotifyAPI();
            SpotifyConnector.updateAccessToken(KingsBot.SPOTIFY_API);
        }catch(IOException | InterruptedException | LoginException
                | NullPointerException | ParseException | SpotifyWebApiException e) {
            KingsBot.LOGGER.fatal(e.getCause().getMessage());
            KingsBot.BOT.shutdown();
        }
    }

    public static JDA getBOT() { return KingsBot.BOT; }
    public static String getCommandPrefix() { return KingsBot.COMMAND_PREFIX; }
    public static Properties getProperties() { return KingsBot.properties; }
    public static SpotifyApi getSpotifyAPI() { return  KingsBot.SPOTIFY_API; }
}
