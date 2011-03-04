package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.AbstractTester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;

/**
 * This interface contains convenience API to perform a action using the
 * submenus of the contextmenu "Saros" by right clicking on a treeItem(ie,
 * project, class, file...)in the package explorer view. then you can start off
 * as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link AbstractTester} object in your
 * junit-test. (How to do it please look at the javadoc in class
 * {@link TestPattern} or read the user guide in TWiki
 * https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
 * <li>
 * then you can use the object pEV initialized in {@link AbstractTester} to
 * access the API :), e.g.
 * 
 * <pre>
 * alice.pEV.shareProject();
 * </pre>
 * 
 * </li>
 * 
 * @author Lin
 */
public interface ShareWithC extends Remote {

    /**
     * Perform the action share project with multiple buddies which should be
     * activated by clicking the contextMenu Share With-> multiple buddies of
     * the given project in the package explorer view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function treat also the event e.g. popUpWindow activated by
     * clicking the contextMenut</li>
     * </ol>
     * 
     * @param inviteeBaseJIDS
     *            the base JIDs of the users with whom you want to share your
     *            project.
     * @throws RemoteException
     * 
     * @Deprecated
     * 
     *             FIXME: Can't click the contextMenu
     */
    public void multipleBuddies(String projectName, JID... inviteeBaseJIDS)
        throws RemoteException;

    /**
     * Perform the action share project with the given user which should be
     * activated by clicking the contextMenu Share With-> [user's account] of
     * the given project in the package explorer view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function treat also the event e.g. popUpWindow activated by
     * clicking the contextMenut</li>
     * </ol>
     * 
     * @param jid
     * @throws RemoteException
     */
    public void buddy(JID jid) throws RemoteException;
}
