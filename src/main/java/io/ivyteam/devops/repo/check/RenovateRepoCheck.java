package io.ivyteam.devops.repo.check;

import io.ivyteam.devops.file.FileRepository;
import io.ivyteam.devops.repo.Repo;

class RenovateRepoCheck implements RepoCheck {

  private static final String SHARED = "local>axonivy/renovate-config";

  private final FileRepository files;

  public RenovateRepoCheck(FileRepository files) {
    this.files = files;
  }

  @Override
  public String name() {
    return "renovate.json exists and uses shared config";
  }

  @Override
  public String check(Repo repo) {
    if (repo.archived()) {
      return null;
    }
    var renovateFile = files.byPath(repo, "renovate.json");
    if (renovateFile == null) {
      renovateFile = files.byPath(repo, ".github/renovate.json");
    }
    if (renovateFile == null) {
      return "renovate.json and .github/renovate.json is missing";
    }
    var content = renovateFile.content();
    if (content == null || content.isEmpty()) {
      return renovateFile.path() + " is empty";
    }
    if (!content.contains(SHARED)) {
      return renovateFile + " does not use shared config '" + SHARED + "'";
    }
    return null;
  }
}
