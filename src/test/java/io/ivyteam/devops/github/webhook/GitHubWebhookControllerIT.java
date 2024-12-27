package io.ivyteam.devops.github.webhook;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.jupiter.api.Test;

class GitHubWebhookControllerIT {

  @Test
  void get() throws Exception {
    try (var client = HttpClient.newHttpClient()) {
      var request = HttpRequest.newBuilder()
          .uri(URI.create("http://localhost:8080/github-webhook/"))
          .build();
      var response = client.send(request, BodyHandlers.ofString());
      assertThat(response.statusCode()).isEqualTo(200);
      assertThat(response.body()).isEqualTo("OK");
    }
  }

  @Test
  void push_create() throws Exception {
    var push = """
          {
            "ref": "refs/heads/market-install-result",
            "repository": {
              "full_name": "axonivy/core"
            },
            "deleted": false,
            "head_commit": {
              "timestamp": "2024-12-27T09:44:59+01:00",
              "author": {
                "username": "ivy-lmu"
              }
            }
          }
        """;

    try (var client = HttpClient.newHttpClient()) {
      var request = HttpRequest.newBuilder()
          .uri(URI.create("http://localhost:8080/github-webhook/"))
          .header("X-GitHub-Event", "push")
          .header("Content-Type", "application/json")
          .POST(BodyPublishers.ofString(push))
          .build();
      var response = client.send(request, BodyHandlers.ofString());
      response.headers().map().forEach((k, v) -> System.out.println(k + ": " + v));
      // assertThat(response.statusCode()).isEqualTo(200);
      assertThat(response.body()).isEqualTo("CREATED");
    }
  }

  @Test
  void push_delete() throws Exception {
    var push = """
          {
            "ref": "refs/heads/market-install-result",
            "repository": {
              "full_name": "axonivy/core"
            },
            "deleted": true,
            "head_commit": {
              "timestamp": "2024-12-27T09:44:59+01:00",
              "author": {
                "username": "ivy-lmu"
              }
            }
          }
        """;

    try (var client = HttpClient.newHttpClient()) {
      var request = HttpRequest.newBuilder()
          .uri(URI.create("http://localhost:8080/github-webhook/"))
          .header("X-GitHub-Event", "push")
          .header("Content-Type", "application/json")
          .POST(BodyPublishers.ofString(push))
          .build();
      var response = client.send(request, BodyHandlers.ofString());
      assertThat(response.statusCode()).isEqualTo(200);
      assertThat(response.body()).isEqualTo("DELETED");
    }
  }

  @Test
  void delete() throws Exception {
    var push = """
          {
            "ref": "market-install-result",
            "repository": {
              "full_name": "axonivy/core"
            },
            "ref_type": "branch"
          }
        """;

    try (var client = HttpClient.newHttpClient()) {
      var request = HttpRequest.newBuilder()
          .uri(URI.create("http://localhost:8080/github-webhook/"))
          .header("X-GitHub-Event", "delete")
          .header("Content-Type", "application/json")
          .POST(BodyPublishers.ofString(push))
          .build();
      var response = client.send(request, BodyHandlers.ofString());
      assertThat(response.statusCode()).isEqualTo(200);
      assertThat(response.body()).isEqualTo("DELETED");
    }
  }
}
