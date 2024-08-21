package io.ivyteam.devops.users;

import java.util.Comparator;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.repo.RepoRepository;
import io.ivyteam.devops.view.View;

@Route("/users")
public class UsersView extends View {

  private final Grid<User> grid;

  public UsersView() {
    var users = RepoRepository.INSTANCE.all().stream()
        .flatMap(repo -> repo.branches().stream())
        .map(branch -> branch.lastCommitAuthor())
        .distinct()
        .map(name -> new User(name))
        .toList();

    grid = new Grid<>();
    grid.setItems(users);

    grid.addColumn(LitRenderer.<User>of("""
           <a href="${item.link}">${item.name}</a>
        """)
        .withProperty("link", p -> "/users/" + p.name())
        .withProperty("name", p -> p.name()))
        .setHeader("Name")
        .setWidth("100%")
        .setSortable(true)
        .setComparator(Comparator.comparing(User::name));

    grid.setSizeFull();
    setContent(grid);
  }

  @Override
  public String title() {
    return "Users";
  }
}
