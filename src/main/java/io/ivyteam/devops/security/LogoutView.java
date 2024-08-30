package io.ivyteam.devops.security;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("logout")
@PageTitle("Logout")
@AnonymousAllowed
public class LogoutView extends VerticalLayout {

  public LogoutView(@Autowired UserSession session) {
    session.logout();
  }
}
