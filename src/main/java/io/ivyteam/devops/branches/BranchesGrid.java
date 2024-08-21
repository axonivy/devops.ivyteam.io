package io.ivyteam.devops.branches;

import java.util.Comparator;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;

import io.ivyteam.devops.repo.Branch;

public class BranchesGrid {

  public static Grid<Branch> create(List<Branch> branches) {
    var grid = new Grid<Branch>();
    grid.setItems(branches);

    grid
        .addColumn(Branch::lastCommitAuthor)
        .setHeader("Author")
        .setWidth("10%")
        .setSortable(true);

    grid.addColumn(LitRenderer.<Branch>of("""
           <a href="${item.link}">${item.name}</a>
        """)
        .withProperty("link", p -> p.repoLink())
        .withProperty("name", p -> p.repository()))
        .setHeader("Repository")
        .setWidth("20%")
        .setSortable(true)
        .setComparator(Comparator.comparing(Branch::repository));

    grid
        .addColumn(new ComponentRenderer<Anchor, Branch>(b -> {
          return new Anchor(b.ghLink(), b.name());
        }))
        .setHeader("Name")
        .setWidth("40%");
    grid.setSizeFull();
    return grid;
  }
}
