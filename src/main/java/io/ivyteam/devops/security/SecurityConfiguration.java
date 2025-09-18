package io.ivyteam.devops.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;

import io.ivyteam.devops.github.webhook.GitHubWebhookController;

@EnableWebSecurity
@Configuration
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
public class SecurityConfiguration {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(authz -> authz.requestMatchers(GitHubWebhookController.PATH).anonymous())
        .csrf(c -> c.ignoringRequestMatchers(GitHubWebhookController.PATH))
        .oauth2Login(c -> c.loginPage("/login").permitAll())
        .with(VaadinSecurityConfigurer.vaadin(), configurer -> {
        })
        .build();
  }

  @Bean
  public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository repo,
      OAuth2AuthorizedClientService service) {
    var provider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();
    var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(repo, service);
    manager.setAuthorizedClientProvider(provider);
    return manager;
  }
}
