package http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpFetcher {

    public String get(String url) throws Exception {
        URL requestUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();

        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);

        int statusCode = conn.getResponseCode();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        statusCode == 200
                                ? conn.getInputStream()
                                : conn.getErrorStream(),
                        "UTF-8"
                ))) {

            String response = readAllLines(reader);

            if (statusCode != 200) {
                throw new RuntimeException(
                        "HTTP " + statusCode + ": " + response
                );
            }

            return response;
        } finally {
            conn.disconnect();
        }
    }

    private String readAllLines(BufferedReader reader) throws Exception {
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }
}
