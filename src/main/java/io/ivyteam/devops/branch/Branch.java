package io.ivyteam.devops.branch;

public record Branch(
    String repository,
    String name,
    String lastCommitAuthor) {

  public String repoLink() {
    return "/repository/" + repository;
  }

  public String ghRepoLink() {
    return "https://github.com/" + repository;
  }

  public String ghLink() {
    return ghRepoLink() + "/tree/" + name;
  }
}
