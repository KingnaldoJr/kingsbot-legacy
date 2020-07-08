package dev.kingnaldo.kingsbot.commands.music;

import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.commands.Command;
import dev.kingnaldo.kingsbot.music.MusicPlayerHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class SkipCommand implements Command {
    @Override
    public String name() {
        return "skip";
    }

    @Override
    public String[] aliases() {
        return new String[]{ "next" };
    }

    @Override
    public String usage() {
        return "Use " + KingsBot.getCommandPrefix() + " skip to skip to next music.";
    }

    @Override
    public void execute(TextChannel channel, Member author, Message message, List<String> args) {
        if(!channel.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
            channel.sendMessage("Not connected to a voice channel.").queue();
            return;
        }

        if(!author.getVoiceState().inVoiceChannel()) {
            channel.sendMessage("You're not connected in a voice channel.").queue();
            return;
        }

        if(author.getVoiceState().inVoiceChannel() &&
                author.getVoiceState().getChannel().getIdLong() !=
                        channel.getGuild().getSelfMember().getVoiceState().getChannel().getIdLong()) {
            channel.sendMessage("You're not in the same channel as bot").queue();
            return;
        }

        MusicPlayerHandler.getInstance(message.getGuild(), channel).forceSkip();
    }
}
