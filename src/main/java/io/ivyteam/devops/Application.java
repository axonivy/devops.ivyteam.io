package io.ivyteam.devops;

import java.time.Clock;
import java.util.HashMap;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.MapPropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import io.ivyteam.devops.settings.SettingsManager;

@SpringBootApplication
@Theme(value = "my-theme", variant = Lumo.LIGHT)
@Push
@EnableScheduling
public class Application implements AppShellConfigurator {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }

  public static void main(String[] args) {
    new SpringApplicationBuilder(Application.class)
        .initializers(new MyContextInitializer())
        .run(args);
  }

  @Override
  public void configurePage(AppShellSettings settings) {
    settings.setPageTitle("ivyTeam DevOps");
    settings.addFavIcon("icon", "icons/axonivy.svg", "80x80");
  }

  private static class MyContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      var environment = configurableApplicationContext.getEnvironment();
      var props = new HashMap<String, Object>();
      var settings = SettingsManager.INSTANCE.get();
      props.put("spring.security.oauth2.client.registration.github.client-id", settings.gitHubClientId());
      props.put("spring.security.oauth2.client.registration.github.client-secret", settings.gitHubClientSecret());
      environment.getPropertySources().addFirst(new MapPropertySource("devops-props", props));
    }
  }
}
