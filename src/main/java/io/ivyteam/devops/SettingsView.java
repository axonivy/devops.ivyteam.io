package io.ivyteam.devops;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;

@Route("/settings")
public class SettingsView extends View {
  
  public SettingsView() {
    var button = new Button("Reindex");
    button.addClickListener(clickEvent -> {
        Database.delete();
        new GitHubSynchronizer().run();
    });
    setContent(button);
  }

  public String title() {
    return "Settings";
  }
}
