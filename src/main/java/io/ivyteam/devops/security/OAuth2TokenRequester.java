package io.ivyteam.devops.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;

@Service
public class OAuth2TokenRequester {

  private OAuth2AuthorizedClientManager authorizedClientManager;

  public OAuth2TokenRequester(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
    this.authorizedClientManager = oAuth2AuthorizedClientManager;
  }

  private OAuth2AuthorizedClient authorize() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    var request = OAuth2AuthorizeRequest.withClientRegistrationId("github").principal(authentication).build();
    return authorizedClientManager.authorize(request);
  }

  public String requestAccessToken() {
    return authorize().getAccessToken().getTokenValue();
  }
}
