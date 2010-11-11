package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.IEclipseMainMenuObject;

public interface ISarosMainMenuObject extends IEclipseMainMenuObject {

    public void creatNewAccount(JID jid, String password)
        throws RemoteException;
}
