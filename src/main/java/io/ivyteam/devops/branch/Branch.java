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

  public static Builder create() {
    return new Builder();
  }

  public static class Builder {

    private String repository;
    private String name;
    private String lastCommitAuthor;
    private boolean protectedBranch;
    private Date authoredDate;

    public Builder repository(String repository) {
      this.repository = repository;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder lastCommitAuthor(String lastCommitAuthor) {
      this.lastCommitAuthor = lastCommitAuthor;
      return this;
    }

    public Builder protectedBranch(boolean protectedBranch) {
      this.protectedBranch = protectedBranch;
      return this;
    }

    public Builder authoredDate(Date authoredDate) {
      this.authoredDate = authoredDate;
      return this;
    }

    public Branch build() {
      return new Branch(repository, name, lastCommitAuthor, protectedBranch, authoredDate);
    }
  }
}
