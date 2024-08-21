package io.ivyteam.devops.repo;

public record Branch(
        String repository,
        String name,
        String lastCommitAuthor) {

    public String ghLink() {
        return "https://github.com/" + repository + "/tree/" + name;
    }
}
