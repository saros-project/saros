package de.fu_berlin.inf.dpp.stf.test.session;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;

public class ShareMultipleProjectsTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB, CARL);
    }

    @Before
    public void beforeEveryTest() throws RemoteException {

        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses("foo", "bar", "HelloAlice");

        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses("foo1", "bar", "HelloBob");

        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses("foo2", "bar", "HelloCarl");
    }

    @After
    public void afterEveryTest() throws RemoteException {

        leaveSessionHostFirst(ALICE);
        clearWorkspaces();
    }

    @Test
    public void testShareMultipleWithBobAndCarlSequencetially()
        throws RemoteException {

        Util.buildSessionConcurrently("foo", TypeOfCreateProject.NEW_PROJECT,
            ALICE, BOB, CARL);

        BOB.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo", "bar", "HelloAlice");

        CARL.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo", "bar", "HelloAlice");

        Util.addProjectToSessionSequentially("foo1",
            TypeOfCreateProject.NEW_PROJECT, ALICE, BOB, CARL);

        BOB.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo1", "bar", "HelloBob");

        CARL.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo1", "bar", "HelloBob");

        Util.addProjectToSessionSequentially("foo2",
            TypeOfCreateProject.NEW_PROJECT, ALICE, BOB, CARL);

        BOB.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo2", "bar", "HelloCarl");

        CARL.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo2", "bar", "HelloCarl");

    }

    @Test
    public void testShareSameDocumentManyTimesDuringSynchronizing()
        throws RemoteException {

        Util.buildSessionConcurrently("foo", TypeOfCreateProject.NEW_PROJECT,
            ALICE, BOB, CARL);

        for (int i = 0; i < 10; i++)
            Util.addProjectToSessionSequentially("foo2",
                TypeOfCreateProject.NEW_PROJECT, ALICE, BOB, CARL);

        Util.addProjectToSessionSequentially("foo1",
            TypeOfCreateProject.NEW_PROJECT, ALICE, BOB, CARL);

    }

}
