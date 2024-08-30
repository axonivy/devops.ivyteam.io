package io.ivyteam.devops.security;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route("login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

  private static final String OAUTH_URL = "/oauth2/authorization/github";

  public LoginView() {
    setAlignItems(Alignment.CENTER);
    Anchor loginLink = new Anchor(OAUTH_URL, "Login with GitHub");
    loginLink.addClassName(LumoUtility.FontSize.XLARGE);
    loginLink.setRouterIgnore(true);
    add(loginLink);
  }
}
