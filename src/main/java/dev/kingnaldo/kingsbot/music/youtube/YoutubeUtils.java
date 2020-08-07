package dev.kingnaldo.kingsbot.music.youtube;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.config.Config;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YoutubeUtils {

    private final static String KEY = Config.get("YOUTUBE_KEY");
    private final static YouTube YOUTUBE = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), request -> {})
            .setApplicationName("King's BOT")
            .build();

    private static SearchListResponse searchVideosByTerms(String searchTerms, Long maxResults) {
        try {
            YouTube.Search.List search = YOUTUBE.search().list(List.of("id", "snippet"));
            search.setKey(YoutubeUtils.KEY);
            search.setQ(searchTerms);
            search.setType(List.of("video"));
            search.setFields("items(id/videoId,snippet/title)");
            search.setMaxResults(maxResults);

            return search.execute();
        }catch(IOException e) {
            KingsBot.LOGGER.error(e.getMessage());
            return null;
        }
    }

    public static String getFirstResultId(String searchTerms) {
        SearchListResponse list = YoutubeUtils.searchVideosByTerms(searchTerms, 1L);
        if(list == null || list.isEmpty()) return null;
        return list.getItems().get(0).getId().getVideoId();
    }

    public static Map<String, String> getListResults(String searchTerms) {
        Map<String, String> results = new HashMap<>();
        SearchListResponse list = YoutubeUtils.searchVideosByTerms(searchTerms, 10L);
        if(list == null || list.isEmpty()) return null;
        list.getItems().forEach(searchResult -> results.put(
                searchResult.getId().getVideoId(),
                searchResult.getSnippet().getTitle()));
        return results;
    }
}
