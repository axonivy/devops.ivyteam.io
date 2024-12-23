package io.ivyteam.devops.pullrequest;

public record PullRequest(
    String repository,
    long id,
    String title,
    String user,
    String branchName) {

  public static Builder create() {
    return new Builder();
  }

  public String repoLink() {
    return "/repository/" + repository;
  }

  public String ghLink() {
    return "https://github.com/" + repository + "/pull/" + id;
  }

  public static class Builder {

    private String repository;
    private long id;
    private String title;
    private String user;
    private String branchName;

    public Builder repository(String repository) {
      this.repository = repository;
      return this;
    }

    public Builder id(long id) {
      this.id = id;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder user(String user) {
      this.user = user;
      return this;
    }

    public Builder branchName(String branchName) {
      this.branchName = branchName;
      return this;
    }

    public PullRequest build() {
      return new PullRequest(repository, id, title, user, branchName);
    }
  }
}
