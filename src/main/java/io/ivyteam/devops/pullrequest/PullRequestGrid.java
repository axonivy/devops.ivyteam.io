package io.ivyteam.devops.pullrequest;

import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.LitRenderer;

import io.ivyteam.devops.repo.PullRequest;

public class PullRequestGrid {

  public static Grid<PullRequest> create(List<PullRequest> prs) {
    var grid = new Grid<PullRequest>();
    grid.setItems(prs);
    grid.addColumn(LitRenderer.<PullRequest>of("""
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
    grid.setSizeFull();
    return grid;
  }
}
