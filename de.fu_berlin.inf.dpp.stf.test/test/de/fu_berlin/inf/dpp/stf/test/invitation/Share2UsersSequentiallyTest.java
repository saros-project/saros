package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;
import org.junit.BeforeClass;
import org.junit.Test;

public class Share2UsersSequentiallyTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  /**
   * Steps:
   *
   * <ol>
   *   <li>Alice share project with BOB.
   *   <li>Alice and BOB leave the session.
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>Alice and Bob are participants and have both {@link Permission#WRITE_ACCESS}.
   *   <li>Alice and BOB have no {@link Permission}s after leaving the session.
   * </ol>
   */
  @Test
  public void testAliceShareProjectWithBobSequentially() throws Exception {
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

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    assertFalse(ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).hasReadOnlyAccess());

    assertTrue(ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).hasWriteAccess());

    leaveSessionPeersFirst(ALICE);

    assertFalse(BOB.superBot().views().sarosView().isInSession());
    assertFalse(ALICE.superBot().views().sarosView().isInSession());
  }
}
