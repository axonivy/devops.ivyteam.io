package io.ivyteam.devops.repo.check;

import io.ivyteam.devops.file.File;
import io.ivyteam.devops.file.FileRepository;
import io.ivyteam.devops.repo.Repo;

class RenovateRepoCheck implements RepoCheck {

  private static final String SHARED = "local>axonivy/renovate-config";

  private final FileRepository files;

  public RenovateRepoCheck(FileRepository files) {
    this.files = files;
  }

  @Override
  public Result check(Repo repo) {
    if (repo.archived()) {
      return Result.success("Archived repository do not need renovate");
    }
    var renovateFile = files.byPath(repo, File.RENOVATE_JSON);
    if (renovateFile == null) {
      renovateFile = files.byPath(repo, File.GITHUB_RENOVATE_JSON);
    }
    if (renovateFile == null) {
      return Result.failed(File.RENOVATE_JSON + " and " + File.GITHUB_RENOVATE_JSON + " is missing");
    }
    var content = renovateFile.content();
    if (content == null || content.isEmpty()) {
      return Result.failed(renovateFile.path() + " is empty");
    }
    if (!content.contains(SHARED)) {
      return Result.failed(renovateFile.path() + " does not use shared config '" + SHARED + "'");
    }
    return Result.success(renovateFile.path() + " exists and uses shared config");
  }
}
