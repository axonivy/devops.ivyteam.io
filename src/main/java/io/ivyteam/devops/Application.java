package io.ivyteam.devops;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

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
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void configurePage(AppShellSettings settings) {
    settings.setPageTitle("ivyTeam DevOps");
    settings.addFavIcon("icon", "icons/axonivy.svg", "80x80");
  }
}
