package io.ivyteam.devops.repo;

public record PullRequest(
    String repository,
    long id,
    String title,
    String user) {

  public String ghLink() {
    return "https://github.com/" + repository + "/pull/" + id;
  }
}
