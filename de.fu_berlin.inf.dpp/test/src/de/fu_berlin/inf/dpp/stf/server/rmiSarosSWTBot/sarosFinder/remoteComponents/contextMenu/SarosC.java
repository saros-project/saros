package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;

/**
 * This interface contains convenience API to perform a action using the
 * submenus of the contextmenu "Saros" by right clicking on a treeItem(ie,
 * project, class, file...)in the package explorer view. then you can start off
 * as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link Tester} object in your junit-test. (How
 * to do it please look at the javadoc in class {@link TestPattern} or read the
 * user guide in TWiki https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
 * <li>
 * then you can use the object pEV initialized in {@link Tester} to access the
 * API :), e.g.
 * 
 * <pre>
 * alice.pEV.shareProject();
 * </pre>
 * 
 * </li>
 * 
 * @author Lin
 */
public interface SarosC extends Remote {

    /**
     * Perform one of the actions "Share project",
     * "Share project partially (experimental)..." and "Add to session
     * (experimental)..." according to the passed parameter "howToShareProject"
     * which should be activated by clicking the corresponding sub menu of the
     * context menu "Saros" of the given project in the package explorer view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions showed
     * by host side, which are activated or indirectly activated by the clicking
     * one of the sub menus . I mean, after clicking the sub menu e.g.
     * "Share project" you need to treat the following popup window too.</li>
     * 
     * </ol>
     * 
     * 
     * @param howToshareProject
     *            with the parameter you can tell the method how to share your
     *            project with "Share project",
     *            "Share project partially (experimental)..." or "Add to session
     *            (experimental)...".
     * @param inviteeBaseJIDs
     *            the base JIDs of the users with whom you want to share your
     *            project.
     * @throws RemoteException
     * 
     */
    public void shareProjectWith(String howToshareProject,
        String[] inviteeBaseJIDs) throws RemoteException;

    /**
     * Perform the action "Share project" which should be activated by clicking
     * the corresponding sub menu "Share project" of the context menu "Saros" of
     * the given project in the package explorer view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions showed
     * by host side, which are activated or indirectly activated by the clicking
     * one of the sub menus . I mean, after clicking the sub menu
     * "Share project" you need to treat the following popup window too.</li>
     * 
     * </ol>
     * 
     * 
     * @param inviteeBaseJIDS
     *            the base JIDs of the users with whom you want to share your
     *            project.
     * @throws RemoteException
     * 
     */
    public void multipleBuddies(String... inviteeBaseJIDS) throws RemoteException;

}
