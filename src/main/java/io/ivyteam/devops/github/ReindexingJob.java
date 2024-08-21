package io.ivyteam.devops.github;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReindexingJob {

  // 12am every day
  @Scheduled(cron = "0 0 0 * * ?")
  public void trackOverduePayments() {
    new GitHubSynchronizer(progress -> System.out.println(progress.message())).run();
  }
}
