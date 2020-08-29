package dev.kingnaldo.kingsbot;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import dev.kingnaldo.kingsbot.commands.CommandManager;
import dev.kingnaldo.kingsbot.config.Config;
import dev.kingnaldo.kingsbot.config.ConfigManager;
import dev.kingnaldo.kingsbot.config.LoggerConfig;
import dev.kingnaldo.kingsbot.db.DatabaseManager;
import dev.kingnaldo.kingsbot.music.MusicPlayerHandler;
import dev.kingnaldo.kingsbot.music.spotify.SpotifyConnector;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.hc.core5.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KingsBot {
    private static Logger LOGGER;

    private static JDA BOT = null;
    private static Config CONFIG;

    private static SpotifyApi SPOTIFY_API;

    public static void main(String[] args) {
        try {
            CONFIG = ConfigManager.load();
            if(CONFIG == null) {
                LOGGER.warn("Shutting down, config.json missing.");
                return;
            }

            LoggerConfig.init();
            LOGGER = LogManager.getLogger(KingsBot.class);

            DatabaseManager.connect();
            MusicPlayerHandler.init();

            SPOTIFY_API = SpotifyConnector.getSpotifyAPI();
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                    () -> SpotifyConnector.updateAccessToken(SPOTIFY_API),
                    3540, 3540, TimeUnit.SECONDS);

            JDABuilder builder = JDABuilder.create(
                    CONFIG.token(),
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_VOICE_STATES);
            builder.setMemberCachePolicy(MemberCachePolicy.ONLINE);
            builder.disableCache(
                    CacheFlag.ACTIVITY,
                    CacheFlag.CLIENT_STATUS,
                    CacheFlag.EMOTE);
            builder.setActivity(Activity.listening("Duck Machine on Spotify"));
            builder.addEventListeners(new CommandManager());
            builder.addEventListeners(MusicPlayerHandler.getLavalink());
            builder.setVoiceDispatchInterceptor(MusicPlayerHandler.getLavalink().getVoiceInterceptor());

            BOT = builder.build();
        }catch(IOException | LoginException | NullPointerException |
                ParseException | SpotifyWebApiException | URISyntaxException e) {
            LOGGER.fatal(e.getCause().getMessage());
            DatabaseManager.disconnect();
            BOT.shutdown();
        }
    }

    public static JDA getBOT() { return BOT; }
    public static Config getConfig() { return CONFIG; }
    public static SpotifyApi getSpotifyAPI() { return  SPOTIFY_API; }
}
