package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;

public class InviteAndLeaveStressTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB, CARL);
    }

    @Before
    public void beforeEveryTest() throws RemoteException {
        clearWorkspaces();
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses("foo", "bar", "HelloWorld");
    }

    @Test
    public void testAliceAlwaysLeavesSessionFirst() throws Exception {

        for (int i = 0; i < 10; i++) {
            Util.buildSessionConcurrently("foo",
                TypeOfCreateProject.NEW_PROJECT, ALICE, BOB, CARL);

            leaveSessionHostFirst(ALICE);

        }

        BOB.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo", "bar", "HelloWorld");

        CARL.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo", "bar", "HelloWorld");

        String contentAliceHelloWorld = ALICE.superBot().views()
            .packageExplorerView()
            .getFileContent("foo/src/bar/HelloWorld.java");
        String contentBobHelloWorld = BOB.superBot().views()
            .packageExplorerView()
            .getFileContent("foo/src/bar/HelloWorld.java");

        String contentCarlHelloWorld = BOB.superBot().views()
            .packageExplorerView()
            .getFileContent("foo/src/bar/HelloWorld.java");

        assertEquals(contentAliceHelloWorld, contentBobHelloWorld);
        assertEquals(contentBobHelloWorld, contentCarlHelloWorld);
    }
}
