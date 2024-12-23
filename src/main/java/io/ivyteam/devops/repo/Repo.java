package io.ivyteam.devops.repo;

public record Repo(
    String name,
    boolean archived,
    boolean privateRepo,
    boolean deleteBranchOnMerge,
    boolean projects,
    boolean issues,
    boolean wiki,
    boolean hooks,
    boolean fork,
    boolean isVulnAlertOn,
    String license,
    String securityMd,
    String codeOfConduct) {

  public String link() {
    return "/repository/" + name;
  }

  public static Builder create() {
    return new Builder();
  }

  public static class Builder {

    private String name;
    private boolean archived;
    private boolean privateRepo;
    private boolean deleteBranchOnMerge;
    private boolean projects;
    private boolean issues;
    private boolean wiki;
    private boolean hooks;
    private boolean fork;
    private boolean isVulnAlertOn;
    private String license;
    private String securityMd;
    private String codeOfConduct;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder archived(boolean archived) {
      this.archived = archived;
      return this;
    }

    public Builder privateRepo(boolean privateRepo) {
      this.privateRepo = privateRepo;
      return this;
    }

    public Builder deleteBranchOnMerge(boolean deleteBranchOnMerge) {
      this.deleteBranchOnMerge = deleteBranchOnMerge;
      return this;
    }

    public Builder projects(boolean projects) {
      this.projects = projects;
      return this;
    }

    public Builder issues(boolean issues) {
      this.issues = issues;
      return this;
    }

    public Builder wiki(boolean wiki) {
      this.wiki = wiki;
      return this;
    }

    public Builder hooks(boolean hooks) {
      this.hooks = hooks;
      return this;
    }

    public Builder fork(boolean fork) {
      this.fork = fork;
      return this;
    }

    public Builder isVulnAlertOn(boolean isVulnAlertOn) {
      this.isVulnAlertOn = isVulnAlertOn;
      return this;
    }

    public Builder license(String license) {
      this.license = license;
      return this;
    }

    public Builder securityMd(String securityMd) {
      this.securityMd = securityMd;
      return this;
    }

    public Builder codeOfConduct(String codeOfConduct) {
      this.codeOfConduct = codeOfConduct;
      return this;
    }

    public Repo build() {
      return new Repo(name, archived, privateRepo, deleteBranchOnMerge, projects, issues, wiki, hooks, fork,
          isVulnAlertOn, license, securityMd, codeOfConduct);
    }
  }
}
