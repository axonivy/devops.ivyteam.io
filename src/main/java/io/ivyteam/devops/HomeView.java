package io.ivyteam.devops;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.RepoRepository.Repo;

@Route("")
public class HomeView extends View {

  public HomeView() {
    new GitHubSynchronizer().run();
    var repos = RepoRepository.INSTANCE.all();

    var grid = new Grid<Repo>();
    grid.setItems(repos);
    grid
        .addColumn(Repo::name)
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
