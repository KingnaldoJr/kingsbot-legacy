package dev.kingnaldo.kingsbot.commands.utils;

import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.commands.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class PingCommand implements Command {

    @Override
    public String name() {
        return "ping";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String usage() {
        return "Use " + KingsBot.getCommandPrefix() + "ping to test the bot's ping.";
    }

    @Override
    public void execute(TextChannel channel, Member author, Message message, List<String> args) {
        channel.sendMessage("Pong!").queue(msg -> msg.editMessageFormat("The bot ping is %sms", KingsBot.getBOT().getGatewayPing()).queue());
    }
}
