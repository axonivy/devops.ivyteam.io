package io.ivyteam.devops.branch;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;

import io.ivyteam.devops.view.View;

@Route("branches")
public class BranchesView extends View {

  public BranchesView(BranchRepository branches) {
    var allBranches = branches.all();
    title.setText("Branches (" + allBranches.size() + ")");
    var routeParameters = new RouteParameters();
    var component = new BranchGrid(allBranches, BranchesView.class, routeParameters).create();
    setContent(component);
  }
}
