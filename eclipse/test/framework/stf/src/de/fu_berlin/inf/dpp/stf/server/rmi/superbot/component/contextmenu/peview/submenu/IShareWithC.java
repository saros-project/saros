package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface contains convenience API to perform actions activated by clicking subMenus of
 * contextMenu {@link StfRemoteObject#CM_SHARE_WITH} in the package explorer view. STF users would
 * start off as follows:
 *
 * <pre>
 * //
 * // init alice and bob
 * //
 * initTesters(TypeOfTester.ALICE, Tester.BOB);
 *
 * //
 * // clean up workbench
 * //
 * setUpWorkbench();
 *
 * //
 * // open sarosViews, connect...
 * //
 * setUpSaros();
 *
 * //
 * // alice create a new java project with name Foo_bar
 * //
 * alice.superBot().views().packageExplorerView().tree().newC()
 *     .javaProject(&quot;Foo_bar&quot;);
 *
 * //
 * // alice share the project Foo_bar with bob
 * //
 * alice.superBot().views().packageExplorerView().selectProject(&quot;Foo_bar&quot;)
 *     .shareWith().contact(bob.getJID());
 * </pre>
 *
 * For more information on how to write STF-Tests please read the user guide.
 *
 * @author lchen
 */
public interface IShareWithC extends Remote {

  /**
   * Perform the action share project with multiple contacts which should be activated by clicking
   * the contextMenu Share With-> multiple contacts of the given project in the package explorer
   * view.
   *
   * <p><b>Attention:</b>
   *
   * <ol>
   *   <li>Makes sure, the package explorer view is open and active.
   *   <li>The function treat also the event e.g. popUpWindow activated by clicking the contextMenut
   * </ol>
   *
   * @param inviteeBaseJIDS the base JIDs of the users with whom you want to share your project.
   * @throws RemoteException @Deprecated
   *     <p>FIXME: Can't click the contextMenu
   */
  public void multipleContacts(String projectName, JID... inviteeBaseJIDS) throws RemoteException;

  /**
   * Perform the action share project with the given user which should be activated by clicking the
   * contextMenu Share With-> [user's account] of the given project in the package explorer view.
   *
   * <p><b>Attention:</b>
   *
   * <ol>
   *   <li>Makes sure, the package explorer view is open and active.
   *   <li>The function treat also the event e.g. popUpWindow activated by clicking the contextMenut
   * </ol>
   *
   * @param jid
   * @throws RemoteException
   */
  public void contact(JID jid) throws RemoteException;

  public void addToSarosSession() throws RemoteException;
}
