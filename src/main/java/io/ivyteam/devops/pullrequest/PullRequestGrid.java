package io.ivyteam.devops.pullrequest;

import java.util.Comparator;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import io.ivyteam.devops.user.User;

public class PullRequestGrid {

  public static Grid<PullRequest> create(List<PullRequest> prs) {
    var grid = new Grid<>(prs);
    grid.setSizeFull();

    grid
        .addColumn(pr -> pr.id())
        .setHeader("ID")
        .setWidth("10%")
        .setSortable(true);

    grid
        .addColumn(new ComponentRenderer<>(pr -> new Anchor(pr.ghLink(), pr.title())))
        .setHeader("Title")
        .setWidth("60%")
        .setSortable(true)
        .setComparator(Comparator.comparing(PullRequest::title));

    grid
        .addColumn(new ComponentRenderer<>(pr -> new Anchor(new User(pr.user()).link(), pr.user())))
        .setHeader("Name")
        .setWidth("30%")
        .setSortable(true)
        .setComparator(Comparator.comparing(PullRequest::user));

    return grid;
  }
}
