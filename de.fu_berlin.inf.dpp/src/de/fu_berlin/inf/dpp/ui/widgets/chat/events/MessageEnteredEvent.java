package de.fu_berlin.inf.dpp.ui.widgets.chat.events;

import java.util.EventObject;

public class MessageEnteredEvent extends EventObject {

  private static final long serialVersionUID = -1262502917480818862L;

  protected String enteredMessage;

  /**
   * Constructs a message entered event
   *
   * @param source The object on which the Event initially occurred.
   * @param message The entered message
   */
  public MessageEnteredEvent(Object source, String message) {
    super(source);

    this.enteredMessage = message;
  }

  /**
   * Gets the entered message
   *
   * @return
   */
  public String getEnteredMessage() {
    return enteredMessage;
  }
}
