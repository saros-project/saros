package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.SarosObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.SarosRmiSWTWorkbenchBot;

public class ChatViewObject extends SarosObject implements IChatViewObject {
    public static ChatViewObject classVariable;

    public ChatViewObject(SarosRmiSWTWorkbenchBot sarosRmiBot) {
        super(sarosRmiBot);
    }

    public void waitUntilGetChatMessage(String jid, String message)
        throws RemoteException {
        waitUntil(SarosConditions.isChatMessageExist(sarosRmiBot, jid, message));
    }

}
