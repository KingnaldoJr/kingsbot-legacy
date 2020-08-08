package dev.kingnaldo.kingsbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kingnaldo.kingsbot.KingsBot;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ConfigManager {
    public static Config load() throws IOException, URISyntaxException {
        File configFile = new File("./config.json");
        if(!configFile.exists()) {
            Path example = Paths.get(KingsBot.class.getResource("config.json.example").toURI());
            Path newFile = configFile.toPath();
            Files.copy(example, newFile, StandardCopyOption.COPY_ATTRIBUTES);

            return null;
        }else return new ObjectMapper().readValue(configFile, Config.class);
    }
}
