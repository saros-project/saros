package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponent;

public interface SarosComponent extends EclipseComponent {

    public void confirmShellCreateNewXMPPAccount(JID jid, String password)
        throws RemoteException;

    public void confirmWizardSarosConfiguration(JID jid, String password)
        throws RemoteException;

    /**
     * After clicking one of the sub menu of the context menu "Saros" in the
     * package explorer view host will get the popup window with the title
     * "Invitation". This method confirm the popup window.
     * 
     * @param baseJIDOfinvitees
     *            the base JID of the users with whom you want to share your
     *            project.
     * @throws RemoteException
     */
    public void confirmShellInvitation(String... baseJIDOfinvitees)
        throws RemoteException;

}
