package io.ivyteam.devops.pullrequest;

import com.vaadin.flow.router.Route;

import io.ivyteam.devops.view.View;

@Route("/pullrequests")
public class PullRequestsView extends View {

  public PullRequestsView(PullRequestRepository prs) {
    var allPrs = prs.all();
    title.setText("Pull Requests (" + allPrs.size() + ")");
    var gridPrs = PullRequestGrid.create(allPrs);
    setContent(gridPrs);
  }
}
