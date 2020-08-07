package dev.kingnaldo.kingsbot.config;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private static Dotenv dotenv;

    public static void load() {
        dotenv = Dotenv.load();
    }

    public static String get(String config) {
        return dotenv.get(config);
    }
}
