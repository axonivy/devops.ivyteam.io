package io.ivyteam.devops.branches;

import java.util.Comparator;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import io.ivyteam.devops.repo.Branch;
import io.ivyteam.devops.users.User;

public class BranchesGrid {

  public static Grid<Branch> create(List<Branch> branches) {
    var grid = new Grid<>(branches);
    grid.setSizeFull();

    grid.addColumn(
        new ComponentRenderer<>(
            branch -> new Anchor(new User(branch.lastCommitAuthor()).link(), branch.lastCommitAuthor())))
        .setHeader("Author")
        .setWidth("10%")
        .setSortable(true)
        .setComparator(Comparator.comparing(Branch::lastCommitAuthor));

    grid.addColumn(new ComponentRenderer<>(branch -> new Anchor(branch.repoLink(), branch.repository())))
        .setHeader("Repository")
        .setWidth("20%")
        .setSortable(true)
        .setComparator(Comparator.comparing(Branch::repository));

    grid
        .addColumn(new ComponentRenderer<>(branch -> new Anchor(branch.ghLink(), branch.name())))
        .setHeader("Name")
        .setSortable(true)
        .setComparator(Comparator.comparing(Branch::name))
        .setWidth("40%");
    return grid;
  }
}
