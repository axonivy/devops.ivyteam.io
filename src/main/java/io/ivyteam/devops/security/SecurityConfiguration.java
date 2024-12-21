package io.ivyteam.devops.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

import io.ivyteam.devops.github.webhook.GitHubWebhookController;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authz -> authz.requestMatchers(GitHubWebhookController.PATH).anonymous());
    super.configure(http);
    http.oauth2Login(c -> c.loginPage("/login").permitAll());
  }

  @Bean
  public OAuth2AuthorizedClientManager authorizedClientManager(
      ClientRegistrationRepository repo,
      OAuth2AuthorizedClientService service) {
    var provider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();
    var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(repo, service);
    manager.setAuthorizedClientProvider(provider);
    return manager;
  }
}
