package io.ivyteam.devops.branch;

import java.util.Date;

public record Branch(
    String repository,
    String name,
    String lastCommitAuthor,
    boolean protectedBranch,
    Date authoredDate) {

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
