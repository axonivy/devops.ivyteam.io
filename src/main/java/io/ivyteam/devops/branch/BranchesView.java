package io.ivyteam.devops.branch;

import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;

import io.ivyteam.devops.pullrequest.PullRequestRepository;
import io.ivyteam.devops.view.View;

@Route("branches")
public class BranchesView extends View {

  public BranchesView(BranchRepository branches, PullRequestRepository prRepo) {
    var allBranches = branches.all();
    var routeParameters = new RouteParameters();
    var component = new BranchGrid(allBranches, prRepo, BranchesView.class, routeParameters, this::updateTitle)
        .create();
    setContent(component);
  }

  private void updateTitle(GridListDataView<?> dataView) {
    title.setText("Branches (" + dataView.getItemCount() + ")");
  }
}
