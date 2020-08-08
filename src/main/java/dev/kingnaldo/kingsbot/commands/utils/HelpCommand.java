package dev.kingnaldo.kingsbot.commands.utils;

import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.commands.Command;
import dev.kingnaldo.kingsbot.commands.CommandCategory;
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
    public CommandCategory category() { return CommandCategory.MUSIC; }

    @Override
    public String usage() {
        return "Use " + KingsBot.getConfig().prefix() + "help to get help.";
    }

    @Override
    public void execute(TextChannel channel, Member author, Message message, List<String> args) {
        StringBuilder builder = new StringBuilder();
        commands.forEach(command -> builder.append(command.usage()).append("\n"));
        channel.sendMessage(builder.toString()).queue();
    }
}
