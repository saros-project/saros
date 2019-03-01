package saros.intellij.ui.actions;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.picocontainer.annotations.Inject;
import saros.communication.connection.ConnectionHandler;

/** Disconnects from XMPP/Jabber server */
public class DisconnectServerAction extends AbstractSarosAction {
  public static final String NAME = "disconnect";

  @Inject private ConnectionHandler connectionHandler;

  @Override
  public String getActionName() {
    return NAME;
  }

  @Override
  public void execute() {

    // FIXME use the project from the action event !
    // AnActionEvent.getDataContext().getData(DataConstants.PROJECT)
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
