package dev.kingnaldo.kingsbot.commands.music;

import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.commands.Command;
import dev.kingnaldo.kingsbot.commands.CommandCategory;
import dev.kingnaldo.kingsbot.music.MusicPlayerHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class JoinCommand implements Command {

    @Override
    public String name() {
        return "join";
    }

    @Override
    public String[] aliases() {
        return new String[]{ "j" };
    }

    @Override
    public CommandCategory category() { return CommandCategory.MUSIC; }

    @Override
    public String usage() {
        return "Use " + KingsBot.getConfig().prefix() + "join to make the bot join in your voice channel.";
    }

    @Override
    public void execute(TextChannel channel, Member author, Message message, List<String> args) {
        MusicPlayerHandler playerHandler = MusicPlayerHandler.getInstance(channel.getGuild(), channel);
        if(playerHandler.isConnected()) {
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

        playerHandler.connectToVoiceChannel(voiceChannel);
    }
}
