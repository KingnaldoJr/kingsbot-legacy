package dev.kingnaldo.kingsbot.commands.music;

import dev.kingnaldo.kingsbot.commands.Command;
import dev.kingnaldo.kingsbot.config.Config;
import dev.kingnaldo.kingsbot.music.MusicPlayerHandler;
import dev.kingnaldo.kingsbot.music.RepeatMode;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class LoopCommand implements Command {
    @Override
    public String name() {
        return "loop";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String usage() {
        return "Use " + Config.get("PREFIX") + "loop to change the loop of the queue.";
    }

    @Override
    public void execute(TextChannel channel, Member author, Message message, List<String> args) {
        if(!channel.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
            channel.sendMessage("Not connected on voice channel!").queue();
            return;
        }

        if(author.getVoiceState().inVoiceChannel() &&
                author.getVoiceState().getChannel().getIdLong() !=
                        channel.getGuild().getSelfMember().getVoiceState().getChannel().getIdLong()) {
            channel.sendMessage("You're not in the same channel as bot").queue();
            return;
        }

        MusicPlayerHandler playerHandler = MusicPlayerHandler.getInstance(channel.getGuild(), channel);
        switch(playerHandler.getRepeatMode()) {
            case NONE -> {
                playerHandler.setRepeatMode(RepeatMode.QUEUE);
                channel.sendMessage("Now looping queue.").queue();
            }
            case QUEUE -> {
                playerHandler.setRepeatMode(RepeatMode.TRACK);
                channel.sendMessage("Now looping track.").queue();
            }
            case TRACK -> {
                playerHandler.setRepeatMode(RepeatMode.NONE);
                channel.sendMessage("Looping stopped.").queue();
            }
        }
    }
}
