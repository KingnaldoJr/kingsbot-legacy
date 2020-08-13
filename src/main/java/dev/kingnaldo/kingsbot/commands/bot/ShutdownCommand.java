package dev.kingnaldo.kingsbot.commands.bot;

import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.commands.Command;
import dev.kingnaldo.kingsbot.commands.CommandCategory;
import dev.kingnaldo.kingsbot.db.DatabaseManager;
import dev.kingnaldo.kingsbot.music.MusicPlayerHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.LogManager;

import java.util.List;

public class ShutdownCommand implements Command {
    @Override
    public String name() {
        return "shutdown";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public CommandCategory category() { return CommandCategory.BOT; }

    @Override
    public String usage() {
        return "Use " + KingsBot.getConfig().prefix() + "shutdown to shutdown the bot!";
    }

    @Override
    public void execute(TextChannel channel, Member author, Message message, List<String> args) {
        if(author.getId().equals(KingsBot.getConfig().ownerId())) {
            LogManager.getLogger(ShutdownCommand.class).info("Shutdown command executed, shutting down now.");
            DatabaseManager.disconnect();
            MusicPlayerHandler.getLavalink().shutdown();
            KingsBot.getBOT().shutdown();
        }
    }
}
