package saros.ui.widgets.chat.events;

import java.util.EventListener;

public interface IChatDisplayListener extends EventListener {

  public void chatCleared(ChatClearedEvent event);
}
