package dev.kingnaldo.kingsbot.commands.utils;

import dev.kingnaldo.kingsbot.commands.Command;
import dev.kingnaldo.kingsbot.config.Config;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand implements Command {
    public static List<Command> commands = new ArrayList<>();

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String[] aliases() {
        return new String[]{ "h" };
    }

    @Override
    public String usage() {
        return "Use " + Config.get("PREFIX") + "help to get help.";
    }

    @Override
    public void execute(TextChannel channel, Member author, Message message, List<String> args) {
        StringBuilder builder = new StringBuilder();
        commands.forEach(command -> builder.append(command.usage()).append("\n"));
        channel.sendMessage(builder.toString()).queue();
    }
}
