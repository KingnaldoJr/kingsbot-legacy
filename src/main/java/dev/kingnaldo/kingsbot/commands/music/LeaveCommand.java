package dev.kingnaldo.kingsbot.commands.music;

import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.commands.Command;
import dev.kingnaldo.kingsbot.commands.CommandCategory;
import dev.kingnaldo.kingsbot.music.MusicPlayerHandler;
import net.dv8tion.jda.api.entities.*;

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
    public CommandCategory category() { return CommandCategory.MUSIC; }

    @Override
    public String usage() {
        return "Use " + KingsBot.getConfig().prefix() + "leave to make the bot disconnect from voice channel.";
    }

    @Override
    public void execute(TextChannel channel, Member author, Message message, List<String> args) {
        MusicPlayerHandler playerHandler = MusicPlayerHandler.getInstance(channel.getGuild(), channel);
        if(!playerHandler.isConnected()) {
            channel.sendMessage("Not connected to a channel.").queue();
            return;
        }

        GuildVoiceState state = author.getVoiceState();
        if(!state.inVoiceChannel()) {
            channel.sendMessage("You're not connected in a voice channel.").queue();
            return;
        }

        playerHandler.stopQueue();
        MusicPlayerHandler.removeGuild(channel.getGuild());
    }
}
