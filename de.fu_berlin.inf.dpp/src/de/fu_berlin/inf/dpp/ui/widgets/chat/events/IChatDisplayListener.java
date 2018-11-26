package de.fu_berlin.inf.dpp.ui.widgets.chat.events;

import java.util.EventListener;

public interface IChatDisplayListener extends EventListener {

  public void chatCleared(ChatClearedEvent event);
}
