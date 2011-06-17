package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;

public class TestInvitation extends StfTestCase {
    @BeforeClass
    public static void beforeClass() throws Exception {
        initTesters(ALICE, BOB);
        setUpWorkbench();
        setUpSaros();
    }

    @Override
    @Before
    public void setUp() throws RemoteException {
        super.setUp();
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses("foo", "bar", "HelloAlice");

        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses("foo1", "bar", "HelloBob");

        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses("foo2", "bar", "HelloCarl");
    }

    @Override
    @After
    public void tearDown() throws RemoteException {
        announceTestCaseEnd();
        leaveSessionHostFirst(ALICE);
        ALICE.remoteBot().closeAllEditors();
        BOB.remoteBot().closeAllEditors();
        deleteAllProjectsByActiveTesters();
    }

    @Test
    public void testShareMultipleWithBobAndCarlSequencetially()
        throws RemoteException {

        Util.buildSessionSequentially("foo", TypeOfCreateProject.NEW_PROJECT,
            ALICE, BOB);

        BOB.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo", "bar", "HelloAlice");

        Util.addProjectToSessionSequentially("foo1",
            TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

        BOB.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo1", "bar", "HelloBob");

        Util.addProjectToSessionSequentially("foo2",
            TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

        BOB.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo2", "bar", "HelloCarl");

        BOB.superBot().views().packageExplorerView()
            .selectClass("foo", "bar", "HelloAlice").open();

        BOB.remoteBot().editor("HelloAlice.java").waitUntilIsActive();

        BOB.superBot().views().packageExplorerView()
            .selectClass("foo1", "bar", "HelloBob").open();

        BOB.remoteBot().editor("HelloBob.java").waitUntilIsActive();

        BOB.superBot().views().packageExplorerView()
            .selectClass("foo2", "bar", "HelloCarl").open();

        BOB.remoteBot().editor("HelloCarl.java").waitUntilIsActive();

        ALICE.superBot().views().packageExplorerView()
            .selectClass("foo", "bar", "HelloAlice").open();

        ALICE.remoteBot().editor("HelloAlice.java").waitUntilIsActive();

        ALICE.remoteBot().editor("HelloAlice.java").typeText("testtext");

        ALICE.superBot().views().packageExplorerView()
            .selectClass("foo1", "bar", "HelloBob").open();

        ALICE.remoteBot().editor("HelloBob.java").waitUntilIsActive();

        ALICE.remoteBot().editor("HelloBob.java").typeText("testtext");

        ALICE.superBot().views().packageExplorerView()
            .selectClass("foo2", "bar", "HelloCarl").open();

        ALICE.remoteBot().editor("HelloCarl.java").waitUntilIsActive();

        ALICE.remoteBot().editor("HelloCarl.java").typeText("testtext");

        ALICE.remoteBot().sleep(2000);

        assertEquals(ALICE.remoteBot().editor("HelloAlice.java").getText(), BOB
            .remoteBot().editor("HelloAlice.java").getText());
        assertEquals(ALICE.remoteBot().editor("HelloBob.java").getText(), BOB
            .remoteBot().editor("HelloBob.java").getText());
        assertEquals(ALICE.remoteBot().editor("HelloCarl.java").getText(), BOB
            .remoteBot().editor("HelloCarl.java").getText());

    }

}
