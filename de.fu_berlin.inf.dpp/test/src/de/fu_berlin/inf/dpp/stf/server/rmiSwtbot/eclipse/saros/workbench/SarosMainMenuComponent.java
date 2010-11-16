package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.MainMenuComponent;

public interface SarosMainMenuComponent extends MainMenuComponent {

    public void creatNewAccount(JID jid, String password)
        throws RemoteException;
}
