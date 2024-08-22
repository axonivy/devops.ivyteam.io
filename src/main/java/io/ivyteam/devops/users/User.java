package io.ivyteam.devops.users;

public record User(String name) {

  public String link() {
    return "/users/" + name;
  }
}
