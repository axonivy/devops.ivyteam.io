package io.ivyteam.devops.security;

import java.io.Serializable;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;

@Component
@SessionScope
public class UserSession implements Serializable {

  public AuthenticatedUser user() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    var principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();
    var username = principal.getAttribute("login").toString();
    var email = principal.getAttribute("email").toString();
    var avatarUrl = principal.getAttribute("avatar_url").toString();
    var profileUrl = principal.getAttribute("html_url").toString();
    return new AuthenticatedUser(username, email, avatarUrl, profileUrl);
  }

  public void logout() {
    UI.getCurrent().getPage().setLocation("/");
    var logoutHandler = new SecurityContextLogoutHandler();
    var request = VaadinServletRequest.getCurrent().getHttpServletRequest();
    logoutHandler.logout(request, null, null);
  }
}
