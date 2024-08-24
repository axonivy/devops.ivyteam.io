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
    String license,
    String securityMd,
    String codeOfConduct) {

  public String link() {
    return "/repository/" + name;
  }
}
