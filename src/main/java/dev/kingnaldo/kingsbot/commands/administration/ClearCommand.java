package dev.kingnaldo.kingsbot.commands.administration;

import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.commands.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class ClearCommand implements Command {

    @Override
    public String name() {
        return "clear";
    }

    @Override
    public String[] aliases() {
        return new String[]{ "cl" };
    }

    @Override
    public String usage() {
        return """
                Use %prefix%clear to delete all messages of the channel.
                Use %prefix%clear <amount> to delete the last <amount> messages from the channel.
                """.replaceAll("%prefix%", KingsBot.getCommandPrefix());
    }

    @Override
    public void execute(TextChannel channel, Member author, Message message, List<String> args) {
        if(!author.hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            channel.sendMessage("You don't have permission to do this.").queue();
            return;
        }

        if(!channel.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            channel.sendMessage("I don't have permission to do this.").queue();
            return;
        }

        if(args.size() == 0) {
            channel.getIterableHistory().parallelStream().forEach(msg -> msg.getChannel().purgeMessages(msg));
        }else if(args.size() == 1 && args.get(0).matches("\\d+")) {
            int amount = Integer.parseInt(args.get(0));
            if(amount < 100 && amount > 1) {
                channel.getHistory().retrievePast(amount).complete().parallelStream()
                        .forEach(msg -> msg.delete().queue());
            }else{
                channel.sendMessage("Invalid amount of messages, " +
                        "choose a number between 2 and 100.").queue();
            }
        }else{
            channel.sendMessage("Invalid command usage, use %prefix%help clear for help."
                    .replaceAll("%prefix%", KingsBot.getCommandPrefix())).queue();
        }
    }
}
