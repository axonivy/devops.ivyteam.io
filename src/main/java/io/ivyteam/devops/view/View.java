package io.ivyteam.devops.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;

public abstract class View extends AppLayout {

  public View() {
    var toggle = new DrawerToggle();
    var title = new H1(title());
    title.getStyle()
        .set("font-size", "var(--lumo-font-size-l)");

    var nav = getSideNav();
    var scroller = new Scroller(nav);
    scroller.setClassName(LumoUtility.Padding.SMALL);
    addToDrawer(scroller);
    addToNavbar(toggle, title);

    setPrimarySection(Section.DRAWER);
  }

  private SideNav getSideNav() {
    var sideNav = new SideNav();
    sideNav.addItem(new SideNavItem("Dashboard", "/", VaadinIcon.DASHBOARD.create()));
    sideNav.addItem(new SideNavItem("Branches", "/branches", VaadinIcon.CODE.create()));
    sideNav.addItem(new SideNavItem("Settings", "/settings", VaadinIcon.LIST.create()));
    return sideNav;
  }

  public abstract String title();
}
