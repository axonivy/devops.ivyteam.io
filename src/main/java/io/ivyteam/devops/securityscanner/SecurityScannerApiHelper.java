package io.ivyteam.devops.securityscanner;

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

public class SecurityScannerApiHelper {

  private final static ObjectMapper MAPPER = new ObjectMapper();
  private final static Logger LOGGER = LoggerFactory.getLogger(SecurityScannerApiHelper.class);
  private final static String VULNAERABILITY_ALERTS_PATH = "vulnerability-alerts";
  private final static String ALERT_REQUIRED_STATE = "open";
  private final static String LEVEL_CRITICAL = "critical";
  private final static String LEVEL_HIGH = "high";
  private final static String LEVEL_MEDIUM = "medium";
  private final static String LEVEL_LOW = "low";

  private SecurityScannerRepository securityScanners;
  private GHRepository repo;
  private String token;

  public SecurityScannerApiHelper(SecurityScannerRepository securityScanners, GHRepository repo, String token) {
    this.securityScanners = securityScanners;
    this.repo = repo;
    this.token = token;
  }

  public static void enableAlerts(URL url, String token, String scantype) {
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

  private static String getAlerts(URL url, String token, String scantype) {
    var apiUrl = toUri(url, "/" + scantype + "/alerts?per_page=100");
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
      }
      if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
        return null;
      } else {
        LOGGER.warn("get " + scantype + " is failed: " + response.statusCode() + ", api/url: " + apiUrl);
      }
    } catch (Exception ex) {
      LOGGER.warn("Could not get " + scantype + " alerts", ex);
    }
    return null;
  }

  private static SecurityScanner parseAlerts(String json, String repoName, String scantype) {
    try {
      JsonNode root = MAPPER.readTree(json);
      Map<String, Long> severityCounts = new HashMap<>();

      if (scantype.equals(ScanTypeEnum.DEPENDABOT.getValue())) {
        for (JsonNode node : root) {
          if (node.path("state").asText().equals(ALERT_REQUIRED_STATE)) {
            severityCounts.merge(node.path("security_vulnerability").path("severity").asText(), 1L, Long::sum);
          }
        }
      } else if (scantype.equals(ScanTypeEnum.CODE_SCANNING.getValue())) {
        for (JsonNode node : root) {
          if (node.path("state").asText().equals(ALERT_REQUIRED_STATE)) {
            severityCounts.merge(node.path("rule").path("security_severity_level").asText(), 1L, Long::sum);
          }
        }
      } else if (scantype.equals(ScanTypeEnum.SECRET_SCANNING.getValue())) {
        for (JsonNode node : root) {
          if (node.path("state").asText().equals(ALERT_REQUIRED_STATE)) {
            severityCounts.merge((node.path("url").asText() != null ? "high" : null), 1L, Long::sum);
          }
        }
      }

      int low = Math.toIntExact(severityCounts.getOrDefault(LEVEL_LOW, 0L));
      int medium = Math.toIntExact(severityCounts.getOrDefault(LEVEL_MEDIUM, 0L));
      int high = Math.toIntExact(severityCounts.getOrDefault(LEVEL_HIGH, 0L));
      int critical = Math.toIntExact(severityCounts.getOrDefault(LEVEL_CRITICAL, 0L));
      return new SecurityScanner(repoName, scantype, critical, high, medium, low);
    } catch (Exception ex) {
      LOGGER.warn("Could not read jsonFile", ex);
    }
    return null;
  }

  public void synch(String scantype) throws IOException {
    var json = SecurityScannerApiHelper.getAlerts(repo.getUrl(), token, scantype);
    if (json == null) {
      return;
    }
    var alerts = SecurityScannerApiHelper.parseAlerts(json, repo.getName(), scantype);
    if (alerts == null) {
      return;
    }
    SecurityScanner ss = new SecurityScanner(repo.getFullName(), alerts.scantype(), alerts.critical(), alerts.high(),
        alerts.medium(), alerts.low());
    securityScanners.create(ss);
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
