package io.ivyteam.devops.dependabot;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DependabotApiHelper {

  private final static ObjectMapper MAPPER = new ObjectMapper();
  private final static Logger LOGGER = LoggerFactory.getLogger(DependabotApiHelper.class);
  private final static String DEPENDABOT_ALERTS_PATH = "/dependabot/alerts";
  private final static String VULNAERABILITY_ALERTS_PATH = "vulnerability-alerts";
  private final static String ALERT_REQUIRED_STATE = "open";
  private final static String LEVEL_CRITICAL = "critical";
  private final static String LEVEL_HIGH = "high";
  private final static String LEVEL_MEDIUM = "medium";
  private final static String LEVEL_LOW = "low";

  private DependabotRepository dependabots;
  private GHRepository repo;
  private String token;

  public DependabotApiHelper(DependabotRepository dependabots, GHRepository repo, String token) {
    this.dependabots = dependabots;
    this.repo = repo;
    this.token = token;
  }

  public static void enableAlerts(URL url, String token) {
    var apiUrl = toUri(url, VULNAERABILITY_ALERTS_PATH);
    try (var client = HttpClient.newHttpClient()) {
      var request = HttpRequest.newBuilder()
          .uri(apiUrl)
          .header("Accept", "application/vnd.github+json")
          .header("Authorization", "Bearer " + token)
          .PUT(HttpRequest.BodyPublishers.noBody())
          .build();

      var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
        LOGGER.info("dependabot alerts is enabled, api/url: " + apiUrl);
      } else {
        LOGGER.warn("enable dependabot alerts is failed: " + response.statusCode() + ", api/url: " + apiUrl);
      }
    } catch (Exception ex) {
      LOGGER.warn("Could not enable dependabot alerts", ex);
    }
  }

  private static String getAlerts(URL url, String token) {
    var apiUrl = toUri(url, DEPENDABOT_ALERTS_PATH + "?per_page=100");
    try (var client = HttpClient.newHttpClient()) {
      var request = HttpRequest.newBuilder()
          .uri(apiUrl)
          .header("Accept", "application/vnd.github+json")
          .header("Authorization", "Bearer " + token)
          .GET()
          .build();

      var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == HttpURLConnection.HTTP_OK) {
        return response.body();
      } else {
        LOGGER.warn("get dependabots-alerts is failed: " + response.statusCode() + ", api/url: " + apiUrl);
      }
    } catch (Exception ex) {
      LOGGER.warn("Could not get dependabot alerts", ex);
    }
    return null;
  }

  private static Dependabot parseAlertsResponse(String json, String repoName) {
    try {
      JsonNode root = MAPPER.readTree(json);
      Map<String, Long> severityCounts = new HashMap<>();

      for (JsonNode node : root) {
        if (node.path("state").asText().equals(ALERT_REQUIRED_STATE)) {
          severityCounts.merge(node.path("security_vulnerability").path("severity").asText(), 1L, Long::sum);
        }
      }
      int low = Math.toIntExact(severityCounts.getOrDefault(LEVEL_LOW, 0L));
      int medium = Math.toIntExact(severityCounts.getOrDefault(LEVEL_MEDIUM, 0L));
      int high = Math.toIntExact(severityCounts.getOrDefault(LEVEL_HIGH, 0L));
      int critical = Math.toIntExact(severityCounts.getOrDefault(LEVEL_CRITICAL, 0L));
      return new Dependabot(repoName, critical, high, medium, low);
    } catch (Exception ex) {
      LOGGER.warn("Could not read jsonFile", ex);
    }
    return null;
  }

  public void synch() throws IOException {
    var json = DependabotApiHelper.getAlerts(repo.getUrl(), token);
    if (json == null) {
      return;
    }
    var alerts = DependabotApiHelper.parseAlertsResponse(json, repo.getName());
    if (alerts == null) {
      return;
    }
    var name = repo.getFullName();
    int critical = alerts.critical();
    int high = alerts.high();
    int medium = alerts.medium();
    int low = alerts.low();

    var d = new Dependabot(name, critical, high, medium, low);
    dependabots.create(d);
  }

  private static URI toUri(URL url, String path) {
    if (url == null || path == null) {
      throw new RuntimeException("URL or Path cannot be null");
    }
    try {
      return URI.create(url.toString() + path);
    } catch (IllegalArgumentException ex) {
      throw new RuntimeException("Failed to build URI for dependabot alerts: " + url, ex);
    }
  }
}
