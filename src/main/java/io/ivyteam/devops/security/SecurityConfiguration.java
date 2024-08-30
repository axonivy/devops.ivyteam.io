package io.ivyteam.devops.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);
    http.oauth2Login(c -> c.loginPage("/login").permitAll());
  }

  @Bean
  public OAuth2AuthorizedClientManager authorizedClientManager(
      ClientRegistrationRepository clientRegistrationRepository,
      OAuth2AuthorizedClientService authorizedClientService) {

    OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
        .clientCredentials().build();

    AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
        clientRegistrationRepository, authorizedClientService);
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

    return authorizedClientManager;
  }
}
