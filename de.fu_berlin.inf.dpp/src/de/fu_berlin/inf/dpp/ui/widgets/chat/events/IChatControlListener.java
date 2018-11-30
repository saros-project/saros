package de.fu_berlin.inf.dpp.ui.widgets.chat.events;

public interface IChatControlListener extends IChatDisplayListener {

  public void characterEntered(CharacterEnteredEvent event);

  public void messageEntered(MessageEnteredEvent event);
}
