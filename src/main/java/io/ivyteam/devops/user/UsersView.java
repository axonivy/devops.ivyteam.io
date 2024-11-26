package io.ivyteam.devops.user;

import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
        .addComponentColumn(user -> new AvatarLinkComponent(user))
        .setHeader("Name")
        .setWidth("100%")
        .setSortable(true)
        .setComparator(Comparator.comparing(User::name));

    setContent(grid);
  }

  public class AvatarLinkComponent extends HorizontalLayout {

    public AvatarLinkComponent(User user) {
      var avatar = new Avatar(user.name());
      avatar.setImage(user.avatarUrl());
      var link = new Anchor(user.link(), user.name());
      add(avatar, link);
      setSpacing(true);
      setAlignItems(FlexComponent.Alignment.CENTER);
    }
  }
}
