package io.ivyteam.devops.user;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.branch.BranchGrid;
import io.ivyteam.devops.branch.BranchRepository;
import io.ivyteam.devops.pullrequest.PullRequestGrid;
import io.ivyteam.devops.pullrequest.PullRequestRepository;
import io.ivyteam.devops.view.View;

@Route("/users")
public class UserView extends View implements HasUrlParameter<String> {

  @Autowired
  BranchRepository branches;

  @Autowired
  PullRequestRepository pullRequests;

  @Override
  public void setParameter(BeforeEvent event, String user) {
    title.setText(user);

    var tabSheet = new TabSheet();
    tabSheet.setSizeFull();

    var allPrs = pullRequests.findByUser(user);
    var prCounter = new Span("Pull Requests (" + allPrs.size() + ")");
    var tabPrs = new Tab(prCounter);
    var gridPrs = PullRequestGrid.create(allPrs);
    tabSheet.add(tabPrs, gridPrs);

    var allBranches = branches.findByUser(user);
    var branchCounter = new Span("Branches (" + allBranches.size() + ")");
    var tabBranches = new Tab(branchCounter);
    var gridBranches = BranchGrid.create(allBranches);
    tabSheet.add(tabBranches, gridBranches);

    setContent(tabSheet);
  }
}
