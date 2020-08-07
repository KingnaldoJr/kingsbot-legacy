package dev.kingnaldo.kingsbot.commands.music;

import dev.kingnaldo.kingsbot.commands.Command;
import dev.kingnaldo.kingsbot.config.Config;
import dev.kingnaldo.kingsbot.music.MusicPlayerHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.List;

public class LeaveCommand implements Command {
    @Override
    public String name() {
        return "leave";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String usage() {
        return "Use " + Config.get("PREFIX") + "leave to make the bot disconnect from voice channel.";
    }

    @Override
    public void execute(TextChannel channel, Member author, Message message, List<String> args) {
        AudioManager manager = channel.getGuild().getAudioManager();

        if(!manager.isConnected()) {
            channel.sendMessage("Not connected to a voice channel.").queue();
            return;
        }

        VoiceChannel voiceChannel = manager.getConnectedChannel();
        assert voiceChannel != null;

        if(!voiceChannel.getMembers().contains(author)) {
            channel.sendMessage("We are not connected to the same voice channel.").queue();
            return;
        }

        manager.closeAudioConnection();
        MusicPlayerHandler.getInstance(message.getGuild(), channel).stopQueue();
    }
}
