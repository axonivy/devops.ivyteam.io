package io.ivyteam.devops.github;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.ivyteam.devops.github.GitHubSynchronizer.Progress;

@Component
public class GitHubReindexingJob {

  @Autowired
  private GitHubSynchronizer synchronizer;

  @Scheduled(cron = "0 11 * * * ?")
  public void reindex() {
    Consumer<Progress> listener = progress -> System.out.println(progress.message());
    try {
      synchronizer.addListener(listener);
      synchronizer.run();
    } finally {
      synchronizer.removeListener(listener);
    }
  }
}
