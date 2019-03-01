package saros.stf.test.roster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.shared.Constants.FINISH;
import static saros.stf.shared.Constants.LABEL_XMPP_JABBER_ID;
import static saros.stf.shared.Constants.NO;
import static saros.stf.shared.Constants.SHELL_ADD_CONTACT_WIZARD;
import static saros.stf.shared.Constants.SHELL_SERVER_NOT_FOUND;
import static saros.stf.shared.Constants.TB_ADD_NEW_CONTACT;
import static saros.stf.shared.Constants.VIEW_SAROS;

import java.rmi.RemoteException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotShell;

public class HandleContactsTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @After
  public void afterEveryTest() throws Exception {
    resetContacts();
  }

  /**
   * Steps:
   *
   * <ol>
   *   <li>BOB deletes contact ALICE.
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>BOB and ALICE are not each other's contacts.
   * </ol>
   *
   * @throws RemoteException
   */
  @Test
  public void testBobRemoveContactAlice() throws Exception {
    assertTrue(ALICE.superBot().views().sarosView().isInContactList(BOB.getJID()));
    assertTrue(BOB.superBot().views().sarosView().isInContactList(ALICE.getJID()));
    Util.removeTestersFromContactList(BOB, ALICE);
    assertFalse(BOB.superBot().views().sarosView().isInContactList(ALICE.getJID()));
    assertFalse(ALICE.superBot().views().sarosView().isInContactList(BOB.getJID()));
  }

  /**
   * Steps:
   *
   * <ol>
   *   <li>ALICE deletes contact BOB.
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>BOB and ALICE are not each other's contacts.
   * </ol>
   *
   * @throws RemoteException
   */
  @Test
  public void testAliceRemoveContactBob() throws Exception {
    assertTrue(ALICE.superBot().views().sarosView().isInContactList(BOB.getJID()));
    assertTrue(BOB.superBot().views().sarosView().isInContactList(ALICE.getJID()));
    Util.removeTestersFromContactList(ALICE, BOB);
    assertFalse(BOB.superBot().views().sarosView().isInContactList(ALICE.getJID()));
    assertFalse(ALICE.superBot().views().sarosView().isInContactList(BOB.getJID()));
  }

  /**
   * Steps:
   *
   * <ol>
   *   <li>ALICE deletes contact BOB first and then adds BOB.
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>BOB and ALICE are each other's contacts.
   * </ol>
   *
   * @throws RemoteException
   */
  @Test
  public void testAliceAddContactBob() throws Exception {
    Util.removeTestersFromContactList(ALICE, BOB);
    Util.addTestersToContactList(ALICE, BOB);
    assertTrue(BOB.superBot().views().sarosView().isInContactList(ALICE.getJID()));
    assertTrue(ALICE.superBot().views().sarosView().isInContactList(BOB.getJID()));
  }

  /**
   * Steps:
   *
   * <ol>
   *   <li>BOB deletes contact ALICE first and then adds ALICE again.
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>BOB and ALICE are each other's contacts.
   * </ol>
   *
   * @throws RemoteException
   */
  @Test
  public void testBobAddContactAlice() throws Exception {
    Util.removeTestersFromContactList(BOB, ALICE);
    Util.addTestersToContactList(BOB, ALICE);
    assertTrue(BOB.superBot().views().sarosView().isInContactList(ALICE.getJID()));
    assertTrue(ALICE.superBot().views().sarosView().isInContactList(BOB.getJID()));
  }

  /**
   * Steps:
   *
   * <ol>
   *   <li>ALICE clicks toolbar button "Add a new contact".
   *   <li>ALICE enters invalid contact name in the popup window "New contact"
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>ALICE should get error message "Contact look up failed".
   * </ol>
   *
   * @throws RemoteException
   */
  @Test
  public void testAddNoValidContact() throws RemoteException {
    ALICE.remoteBot().view(VIEW_SAROS).toolbarButton(TB_ADD_NEW_CONTACT).click();

    ALICE.remoteBot().waitUntilShellIsOpen(SHELL_ADD_CONTACT_WIZARD);
    IRemoteBotShell shell = ALICE.remoteBot().shell(SHELL_ADD_CONTACT_WIZARD);
    shell.activate();
    shell.bot().comboBoxWithLabel(LABEL_XMPP_JABBER_ID).setText("BOB@bla");
    shell.bot().button(FINISH).click();
    ALICE.remoteBot().waitUntilShellIsOpen(SHELL_SERVER_NOT_FOUND);
    assertTrue(ALICE.remoteBot().shell(SHELL_SERVER_NOT_FOUND).isActive());
    ALICE.remoteBot().shell(SHELL_SERVER_NOT_FOUND).confirm(NO);
  }
}
