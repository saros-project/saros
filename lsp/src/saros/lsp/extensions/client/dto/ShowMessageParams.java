package saros.lsp.extensions.client.dto;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ShowMessageRequestParams;

/** Bridge from Saros to language server protocol. */
public class ShowMessageParams extends ShowMessageRequestParams {

  public ShowMessageParams(MessageType type, String title, String message, String... actions) {

    this.setType(type);
    this.setMessage(
        String.format("%s%s%s%s", title, System.lineSeparator(), System.lineSeparator(), message));

    if (actions.length > 0) {
      this.setActions(createActionItemList(actions));
    }
  }

  /**
   * Creates the action items the user can choose from on client side based on their labels.
   *
   * @param actions The labels of the action items
   * @return ActionItems as specified by the language server protocol
   */
  private static List<MessageActionItem> createActionItemList(String... actions) {
    List<MessageActionItem> actionItems = new ArrayList<>();

    for (String action : actions) {
      actionItems.add(new MessageActionItem(action));
    }

    return actionItems;
  }
}
