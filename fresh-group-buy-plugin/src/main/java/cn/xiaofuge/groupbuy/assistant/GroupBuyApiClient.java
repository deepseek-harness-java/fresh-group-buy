package cn.xiaofuge.groupbuy.assistant;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

final class GroupBuyApiClient {
    private static final int CONNECT_TIMEOUT_SECONDS = 5;
    private static final int REQUEST_TIMEOUT_SECONDS = 15;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
            .build();
    private volatile String baseUrl;

    GroupBuyApiClient(String baseUrl) {
        this.baseUrl = normalize(baseUrl);
    }

    void setBaseUrl(String value) {
        this.baseUrl = normalize(value);
    }

    String get(String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("HTTP " + response.statusCode() + ": " + response.body());
            }
            return response.body();
        } catch (Exception exception) {
            throw new IllegalStateException("APP_API_ERROR: " + exception.getMessage(), exception);
        }
    }

    private String normalize(String value) {
        String result = value == null || value.isBlank() ? "http://127.0.0.1:18082" : value.trim();
        return result.endsWith("/") ? result.substring(0, result.length() - 1) : result;
    }
}
