package dev.kingnaldo.kingsbot.music.youtube;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import dev.kingnaldo.kingsbot.KingsBot;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class YoutubeUtils {

    private final static String KEY = KingsBot.getProperties().getProperty("youtube.key");
    private final static YouTube YOUTUBE = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), request -> {})
            .setApplicationName("King's BOT")
            .build();

    private static SearchListResponse searchVideosByTerms(String searchTerms, Long maxResults) {
        try {
            YouTube youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), request -> {})
                    .setApplicationName("King's BOT")
                    .build();

            YouTube.Search.List search = YOUTUBE.search().list(List.of("id"));
            search.setKey(YoutubeUtils.KEY);
            search.setQ(searchTerms);
            search.setType(List.of("video"));
            search.setFields("items(id/videoId)");
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

    public static List<String> getListResultIds(String searchTerms) {
        SearchListResponse list = YoutubeUtils.searchVideosByTerms(searchTerms, 10L);
        if(list == null || list.isEmpty()) return null;
        return list.getItems().stream().map(result -> result.getId().getVideoId()).collect(Collectors.toList());
    }
}
