package dev.kingnaldo.kingsbot.commands.music;

import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.commands.Command;
import dev.kingnaldo.kingsbot.commands.CommandCategory;
import dev.kingnaldo.kingsbot.music.LyricsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.explodingbush.ksoftapi.entities.Lyric;

import java.util.Arrays;
import java.util.List;

public class LyricsCommand implements Command {

    @Override
    public String name() {
        return "lyrics";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public CommandCategory category() {
        return CommandCategory.MUSIC;
    }

    @Override
    public String usage() {
        return "Use " + KingsBot.getConfig().prefix() + "lyrics (music-name) to search a music lyrics.";
    }

    @Override
    public void execute(TextChannel channel, Member author, Message message, List<String> args) {
        if(args.isEmpty()) {
            channel.sendMessage("Incorrect usage.").queue();
            return;
        }

        Lyric lyric = LyricsManager.getLyric(String.join(" ", args));

        if(lyric == null) {
            channel.sendMessage("No matches found.").queue();
        }else{
            String lyricString = lyric.getLyrics();
            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle(lyric.getTitle())
                    .setDescription(lyric.getArtistName())
                    .setFooter("Lyrics provided by KSoft.Si");

            List<String> fields = Arrays.asList(lyricString.split("[\n]{2}"));
            fields.forEach(field -> builder.addField("", field, false));

            channel.sendMessage(builder.build()).queue();
        }
    }
}
