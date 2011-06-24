package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;

import java.rmi.RemoteException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;

public class InviteWithDifferentVersionsTest extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE, BOB);
        setUpWorkbench();
        setUpSaros();
    }

    @Test
    public void testInvitationWithDifferentVersions() throws RemoteException {
        // BOB.superBot().views().sarosView().disconnect();
        // BOB.superBot().views().sarosView().waitUntilIsDisconnected();
        BOB.superBot().internal().changeSarosVersion("1.1.1");

        BOB.superBot().views().sarosView()
            .connectWith(BOB.getJID(), BOB.getPassword());

        BOB.superBot().views().sarosView().waitUntilIsConnected();

        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses("foo", "bar", "VersionMismatch");

        ALICE.superBot().menuBar().saros().shareProjects("foo", BOB.getJID());

        ALICE.remoteBot().sleep(10000);

    }

    @AfterClass
    public static void resetSarosVersion() throws RemoteException {
        BOB.superBot().internal().resetSarosVersion();
    }
}
