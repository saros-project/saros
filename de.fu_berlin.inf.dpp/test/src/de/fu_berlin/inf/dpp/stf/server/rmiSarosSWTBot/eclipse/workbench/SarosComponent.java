package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponent;

public interface SarosComponent extends EclipseComponent {

    public void confirmShellCreateNewXMPPAccount(JID jid, String password)
        throws RemoteException;

    public void confirmWizardSarosConfiguration(JID jid, String password)
        throws RemoteException;
}
