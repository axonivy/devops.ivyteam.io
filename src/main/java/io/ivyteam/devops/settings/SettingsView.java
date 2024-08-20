package io.ivyteam.devops.settings;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.db.Database;
import io.ivyteam.devops.github.GitHubSynchronizer;
import io.ivyteam.devops.github.GitHubSynchronizer.Progress;
import io.ivyteam.devops.view.View;

@Route("/settings")
public class SettingsView extends View {
  
  private final NativeLabel progressBarLabelText;
  private final ProgressBar progressBar;

  public SettingsView() {
    var div = new VerticalLayout();
    
    var button = new Button("Reindex");
    var ui = UI.getCurrent();
    button.addClickListener(clickEvent -> {
      Database.delete();
      new Thread(() -> new GitHubSynchronizer(progress -> updateUi(ui, progress)).run()).start();
    });
    
    progressBarLabelText = new NativeLabel();
    progressBarLabelText.setVisible(false);
    progressBar = new ProgressBar();   
    progressBar.setVisible(false);
    progressBar.setMaxWidth("50%");

    div.add(button);
    div.add(progressBarLabelText, progressBar);

    setContent(div);
  }

  @Override
  public String title() {
    return "Settings";
  }

  private void updateUi(UI ui, Progress progress) {
    ui.access(() -> {      
      progressBarLabelText.setVisible(true);
      progressBar.setVisible(true);
      progressBarLabelText.setText(progress.message());
      progressBar.setValue(progress.percent());
    });
  }
}
