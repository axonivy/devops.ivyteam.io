package io.ivyteam.devops.pullrequest;

public record PullRequest(
    String repository,
    long id,
    String title,
    String user,
    String branchName) {

  public String repoLink() {
    return "/repository/" + repository;
  }

  public String ghLink() {
    return "https://github.com/" + repository + "/pull/" + id;
  }
}
