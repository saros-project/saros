package de.fu_berlin.inf.dpp.stf.test.reproducebugs;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.DAVE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.EDNA;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ACCEPT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.CANCEL;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.OK;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_PROBLEM_OCCURRED;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SESSION_INVITATION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class ParallelInvitationWithTerminationByInviteesTest extends
    StfTestCase {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Carl (Read-Only Access)</li>
     * <li>Dave (Read-Only Access)</li>
     * <li>Edna (Read-Only Access)</li>
     * </ol>
     * 
     * 
     */

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB, CARL, DAVE, EDNA);
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice invites everyone else simultaneously.</li>
     * <li>Bob cancels the invitation at the beginning.</li>
     * <li>Carl cancels the invitation after receiving the file list, but before
     * the synchronisation starts.</li>
     * <li>Dave cancels the invitation during the synchronisation.</li>
     * <li>Edna accepts the invitation (using a new project).</li>
     * <li>Edna leaves the session.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Alice is notified of the peer canceling the invitation.</li>
     * <li>Alice is notified of Edna joining the session.</li>
     * <li></li>
     * <li>Alice is notified of Edna leaving the session.</li> *
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void parallelInvitationWihtTerminationByInvitees()
        throws IOException, CoreException, InterruptedException {
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .newC()
            .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1);

        /*
         * build session with BOB, CARL and DAVE simultaneously
         */
        ALICE
            .superBot()
            .views()
            .sarosView()
            .selectNoSessionRunning()
            .shareProjects(Constants.PROJECT1, BOB.getJID(), DAVE.getJID(),
                CARL.getJID(), EDNA.getJID());

        BOB.remoteBot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        IRemoteBotShell shell_BOB = BOB.remoteBot().shell(
            SHELL_SESSION_INVITATION);
        shell_BOB.activate();
        shell_BOB.bot().button(CANCEL).click();

        IRemoteBotShell shell_ALICE = ALICE.remoteBot().shell(
            SHELL_PROBLEM_OCCURRED);
        shell_ALICE.waitUntilActive();
        assertTrue(ALICE.remoteBot().shell(SHELL_PROBLEM_OCCURRED).bot()
            .label(2).getText().matches(BOB.getName() + ".*"));
        shell_ALICE.bot().button(OK).click();

        IRemoteBotShell shell_CARL = CARL.remoteBot().shell(
            SHELL_SESSION_INVITATION);
        CARL.remoteBot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        shell_CARL.activate();
        CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);
        shell_CARL.bot().button(CANCEL).click();

        shell_ALICE.waitUntilActive();
        assertTrue(ALICE.remoteBot().shell(SHELL_PROBLEM_OCCURRED).bot()
            .label(2).getText().matches(CARL.getName() + ".*"));
        shell_ALICE.bot().button(OK).click();

        DAVE.remoteBot().waitUntilShellIsClosed(SHELL_SESSION_INVITATION);
        IRemoteBotShell shell_DAVE = DAVE.remoteBot().shell(
            SHELL_SESSION_INVITATION);
        shell_DAVE.activate();
        DAVE.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);
        shell_DAVE.bot().button(CANCEL).click();

        shell_ALICE.waitUntilActive();
        assertTrue(ALICE.remoteBot().shell(SHELL_PROBLEM_OCCURRED).bot()
            .label(2).getText().matches(DAVE.getName() + ".*"));
        shell_ALICE.bot().button(OK).click();

        EDNA.remoteBot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        EDNA.remoteBot().shell(SHELL_SESSION_INVITATION).activate();
        EDNA.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);
        EDNA.superBot()
            .confirmShellAddProjectWithNewProject(Constants.PROJECT1);
        EDNA.superBot().views().sarosView().leaveSession();
        assertFalse(EDNA.superBot().views().sarosView().isInSession());
        assertFalse(ALICE.superBot().views().sarosView()
            .selectParticipant(EDNA.getJID()).hasReadOnlyAccess());

    }
}
