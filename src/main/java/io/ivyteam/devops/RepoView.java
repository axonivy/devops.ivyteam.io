package io.ivyteam.devops;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;

import io.ivyteam.devops.RepoRepository.PullRequest;
import io.ivyteam.devops.RepoRepository.Repo;

@Route("/repository")
public class RepoView extends View implements HasUrlParameter<String> {

  @Override
  String title() {
    return "Repository";
  }

  @Override
  public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
    var repo = RepoRepository.INSTANCE.all().stream()
        .filter(r -> r.name().equals(parameter))
        .findAny()
        .orElseThrow();

    var grid = new Grid<PullRequest>();
    grid.setItems(repo.prs());

    /*grid
        .addColumn(PullRequest::title)
        .setHeader("Title")
        .setWidth("70%")
        .setSortable(true);*/

    grid.addColumn(LitRenderer.<PullRequest> of("""
          <a href="${item.link}">${item.title}</a>
       """)
       .withProperty("link", pr -> pr.ghLink())
       .withProperty("title", pr -> pr.title()))
       .setHeader("Title")
       .setWidth("70%")
       .setSortable(true);

    grid
        .addColumn(PullRequest::user)
        .setHeader("User")
        .setWidth("30%")
        .setSortable(true);

    grid.setHeightFull();
    setContent(grid);
  }
}
