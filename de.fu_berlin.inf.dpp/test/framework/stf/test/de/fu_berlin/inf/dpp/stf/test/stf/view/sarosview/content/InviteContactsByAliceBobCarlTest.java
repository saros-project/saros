package de.fu_berlin.inf.dpp.stf.test.stf.view.sarosview.content;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;
import java.rmi.RemoteException;
import org.junit.BeforeClass;
import org.junit.Test;

public class InviteContactsByAliceBobCarlTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  /**
   * Steps:
   *
   * <p>1. Alice share session with Bob.
   *
   * <p>2. Alice invite Carl.
   *
   * <p>Result:
   *
   * <ol>
   *   <li>Carl is in the session
   * </ol>
   *
   * @throws RemoteException
   * @throws InterruptedException
   */
  @Test
  public void inviteContact() throws Exception {
    Util.setUpSessionWithJavaProjectAndClass(
        Constants.PROJECT1, Constants.PKG1, Constants.CLS1, ALICE, BOB);

    assertFalse(CARL.superBot().views().sarosView().isInSession());

    ALICE.superBot().views().sarosView().selectContact(CARL.getJID()).addToSarosSession();

    CARL.superBot()
        .confirmShellSessionInvitationAndShellAddProject(
            Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT);

    CARL.superBot().views().sarosView().waitUntilIsInSession();

    ALICE.superBot().views().packageExplorerView().waitUntilResourceIsShared(Constants.PROJECT1);
    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared(Constants.PROJECT1);
    CARL.superBot().views().packageExplorerView().waitUntilResourceIsShared(Constants.PROJECT1);

    assertTrue(ALICE.superBot().views().sarosView().isInSession());
    assertTrue(BOB.superBot().views().sarosView().isInSession());
    assertTrue(CARL.superBot().views().sarosView().isInSession());
  }
}
