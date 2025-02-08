package io.ivyteam.devops.repo.check;

import io.ivyteam.devops.file.FileRepository;
import io.ivyteam.devops.repo.Repo;

class FileExistsRepoCheck implements RepoCheck {

  private final FileRepository files;
  private final String fileName;

  public FileExistsRepoCheck(FileRepository files, String fileName) {
    this.files = files;
    this.fileName = fileName;
  }

  @Override
  public Result check(Repo repo) {
    if (repo.archived()) {
      return Result.success("Archived repository do not need a " + fileName);
    }
    if (repo.privateRepo()) {
      return Result.success("Private repository do not need a " + fileName);
    }
    var file = files.byPath(repo, fileName);
    if (file == null) {
      return Result.failed(fileName + " file is missing");
    }
    var content = file.content();
    if (content == null || content.isEmpty()) {
      return Result.failed("Content of " + fileName + " file is empty");
    }
    return Result.success("Reposistory contains " + fileName);
  }
}
