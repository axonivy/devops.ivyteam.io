package io.ivyteam.devops;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route("")
public class HomeView extends AppLayout {

  public HomeView() {
    var toggle = new DrawerToggle();
    var title = new H1("ivyTeam DevOps");
    title.getStyle()
            .set("font-size", "var(--lumo-font-size-l)")
            .set("margin", "0");

    var nav = getSideNav();
    var scroller = new Scroller(nav);
    scroller.setClassName(LumoUtility.Padding.SMALL);
    addToDrawer(scroller);
    addToNavbar(toggle, title);
  }

  private SideNav getSideNav() {
    var sideNav = new SideNav();
    sideNav.addItem(new SideNavItem("Dashboard", "/", VaadinIcon.DASHBOARD.create()));
    return sideNav;
  }
}
