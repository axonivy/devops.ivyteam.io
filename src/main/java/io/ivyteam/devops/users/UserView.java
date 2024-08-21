package io.ivyteam.devops.users;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.branches.BranchesGrid;
import io.ivyteam.devops.pullrequest.PullRequestGrid;
import io.ivyteam.devops.repo.RepoRepository;
import io.ivyteam.devops.view.View;

@Route("/users")
public class UserView extends View implements HasUrlParameter<String> {

  @Override
  public String title() {
    return "User";
  }

  @Override
  public void setParameter(BeforeEvent event, String parameter) {
    var prs = RepoRepository.INSTANCE.all().stream()
        .flatMap(repo -> repo.prs().stream())
        .filter(pr -> pr.user().equals(parameter))
        .toList();

    var branches = RepoRepository.INSTANCE.all().stream()
        .flatMap(repo -> repo.branches().stream())
        .filter(branch -> branch.lastCommitAuthor().equals(parameter))
        .toList();

    var tabSheet = new TabSheet();

    var prCounter = new Span("Pull Requests (" + prs.size() + ")");
    var tabPrs = new Tab(prCounter);
    var gridPrs = PullRequestGrid.create(prs);
    tabSheet.add(tabPrs, gridPrs);

    var branchCounter = new Span("Branches (" + branches.size() + ")");
    var tabBranches = new Tab(branchCounter);
    var gridBranches = BranchesGrid.create(branches);
    tabSheet.add(tabBranches, gridBranches);

    tabSheet.setSizeFull();
    setContent(tabSheet);
  }
}
