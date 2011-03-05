package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.menuBar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestMenuEdit extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        deleteAllProjectsByActiveTesters();
    }

    @Test
    @Ignore
    public void testDeleteProject() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        assertTrue(alice.superBot().views().packageExplorerView().tree()
            .existsWithRegex(PROJECT1));
        alice.superBot().views().packageExplorerView()
            .selectJavaProject(PROJECT1).delete();
        assertFalse(alice.superBot().views().packageExplorerView().tree()
            .existsWithRegex(PROJECT1));
    }

    @Test
    public void testDeleteFile() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.superBot().views().packageExplorerView().tree().newC()
            .cls(PROJECT1, "pkg", "Cls");
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, "pkg").existsWithRegex("Cls" + SUFFIX_JAVA));
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, "pkg", "Cls").delete();
        assertFalse(alice.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, "pkg").existsWithRegex("Cls" + SUFFIX_JAVA));
    }

    @Test
    public void testCopyProject() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .project(PROJECT1);
        assertFalse(alice.superBot().views().packageExplorerView().tree()
            .existsWithRegex(PROJECT2));
        alice.superBot().views().packageExplorerView().selectProject(PROJECT1)
            .copy();
        alice.superBot().views().packageExplorerView().tree().paste(PROJECT2);
        assertTrue(alice.superBot().views().packageExplorerView().tree()
            .existsWithRegex(PROJECT2));
    }

    @Test
    public void testDeleteFolder() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.superBot().views().packageExplorerView()
            .selectJavaProject(PROJECT1).newC().folder(FOLDER1);
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectProject(PROJECT1).existsWithRegex(FOLDER1));
        alice.superBot().views().packageExplorerView()
            .selectFolder(PROJECT1, FOLDER1).delete();
        assertFalse(alice.superBot().views().packageExplorerView()
            .selectProject(PROJECT1).existsWithRegex(FOLDER1));
    }

}
