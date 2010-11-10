package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages;

import java.rmi.RemoteException;

public interface IChatViewObject {

    public void waitUntilGetChatMessage(String jid, String message)
        throws RemoteException;
}
