package dev.kingnaldo.kingsbot.music;

import dev.kingnaldo.kingsbot.KingsBot;
import net.explodingbush.ksoftapi.KSoftAPI;
import net.explodingbush.ksoftapi.entities.Lyric;

import java.util.List;

public class LyricsManager {

    public static Lyric getLyric(String music) {
        KSoftAPI kSoftAPI = new KSoftAPI(KingsBot.getConfig().KSoftToken());
        List<Lyric> lyrics = kSoftAPI.getLyrics()
                .search(music)
                .setLimit(1)
                .execute();

        if(lyrics.size() > 0) return lyrics.get(0);
        else return null;
    }
}
