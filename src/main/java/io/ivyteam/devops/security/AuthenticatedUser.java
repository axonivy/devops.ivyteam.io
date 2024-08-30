package io.ivyteam.devops.security;

public record AuthenticatedUser(String username, String email, String avatarUrl, String profileUrl) {

}
