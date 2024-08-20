package io.ivyteam.devops;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.RepoRepository.Repo;

@Route("")
public class HomeView extends View {

  public HomeView() {
    var repos = RepoRepository.INSTANCE.all();

    var grid = new Grid<Repo>();
    grid.setItems(repos);
    grid.addColumn(LitRenderer.<Repo> of("""
          <a href="${item.link}">${item.name}</a>
       """)
       .withProperty("link", p -> p.link())
       .withProperty("name", p -> p.name()))
       .setHeader("Name")
       .setWidth("40%")
       .setSortable(true);

    grid
        .addColumn(Repo::archived)
        .setHeader("Archived")
        .setWidth("10%")
        .setSortable(true);

    grid
        .addColumn(Repo::openPullRequests)
        .setHeader("Open PRs")
        .setWidth("10%")
        .setSortable(true);

    grid
        .addColumn(Repo::license)
        .setHeader("License")
        .setWidth("40%")
        .setSortable(true);

    grid.setHeightFull();
    setContent(grid);
  }

  public String title() {
    return "Dashboard";
  }
}
