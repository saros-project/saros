package de.fu_berlin.inf.dpp.ui.chat.chatControl.events;

import java.util.EventListener;

public interface IChatListener extends EventListener {

    public void characterEntered(CharacterEnteredEvent event);

    public void messageEntered(MessageEnteredEvent event);

}
