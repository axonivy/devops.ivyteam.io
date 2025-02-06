package io.ivyteam.devops.repo.check;

import io.ivyteam.devops.file.FileRepository;
import io.ivyteam.devops.repo.Repo;

class LicenseRepoCheck implements RepoCheck {

  private final FileRepository files;

  public LicenseRepoCheck(FileRepository files) {
    this.files = files;
  }

  @Override
  public String name() {
    return "Public repository needs a LICENSE";
  }

  @Override
  public String check(Repo repo) {
    if (repo.archived()) {
      return null;
    }
    if (repo.privateRepo()) {
      return null;
    }
    var file = files.byPath(repo, "LICENSE");
    if (file == null) {
      return "LICENSE file is missing";
    }
    var content = file.content();
    if (content == null || content.isEmpty()) {
      return "Content of LICENSE file is empty";
    }
    return null;
  }
}
