package dev.kingnaldo.kingsbot.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public interface Command {
    String name();
    String[] aliases();
    CommandCategory category();
    String usage();
    void execute(TextChannel channel, Member author, Message message, List<String> args);
}
