package io.ivyteam.devops.file;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.ivyteam.devops.db.Database;
import io.ivyteam.devops.repo.Repo;

@Repository
public class FileRepository {

  @Autowired
  private Database db;

  public FileRepository(Database db) {
    this.db = db;
  }

  public List<File> all(Repo repo) {
    try (var connection = db.connection()) {
      try (
          var stmt = connection.prepareStatement("SELECT * FROM file WHERE repository = ? ORDER BY repository, path")) {
        stmt.setString(1, repo.name());
        try (var result = stmt.executeQuery()) {
          var files = new ArrayList<File>();
          while (result.next()) {
            var file = toFile(result);
            files.add(file);
          }
          return files;
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public File byPath(Repo repo, String path) {
    return all(repo).stream()
        .filter(file -> file.path().equals(path))
        .findAny()
        .orElse(null);
  }

  private File toFile(ResultSet result) throws SQLException {
    return File.create()
        .repository(result.getString("repository"))
        .path(result.getString("path"))
        .content(result.getString("content"))
        .build();
  }

  public void create(File file) {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement(
          "INSERT INTO file (repository, path, content) VALUES (?, ?, ?)")) {
        stmt.setString(1, file.repository());
        stmt.setString(2, file.path());
        stmt.setString(3, file.content());
        stmt.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }
}
