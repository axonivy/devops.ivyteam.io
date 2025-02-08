package io.ivyteam.devops.repo.check;

import java.util.List;
import java.util.Objects;

import io.ivyteam.devops.db.Database;
import io.ivyteam.devops.file.File;
import io.ivyteam.devops.file.FileRepository;
import io.ivyteam.devops.repo.Repo;

public interface RepoCheck {

  Result check(Repo repo);

  public static List<RepoCheck> all() {
    return all(new Database());
  }

  public static List<RepoCheck> all(Database db) {
    var files = new FileRepository(db);
    return List.of(
        new RenovateRepoCheck(files),
        new FileExistsRepoCheck(files, File.LICENSE),
        new FileExistsRepoCheck(files, File.CODE_OF_CONDUCT_MD),
        new FileExistsRepoCheck(files, File.SECURITY_MD));
  }

  public static List<Result> run(Repo repo) {
    return all().stream()
        .map(check -> check.check(repo))
        .filter(Objects::nonNull)
        .toList();
  }

  public record Result(boolean success, String message) {

    public static Result success(String message) {
      return new Result(true, message);
    }

    public static Result failed(String message) {
      return new Result(false, message);
    }
  }
}
