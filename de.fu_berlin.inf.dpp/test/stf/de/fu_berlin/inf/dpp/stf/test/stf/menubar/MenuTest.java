package de.fu_berlin.inf.dpp.stf.test.stf.menubar;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SUFFIX_JAVA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class MenuTest extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE);
        setUpWorkbench();
    }

    @Override
    @After
    public void after() throws RemoteException {
        announceTestCaseEnd();
        deleteAllProjectsByActiveTesters();
    }

    @Test
    public void testDeleteProject() throws RemoteException {
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProject(Constants.PROJECT1);
        assertTrue(ALICE.superBot().views().packageExplorerView().tree()
            .existsWithRegex(Constants.PROJECT1));
        ALICE.superBot().views().packageExplorerView()
            .selectJavaProject(Constants.PROJECT1).delete();
        assertFalse(ALICE.superBot().views().packageExplorerView().tree()
            .existsWithRegex(Constants.PROJECT1));
    }

    @Test
    public void testDeleteFile() throws RemoteException {
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProject(Constants.PROJECT1);
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .existsWithRegex(Constants.CLS1 + SUFFIX_JAVA));
        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .delete();
        assertFalse(ALICE.superBot().views().packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .existsWithRegex(Constants.CLS1 + SUFFIX_JAVA));
    }

    @Test
    public void testCopyProject() throws RemoteException {
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .project(Constants.PROJECT1);
        assertFalse(ALICE.superBot().views().packageExplorerView().tree()
            .existsWithRegex(Constants.PROJECT2));
        ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1).copy();
        ALICE.superBot().views().packageExplorerView().tree()
            .paste(Constants.PROJECT2);
        assertTrue(ALICE.superBot().views().packageExplorerView().tree()
            .existsWithRegex(Constants.PROJECT2));
    }

    @Test
    public void testDeleteFolder() throws RemoteException {
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProject(Constants.PROJECT1);
        ALICE.superBot().views().packageExplorerView()
            .selectJavaProject(Constants.PROJECT1).newC()
            .folder(Constants.FOLDER1);
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1)
            .existsWithRegex(Constants.FOLDER1));
        ALICE.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1).delete();
        assertFalse(ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1)
            .existsWithRegex(Constants.FOLDER1));
    }

}
