package dev.kingnaldo.kingsbot.commands.music;

import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.commands.Command;
import dev.kingnaldo.kingsbot.commands.CommandCategory;
import dev.kingnaldo.kingsbot.music.MusicPlayerHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class StopCommand implements Command {
    @Override
    public String name() {
        return "stop";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public CommandCategory category() { return CommandCategory.MUSIC; }

    @Override
    public String usage() {
        return "Use " + KingsBot.getConfig().prefix() + "stop to stop the current queue.";
    }

    @Override
    public void execute(TextChannel channel, Member author, Message message, List<String> args) {
        if(!channel.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
            channel.sendMessage("Not connected on voice channel!").queue();
            return;
        }

        if(author.getVoiceState().inVoiceChannel() &&
                !author.getVoiceState().getChannel().equals(
                        channel.getGuild().getSelfMember().getVoiceState().getChannel())) {
            channel.sendMessage("You're not in the same channel as bot").queue();
            return;
        }

        MusicPlayerHandler.getInstance(message.getGuild(), channel).stopQueue();
        channel.sendMessage("Queue stopped!").queue();
    }
}
