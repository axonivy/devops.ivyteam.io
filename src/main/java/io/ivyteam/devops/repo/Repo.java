package io.ivyteam.devops.repo;

import java.util.List;

public record Repo(
  String name,
  boolean archived,
  int openPullRequests,
  boolean license,
  String settingsLog,
  List<PullRequest> prs) {

  public String link() {
    return "/repository/" + name;
  }
}
