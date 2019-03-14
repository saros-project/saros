package saros.intellij.ui.actions;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.repackaged.picocontainer.annotations.Inject;

/** Disconnects from XMPP/Jabber server */
public class DisconnectServerAction extends AbstractSarosAction {
  public static final String NAME = "disconnect";

  private final Project project;

  @Inject private ConnectionHandler connectionHandler;

  public DisconnectServerAction(Project project) {
    this.project = project;

    SarosPluginContext.initComponent(this);
  }

  @Override
  public String getActionName() {
    return NAME;
  }

  @Override
  public void execute() {
    ProgressManager.getInstance()
        .run(
            new Task.Modal(project, "Disconnecting...", false) {

              @Override
              public void run(ProgressIndicator indicator) {

                LOG.info(
                    "Disconnecting current connection: " + connectionHandler.getConnectionID());

                indicator.setIndeterminate(true);

                try {
                  connectionHandler.disconnect();
                } finally {
                  indicator.stop();
                }
              }
            });
  }
}
