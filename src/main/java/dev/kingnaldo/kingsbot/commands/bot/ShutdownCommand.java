package dev.kingnaldo.kingsbot.commands.bot;

import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.commands.Command;
import dev.kingnaldo.kingsbot.config.Config;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

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
    public String usage() {
        return "Use " + Config.get("PREFIX") + "shutdown to shutdown the bot!";
    }

    @Override
    public void execute(TextChannel channel, Member author, Message message, List<String> args) {
        if(author.getId().equals(Config.get("OWNER_ID"))) {
            KingsBot.LOGGER.info("Shutdown command executed, shutdown now.");
            KingsBot.getBOT().shutdown();
        }
    }
}
