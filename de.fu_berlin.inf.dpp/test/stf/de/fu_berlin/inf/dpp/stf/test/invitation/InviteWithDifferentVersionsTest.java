package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.NO;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SESSION_INVITATION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.annotation.TestLink;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;

@TestLink(id = "Saros-132_version_mismatch")
public class InviteWithDifferentVersionsTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB);
    }

    @Test
    public void testInvitationWithDifferentVersions() throws Exception {
        BOB.superBot().views().sarosView().disconnect();

        BOB.superBot().internal().changeSarosVersion("1.1.1");

        BOB.superBot().views().sarosView()
            .connectWith(BOB.getJID(), BOB.getPassword());

        BOB.superBot().views().sarosView().waitUntilIsConnected();

        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses("foo", "bar", "VersionMismatch");

        ALICE.superBot().menuBar().saros().shareProjects("foo", BOB.getJID());

        Thread.sleep(5000);
        List<String> shellNamesAlice = ALICE.remoteBot().getOpenShellNames();
        List<String> shellNamesBob = BOB.remoteBot().getOpenShellNames();

        boolean foundAlice = false;

        for (String shellName : shellNamesAlice) {
            if (shellName.matches(".*Saros Version.*")) {
                foundAlice = true;
                IRemoteBotShell shell = ALICE.remoteBot().shell(shellName);
                shell.activate();
                shell.bot().button(NO).click();
                shell.waitShortUntilIsClosed();
                break;
            }
        }

        boolean foundBob = false;

        for (String shellName : shellNamesBob) {
            if (shellName.equals(SHELL_SESSION_INVITATION)) {
                foundBob = true;
                break;
            }
        }

        assertTrue("Alice version mismatch warning shell was not open",
            foundAlice);
        assertFalse(
            "Bobs invitation window is open although he has an invalid version",
            foundBob);
    }

    @AfterClass
    public static void resetSarosVersion() throws RemoteException {
        BOB.superBot().internal().resetSarosVersion();
    }
}
