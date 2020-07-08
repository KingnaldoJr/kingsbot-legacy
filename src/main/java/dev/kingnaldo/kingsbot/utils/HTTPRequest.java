package dev.kingnaldo.kingsbot.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HTTPRequest {

    public static InputStream POST(String url, List<String> bodyParameters,
                                   Map<String, String> headers) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");

        headers.forEach(connection::setRequestProperty);

        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        for(String parameter : bodyParameters)
            writer.write(parameter);
        writer.flush();
        writer.close();

        return connection.getInputStream();
    }

    public static InputStream GET(String url, Map<String, String> headers) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");

        headers.forEach(connection::setRequestProperty);

        return connection.getInputStream();
    }
}
