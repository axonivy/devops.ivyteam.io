package io.ivyteam.devops.pullrequest;

import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.user.UserCache;
import io.ivyteam.devops.user.UserRepository;
import io.ivyteam.devops.view.View;

@Route("/pulls")
public class PullRequestsView extends View {

  public PullRequestsView(PullRequestRepository prs, UserRepository users) {
    var userCache = new UserCache(users.all());
    var grid = PullRequestGrid.create(prs.all(), this::updateTitle, userCache);
    setContent(grid);
  }

  private void updateTitle(GridListDataView<?> dataView) {
    title.setText("Pull Requests (" + dataView.getItemCount() + ")");
  }
}
