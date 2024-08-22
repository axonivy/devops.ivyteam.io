package io.ivyteam.devops.user;

public record User(String name) {

  public String link() {
    return "/users/" + name;
  }
}
