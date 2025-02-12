package io.ivyteam.devops.user;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.internal.HasUrlParameterFormat;

import io.ivyteam.devops.branch.BranchGrid;
import io.ivyteam.devops.branch.BranchRepository;
import io.ivyteam.devops.pullrequest.PullRequestCache;
import io.ivyteam.devops.pullrequest.PullRequestGrid;
import io.ivyteam.devops.pullrequest.PullRequestRepository;
import io.ivyteam.devops.view.View;

@Route("/users")
public class UserView extends View implements HasUrlParameter<String> {

  @Autowired
  BranchRepository branches;

  @Autowired
  PullRequestRepository pullRequests;

  @Autowired
  private UserRepository users;

  private Span prCounter = new Span();
  private Span branchCounter = new Span();

  @Override
  public void setParameter(BeforeEvent event, String user) {
    prCounter = new Span();
    title.setText(user);

    var tabSheet = new TabSheet();
    tabSheet.setSizeFull();

    var allPrs = pullRequests.findByUser(user);
    var prCache = new PullRequestCache(allPrs);
    var tabPrs = new Tab(prCounter);
    var userCache = new UserCache(users.all());
    var gridPrs = PullRequestGrid.create(allPrs, this::updatePrTitle, userCache);
    tabSheet.add(tabPrs, gridPrs);

    var allBranches = branches.findByUser(user);
    var tabBranches = new Tab(branchCounter);

    var routeParameters = new RouteParameters(HasUrlParameterFormat.PARAMETER_NAME, user);
    var gridBranches = new BranchGrid(allBranches, prCache, UserView.class, routeParameters,
        this::updateBranchTitle, userCache).create();
    tabSheet.add(tabBranches, gridBranches);
    setContent(tabSheet);
  }

  private void updatePrTitle(GridListDataView<?> dataView) {
    prCounter.setText("Pull Requests (" + dataView.getItemCount() + ")");
  }

  private void updateBranchTitle(GridListDataView<?> dataView) {
    branchCounter.setText("Branches (" + dataView.getItemCount() + ")");
  }

  public static Component userLink(User user) {
    var icon = createIcon(VaadinIcon.EXTERNAL_LINK);
    var layout = new HorizontalLayout();
    layout.add(new Anchor(user.link(), user.name()));
    layout.add(new Anchor(user.ghLink(), icon));
    layout.setSpacing(false);
    layout.setAlignItems(Alignment.CENTER);
    return layout;
  }

  private static Icon createIcon(VaadinIcon vaadinIcon) {
    var icon = vaadinIcon.create();
    icon.getStyle().set("padding", "0.25em");
    icon.getStyle().set("margin-bottom", "2px");
    icon.setSize("var(--lumo-icon-size-s)");
    return icon;
  }
}
