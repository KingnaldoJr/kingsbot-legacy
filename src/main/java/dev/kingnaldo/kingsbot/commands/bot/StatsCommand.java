package dev.kingnaldo.kingsbot.commands.bot;

import com.sun.management.OperatingSystemMXBean;
import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.commands.Command;
import dev.kingnaldo.kingsbot.commands.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.lang.management.ManagementFactory;
import java.util.List;

public class StatsCommand implements Command {

    @Override
    public String name() {
        return "stats";
    }

    @Override
    public String[] aliases() {
        return new String[]{ "stt" };
    }

    @Override
    public CommandCategory category() {
        return CommandCategory.BOT;
    }

    @Override
    public String usage() {
        return "Use " + KingsBot.getConfig().prefix() + "stats to see King's BOT stats infos.";
    }

    @Override
    public void execute(TextChannel channel, Member author, Message message, List<String> args) {
        if(!author.getId().equals(KingsBot.getConfig().ownerId())) return;
        EmbedBuilder builder = new EmbedBuilder();
        builder.addField("Guilds", String.valueOf(KingsBot.getBOT().getGuilds().size()), true);
        builder.addField("Users", String.valueOf(KingsBot.getBOT().getUsers().size()), true);
        builder.addField("Latency", KingsBot.getBOT().getRestPing().complete() + " ms", true);
        builder.addField("CPU",Math.round(
                ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class)
                        .getCpuLoad() * 100) + "%", true);
        builder.addField("Threads", String.valueOf(Thread.getAllStackTraces().size()), true);
        Long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        builder.addField("Memory",
                Math.floorDiv(memory, 1073741824) + "." + Math.floorDiv(memory % 1073741824, 1048576) + "G",
                true);

        channel.sendMessage(builder.build()).queue();
    }
}
