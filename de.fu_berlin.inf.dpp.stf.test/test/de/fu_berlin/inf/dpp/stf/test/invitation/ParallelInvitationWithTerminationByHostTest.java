package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.DAVE;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ACCEPT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_ADD_PROJECTS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_INVITATION_CANCELED;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SESSION_INVITATION;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.test.stf.Constants;
import java.io.IOException;
import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelInvitationWithTerminationByHostTest extends StfTestCase {

  /**
   * Preconditions:
   *
   * <ol>
   *   <li>Alice (Host, Write Access)
   *   <li>Bob (Read-Only Access)
   *   <li>Carl (Read-Only Access)
   *   <li>Dave (Read-Only Access)
   * </ol>
   */
  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL, DAVE);
  }

  /**
   * Steps:
   *
   * <ol>
   *   <li>Alice invites everyone else simultaneously.
   *   <li>Alice opens the Progress View and cancels Bob's invitation before Bob accepts.
   *   <li>Carl accepts the invitation but does not choose a target project.
   *   <li>Alice opens the Progress View and cancels Carl's invitation before Carl accepts
   *   <li>Dave accepts the invitation but does not choose a target project.
   *   <li>Alice opens the Progress View and cancels Carl's invitation before Dave accepts
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>
   *   <li>Bob is notified of Alice's canceling the invitation.
   *   <li>
   *   <li>Carl is notified of Alice's canceling the invitation.
   *   <li>
   *   <li>Dave is notified of Alice's canceling the invitation.
   * </ol>
   *
   * @throws CoreException
   * @throws IOException
   * @throws InterruptedException
   */
  @Test
  public void parallelInvitationWithTerminationByHost()
      throws IOException, CoreException, InterruptedException {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    /*
     * as we do not know who gets invited first and we have limited access
     * to the progress view make sure BOBs invitation is at the first place
     */
    ALICE
        .superBot()
        .views()
        .sarosView()
        .selectNoSessionRunning()
        .shareProjects(Constants.PROJECT1, BOB.getJID());

    BOB.remoteBot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
    BOB.remoteBot().shell(SHELL_SESSION_INVITATION).activate();

    ALICE.superBot().views().sarosView().selectContact(CARL.getJID()).addToSarosSession();

    CARL.remoteBot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);

    ALICE.superBot().views().sarosView().selectContact(DAVE.getJID()).addToSarosSession();

    DAVE.remoteBot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);

    // kick BOB
    ALICE.superBot().views().progressView().removeProcess(0);

    BOB.remoteBot().waitLongUntilShellIsOpen(SHELL_INVITATION_CANCELED);
    BOB.remoteBot().shell(SHELL_INVITATION_CANCELED).activate();
    BOB.remoteBot().shell(SHELL_INVITATION_CANCELED).close();

    CARL.remoteBot().shell(SHELL_SESSION_INVITATION).activate();
    CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

    DAVE.remoteBot().shell(SHELL_SESSION_INVITATION).activate();
    DAVE.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

    CARL.remoteBot().waitLongUntilShellIsOpen(SHELL_ADD_PROJECTS);

    DAVE.remoteBot().waitLongUntilShellIsOpen(SHELL_ADD_PROJECTS);

    // stop the session

    ALICE.superBot().views().sarosView().leaveSession();

    // TODO we get an invitation canceled message dialog which is
    // completely wrong
  }
}
