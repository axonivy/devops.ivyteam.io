package io.ivyteam.devops.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class OAuth2TokenRequester {

  @Autowired
  private OAuth2AuthorizedClientService service;

  public String accessToken() {
    var authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    var client = service.loadAuthorizedClient(
        authentication.getAuthorizedClientRegistrationId(),
        authentication.getName());
    return client.getAccessToken().getTokenValue();
  }
}
