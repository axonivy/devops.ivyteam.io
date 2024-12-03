package io.ivyteam.devops.github;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.kohsuke.github.GHRepository;

public class GitHubSettingsHelper extends GHRepository {

    private final static Logger LOGGER = LoggerFactory.getLogger(GitHubSettingsHelper.class);

    public static void enableDependabot(URL url, String token) {
        String apiUrl = url.toString() + "/vulnerability-alerts";

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/vnd.github+json")
                    .header("Authorization", "Bearer s" + token)
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                LOGGER.info("dependabot alerts is enabled, "
                        + ", api/url: " + apiUrl);
            } else {
                LOGGER.warn("enable dependabot alerts is failed: "
                        + response.statusCode()
                        + ", api/url: " + apiUrl);
            }
        } catch (Exception ex) {
            LOGGER.warn("Could not enable dependabot alerts", ex);
        }
    }

}
