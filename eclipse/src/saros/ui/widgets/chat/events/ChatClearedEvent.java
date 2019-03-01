package saros.ui.widgets.chat.events;

import java.util.EventObject;

public class ChatClearedEvent extends EventObject {

  private static final long serialVersionUID = 8613356195511010329L;

  /**
   * Constructs a chat cleared event
   *
   * @param source The object on which the Event initially occurred.
   */
  public ChatClearedEvent(Object source) {
    super(source);
  }
}
