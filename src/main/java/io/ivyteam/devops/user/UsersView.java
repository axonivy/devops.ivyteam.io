package io.ivyteam.devops.user;

import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
        .addComponentColumn(AvatarLinkComponent::new)
        .setHeader("Login")
        .setWidth("12%")
        .setSortable(true)
        .setComparator(Comparator.comparing(User::login));

    grid
        .addColumn(user -> user.name())
        .setHeader("Name")
        .setWidth("12%")
        .setSortable(true)
        .setComparator(Comparator.comparing(User::name));

    grid
        .addColumn(user -> user.email())
        .setHeader("Email")
        .setWidth("12%")
        .setSortable(true)
        .setComparator(Comparator.comparing(User::email));

    grid
        .addColumn(user -> user.location())
        .setHeader("Location")
        .setWidth("15%")
        .setSortable(true)
        .setComparator(Comparator.comparing(User::location));

    grid
        .addColumn(user -> user.company())
        .setHeader("Company")
        .setWidth("15%")
        .setSortable(true)
        .setComparator(Comparator.comparing(User::company));

    grid
        .addColumn(user -> user.bio())
        .setHeader("Bio")
        .setWidth("34%")
        .setSortable(true)
        .setComparator(Comparator.comparing(User::bio));

    setContent(grid);
  }

  public static Component avatar(User user) {
    if (user == null) {
      return null;
    }
    return new AvatarLinkComponent(user);
  }

  public static class AvatarLinkComponent extends HorizontalLayout {

    public AvatarLinkComponent(User user) {
      var icon = createIcon(VaadinIcon.EXTERNAL_LINK);
      var avatar = new Avatar(user.login());
      avatar.setImage(user.avatarUrl());
      avatar.setWidth("30px");
      avatar.setHeight("30px");
      add(avatar);
      add(new Anchor(user.link(), user.login()));
      add(new Anchor(user.ghLink(), icon));
      setAlignItems(Alignment.CENTER);
      setSpacing(false);
      getThemeList().add("spacing-xs");
    }
  }

  private static Icon createIcon(VaadinIcon vaadinIcon) {
    var icon = vaadinIcon.create();
    icon.getStyle().set("padding", "0.25em");
    icon.getStyle().set("margin-bottom", "2px");
    icon.setSize("var(--lumo-icon-size-s)");
    return icon;
  }
}
