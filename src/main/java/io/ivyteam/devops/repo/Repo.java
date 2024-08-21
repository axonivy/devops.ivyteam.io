package io.ivyteam.devops.repo;

import java.util.List;

public record Repo(
    String name,
    boolean archived,
    boolean privateRepo,
    int openPullRequests,
    String license,
    String securityMd,
    String codeOfConduct,
    String settingsLog,
    List<PullRequest> prs,
    List<Branch> branches) {

  public String link() {
    return "/repository/" + name;
  }
}
