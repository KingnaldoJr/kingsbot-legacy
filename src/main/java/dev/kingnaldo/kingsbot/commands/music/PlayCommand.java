package dev.kingnaldo.kingsbot.commands.music;

import dev.kingnaldo.kingsbot.commands.Command;
import dev.kingnaldo.kingsbot.config.Config;
import dev.kingnaldo.kingsbot.music.MusicPlayerHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.List;

public class PlayCommand implements Command {

    @Override
    public String name() {
        return "play";
    }

    @Override
    public String[] aliases() {
        return new String[]{ "p" };
    }

    @Override
    public String usage() {
        return "Use " + Config.get("PREFIX") + "play <music-link> to add the music to queue.";
    }

    @Override
    public void execute(TextChannel channel, Member author, Message message, List<String> args) {
        if(!channel.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
            AudioManager manager = channel.getGuild().getAudioManager();

            if(manager.isConnected()) {
                channel.sendMessage("Already connected in a channel.").queue();
                return;
            }

            GuildVoiceState state = author.getVoiceState();

            if(!state.inVoiceChannel()) {
                channel.sendMessage("You're not connected in a voice channel.").queue();
                return;
            }

            VoiceChannel voiceChannel = state.getChannel();
            Member selfMember = channel.getGuild().getSelfMember();

            if(!selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT)) {
                channel.sendMessage("I don't have permission to join this voice channel.").queue();
            }

            manager.openAudioConnection(voiceChannel);
        }

        if(args.isEmpty()) {
            channel.sendMessage("Incorrect usage.").queue();
            return;
        }

        MusicPlayerHandler.getInstance(message.getGuild(), channel).addToQueue(String.join(" ", args), false);
    }
}
