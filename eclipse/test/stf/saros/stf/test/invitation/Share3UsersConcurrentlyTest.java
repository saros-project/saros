package saros.stf.test.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.client.tester.SarosTester.CARL;

import org.junit.BeforeClass;
import org.junit.Test;
import saros.session.User.Permission;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.stf.shared.Constants.TypeOfCreateProject;
import saros.stf.test.Constants;

public class Share3UsersConcurrentlyTest extends StfTestCase {

  /**
   * Preconditions:
   *
   * <ol>
   *   <li>Alice (Host, Write Access)
   *   <li>Bob (Read-Only Access)
   *   <li>Carl (Read-Only Access)
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
   *   <li>Alice share project with BOB and CARL concurrently.
   *   <li>Alice and BOB leave the session.
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>Alice, Bob and Carl are participants and have {@link Permission#WRITE_ACCESS}.
   *   <li>Alice, Bob and Carl have no {@link Permission}s after leaving the session.
   * </ol>
   *
   * @throws InterruptedException
   */
  @Test
  public void testShareProjectConcurrently() throws Exception, InterruptedException {

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
    Util.buildSessionConcurrently(
        Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT, ALICE, BOB, CARL);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    assertTrue(CARL.superBot().views().sarosView().isInSession());
    assertFalse(ALICE.superBot().views().sarosView().selectUser(CARL.getJID()).hasReadOnlyAccess());
    assertTrue(ALICE.superBot().views().sarosView().selectUser(CARL.getJID()).hasWriteAccess());

    assertTrue(BOB.superBot().views().sarosView().isInSession());
    assertFalse(ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).hasReadOnlyAccess());
    assertTrue(ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).hasWriteAccess());

    assertTrue(ALICE.superBot().views().sarosView().isInSession());

    leaveSessionPeersFirst(ALICE);

    assertFalse(CARL.superBot().views().sarosView().isInSession());

    assertFalse(BOB.superBot().views().sarosView().isInSession());

    assertFalse(ALICE.superBot().views().sarosView().isInSession());
  }
}
