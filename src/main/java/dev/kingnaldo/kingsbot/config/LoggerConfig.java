package dev.kingnaldo.kingsbot.config;

import dev.kingnaldo.kingsbot.KingsBot;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class LoggerConfig {

    public static void load() {
        ConfigurationBuilder<BuiltConfiguration> builder =
                ConfigurationBuilderFactory.newConfigurationBuilder();

        AppenderComponentBuilder console =
                builder.newAppender("stdout", "Console");
        AppenderComponentBuilder file =
                builder.newAppender("log", "File");
        file.addAttribute("fileName", "logs/log.log");

        LayoutComponentBuilder standard = builder.newLayout("PatternLayout");
        standard.addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable");
        console.add(standard);
        file.add(standard);

        RootLoggerComponentBuilder rootLogger =
                builder.newRootLogger(Level.toLevel(KingsBot.getConfig().verbose(), Level.WARN));
        rootLogger.add(builder.newAppenderRef("stdout"));

        builder.add(console);
        builder.add(file);
        builder.add(rootLogger);

        Configurator.initialize(builder.build());
    }
}
