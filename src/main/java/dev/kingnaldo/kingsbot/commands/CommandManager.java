package dev.kingnaldo.kingsbot.commands;

import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.commands.administration.PurgeCommand;
import dev.kingnaldo.kingsbot.commands.bot.ShutdownCommand;
import dev.kingnaldo.kingsbot.commands.music.*;
import dev.kingnaldo.kingsbot.commands.utils.HelpCommand;
import dev.kingnaldo.kingsbot.commands.utils.PingCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager extends ListenerAdapter {
    private Map<String, Command> commands = new HashMap<>();
    private Map<String, Command> commandsAliases = new HashMap<>();

    public CommandManager() {
        addCommand(new ShutdownCommand());
        addCommand(new PingCommand());
        addCommand(new HelpCommand());
        addCommand(new PurgeCommand());
        addCommand(new JoinCommand());
        addCommand(new LeaveCommand());
        addCommand(new PlayCommand());
        addCommand(new PauseCommand());
        addCommand(new ResumeCommand());
        addCommand(new SkipCommand());
        addCommand(new StopCommand());
        addCommand(new ShuffleCommand());
        addCommand(new UnshuffleCommand());
        addCommand(new LoopCommand());
        addCommand(new LyricsCommand());

        HelpCommand.commands.addAll(this.commands.values());
    }

    public Map<String, Command> getCommands() {
        return commands;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return;

        final String message = event.getMessage().getContentRaw();
        if(!message.startsWith(KingsBot.getConfig().prefix())) return;

        final String[] entry = message.substring(KingsBot.getConfig().prefix().length()).split("\\s+");
        final String command = entry[0].toLowerCase();
        if(this.commands.containsKey(command))
            executeCommand(this.commands.get(command), entry, event);
        else if(this.commandsAliases.containsKey(command))
            executeCommand(this.commandsAliases.get(command), entry, event);
    }

    private void executeCommand(Command command, String[] entry, GuildMessageReceivedEvent event) {
        TextChannel channel = event.getChannel();
        Member author = event.getMember();
        Message msg = event.getMessage();
        List<String> args = Arrays.asList(entry).subList(1, entry.length);
        command.execute(channel, author, msg, args);
    }

    private void addCommand(Command command) {
        this.commands.putIfAbsent(command.name(), command);
        Arrays.stream(command.aliases()).forEach(alias -> this.commandsAliases.putIfAbsent(alias, command));
    }
}
