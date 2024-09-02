package io.ivyteam.devops.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;

import io.ivyteam.devops.security.UserSession;
import jakarta.annotation.security.PermitAll;

@PermitAll
public abstract class View extends AppLayout {

  protected H1 title;

  public View() {
    var layout = new HorizontalLayout();

    title = new H1(title());
    title.getStyle()
        .set("font-size", "var(--lumo-font-size-l)")
        .set("margin-left", "18px")
        .set("padding-top", "5px");
    layout.addAndExpand(title);

    var session = ApplicationContextUtil.getBean(UserSession.class);
    var user = session.user();
    var avatar = new Avatar(user.username(), user.avatarUrl());
    avatar.setTooltipEnabled(true);
    avatar.getStyle().set("margin-right", "20px");
    avatar.addThemeVariants(AvatarVariant.LUMO_SMALL);
    layout.add(avatar);
    addToNavbar(layout);

    var nav = getSideNav();
    var scroller = new Scroller(nav);
    scroller.setClassName(LumoUtility.Padding.SMALL);
    addToDrawer(scroller);

    setPrimarySection(Section.DRAWER);
  }

  private SideNav getSideNav() {
    var sideNav = new SideNav();
    sideNav.addItem(new SideNavItem("Repositories", "/", VaadinIcon.DATABASE.create()));
    sideNav.addItem(new SideNavItem("Pull Requests", "/pulls", VaadinIcon.ARROW_BACKWARD.create()));
    sideNav.addItem(new SideNavItem("Branches", "/branches", VaadinIcon.ROAD_BRANCH.create()));
    sideNav.addItem(new SideNavItem("Users", "/users", VaadinIcon.USERS.create()));
    sideNav.addItem(new SideNavItem("Settings", "/settings", VaadinIcon.COG_O.create()));
    return sideNav;
  }

  public String title() {
    return "";
  }
}
