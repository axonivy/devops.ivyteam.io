package io.ivyteam.devops.repo;

public record Repo(
    String name,
    boolean archived,
    boolean privateRepo,
    String license,
    String securityMd,
    String codeOfConduct,
    String settingsLog) {

  public String link() {
    return "/repository/" + name;
  }
}
