package io.ivyteam.devops.repo.check;

import java.util.List;
import java.util.Objects;

import io.ivyteam.devops.db.Database;
import io.ivyteam.devops.file.FileRepository;
import io.ivyteam.devops.repo.Repo;

public interface RepoCheck {

  String name();

  String check(Repo repo);

  public static List<RepoCheck> all() {
    return all(new Database());
  }

  public static List<RepoCheck> all(Database db) {
    var files = new FileRepository(db);
    return List.of(
        new RenovateRepoCheck(files), new LicenseRepoCheck(files));
  }

  public static List<String> run(Repo repo) {
    return all().stream()
        .map(check -> check.check(repo))
        .filter(Objects::nonNull)
        .toList();
  }
}
