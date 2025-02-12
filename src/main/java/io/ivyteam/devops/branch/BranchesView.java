package io.ivyteam.devops.branch;

import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;

import io.ivyteam.devops.pullrequest.PullRequestCache;
import io.ivyteam.devops.pullrequest.PullRequestRepository;
import io.ivyteam.devops.user.UserCache;
import io.ivyteam.devops.user.UserRepository;
import io.ivyteam.devops.view.View;

@Route("branches")
public class BranchesView extends View {

  public BranchesView(BranchRepository branches, PullRequestRepository prRepo, UserRepository users) {
    var allBranches = branches.all();
    var routeParameters = new RouteParameters();
    var userCache = new UserCache(users.all());
    var prCache = new PullRequestCache(prRepo.all());
    var component = new BranchGrid(allBranches, prCache, BranchesView.class, routeParameters, this::updateTitle,
        userCache)
        .create();
    setContent(component);
  }

  private void updateTitle(GridListDataView<?> dataView) {
    title.setText("Branches (" + dataView.getItemCount() + ")");
  }
}
