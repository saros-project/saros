package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.SarosRmiSWTWorkbenchBot;

public class ChatViewObject extends EclipseObject implements IChatViewObject {

    public static ChatViewObject classVariable;

    public ChatViewObject(SarosRmiSWTWorkbenchBot rmiBot) {
        super(rmiBot);
    }

    public void waitUntilGetChatMessage(String jid, String message)
        throws RemoteException {
        waitUntil(SarosConditions.isChatMessageExist(rmiBot, jid, message));
    }

}
