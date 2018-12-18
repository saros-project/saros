package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.OK;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SESSION_INVITATION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.stf.Constants;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.junit.BeforeClass;
import org.junit.Test;

public class Share3UsersLeavingSessionTest extends StfTestCase {

  /**
   * Preconditions:
   *
   * <ol>
   *   <li>Alice (Host, Write Access)
   *   <li>Bob (Read-Only Access)
   * </ol>
   */
  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  /**
   * Steps:
   *
   * <ol>
   *   <li>Alice share project with Bob.
   *   <li>Alice invites Carl.
   *   <li>Alice and Bob leave the session.
   *   <li>Carl accepts the session.
   * </ol>
   *
   * Result: Alice, Bob and Carl are not in a session.
   */
  @Test
  public void testShare3UsersLeavingSession() throws Exception {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    Util.buildSessionSequentially(Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    assertTrue(BOB.superBot().views().sarosView().isInSession());
    assertTrue(ALICE.superBot().views().sarosView().isInSession());

    ALICE.superBot().views().sarosView().selectSession().addContactsToSession(CARL.getBaseJid());

    CARL.remoteBot().waitLongUntilShellIsOpen(SHELL_SESSION_INVITATION);

    ALICE.superBot().views().sarosView().leaveSession();

    ALICE.superBot().views().sarosView().waitUntilIsNotInSession();
    BOB.superBot().views().sarosView().waitUntilIsNotInSession();

    Thread.sleep(2000);

    try {
      CARL.remoteBot().shell("Invitation Canceled").confirm(OK);
    } catch (WidgetNotFoundException e) {
      fail("Invitation Canceled is not open: " + e.getMessage());
    }
    assertFalse(
        CARL + " is in a closed session", CARL.superBot().views().sarosView().isInSession());
    assertFalse(
        ALICE + " is in a closed session", ALICE.superBot().views().sarosView().isInSession());
    assertFalse(BOB + " is in a closed session", BOB.superBot().views().sarosView().isInSession());
  }
}
