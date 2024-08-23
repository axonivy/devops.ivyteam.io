package io.ivyteam.devops.user;

import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.view.View;

@Route("/users")
public class UsersView extends View {

  @Autowired
  public UsersView(UserRepository users) {
    var allUsers = users.all();
    title.setText("Users (" + allUsers.size() + ")");
    var grid = new Grid<>(allUsers);
    grid.setSizeFull();

    grid
        .addColumn(new ComponentRenderer<>(user -> new Anchor(user.link(), user.name())))
        .setHeader("Name")
        .setWidth("100%")
        .setSortable(true)
        .setComparator(Comparator.comparing(User::name));

    setContent(grid);
  }
}
