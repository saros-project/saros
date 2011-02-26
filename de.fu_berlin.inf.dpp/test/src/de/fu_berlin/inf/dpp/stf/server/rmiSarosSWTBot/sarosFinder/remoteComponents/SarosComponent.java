package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * Some events e.g. popUp window "Create new XMPP Account"can be triggered by
 * different click_path like clicking mainMenu Saros-> Create Account or
 * clicking toolbarButton connect. So functions to handle such events would be
 * defined in this interface, so that all other saros_feature contained
 * components can use them.
 * 
 * 
 * @author lchen
 */
public interface SarosComponent extends EclipseComponent {

    /**********************************************
     * 
     * action
     * 
     **********************************************/
    /**
     * Confirm the popUp window "create new XMPP account".
     * 
     * @param jid
     *            jid of the new XMPP account
     * @param password
     *            password of the new XMPP account
     * @throws RemoteException
     */
    public void confirmShellCreateNewXMPPAccount(JID jid, String password)
        throws RemoteException;

    /**
     * confirm the wizard "Saros configuration".
     * 
     * @param jid
     *            jid of the new XMPP account
     * @param password
     *            password of the new XMPP account
     * @throws RemoteException
     */
    public void confirmWizardSarosConfiguration(JID jid, String password)
        throws RemoteException;

}
