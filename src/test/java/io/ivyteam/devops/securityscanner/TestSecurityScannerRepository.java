package io.ivyteam.devops.securityscanner;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.ivyteam.devops.db.Database;
import io.ivyteam.devops.repo.Repo;
import io.ivyteam.devops.repo.RepoRepository;
import io.ivyteam.devops.securityscanner.SecurityScannerRepository.Key;

class TestSecurityScannerRepository {

  SecurityScanner SCANNER_1 = SecurityScanner.create()
      .critical(5)
      .high(6)
      .low(2)
      .medium(4)
      .scantype(ScanType.DEPENDABOT)
      .repo("axonivy/test")
      .build();

  SecurityScanner SCANNER_2 = SecurityScanner.create()
      .critical(5)
      .high(6)
      .low(2)
      .medium(4)
      .scantype(ScanType.SECRET_SCANNING)
      .repo("axonivy/test")
      .build();

  SecurityScanner SCANNER_3 = SecurityScanner.create()
      .critical(5)
      .high(6)
      .low(2)
      .medium(4)
      .scantype(ScanType.SECRET_SCANNING)
      .repo("axonivy/vulnerarbilty")
      .build();

  @TempDir
  Path tempDir;

  SecurityScannerRepository scanners;

  RepoRepository repos;

  @BeforeEach
  void beforeEach() {
    var db = new Database(tempDir.resolve("test.db"));
    scanners = new SecurityScannerRepository(db);
    repos = new RepoRepository(db);
  }

  @Test
  void all() {
    repos.create(Repo.create().name("axonivy/test").build());
    repos.create(Repo.create().name("axonivy/vulnerarbilty").build());
    scanners.create(SCANNER_1);
    scanners.create(SCANNER_2);
    scanners.create(SCANNER_3);
    assertThat(scanners.all()).containsOnly(
        Map.entry(new Key("axonivy/test", ScanType.DEPENDABOT), SCANNER_1),
        Map.entry(new Key("axonivy/test", ScanType.SECRET_SCANNING), SCANNER_2),
        Map.entry(new Key("axonivy/vulnerarbilty", ScanType.SECRET_SCANNING), SCANNER_3));
  }
}
