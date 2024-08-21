package io.ivyteam.devops.github;

import java.util.function.Consumer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.ivyteam.devops.github.GitHubSynchronizer.Progress;

@Component
public class ReindexingJob {

  // 12am every day
  @Scheduled(cron = "0 0 0 * * ?")
  public void reindex() {
    Consumer<Progress> listener = progress -> System.out.println(progress.message());
    try {
      GitHubSynchronizer.INSTANCE.addListener(listener);
      GitHubSynchronizer.INSTANCE.run();
    } finally {
      GitHubSynchronizer.INSTANCE.removeListener(listener);
    }
  }
}
