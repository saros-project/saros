package saros.lsp.ui;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import saros.lsp.extensions.client.ISarosLanguageClient;

/** A manager that provides methods to request user input. */
public class UIInteractionManager {

  private ISarosLanguageClient client;

  public UIInteractionManager(ISarosLanguageClient client) {
    this.client = client;
  }

  /**
   * Shows the user a message with a choice between yes and no to answer.
   *
   * @param title The title of the message to show
   * @param message The actual message
   * @return <i>true</i> if the user responds with <b>yes</b> and <i>false</i> otherwise
   */
  public boolean getUserInputYesNo(final String title, final String message) {

    final MessageActionItem yes = new MessageActionItem("yes");
    final MessageActionItem no = new MessageActionItem("no");

    final ShowMessageRequestParams params = new ShowMessageRequestParams();
    params.setType(MessageType.Info);
    params.setMessage(String.format("%s\n\n%s", title, message));
    params.setActions(Arrays.asList(yes, no));

    MessageActionItem result;

    try {
      result = this.client.showMessageRequest(params).get();
      final boolean r = result.getTitle().equalsIgnoreCase("yes");

      return r;
    } catch (InterruptedException | ExecutionException e) {
      final MessageParams p = new MessageParams();
      p.setType(MessageType.Error);
      p.setMessage(e.toString());

      this.client.showMessage(p);
      return false;
    }
  }
}
