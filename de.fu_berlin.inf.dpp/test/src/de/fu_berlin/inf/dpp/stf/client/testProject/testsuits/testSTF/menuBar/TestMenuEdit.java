package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.menuBar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestMenuEdit extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbenchs();
    }

    @AfterClass
    public static void runAfterClass() {
        //
    }

    @Before
    public void runBeforeEveryTest() {
        //
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        deleteAllProjectsByActiveTesters();
    }

    @Test
    @Ignore
    public void testDeleteProjectUsingGUI() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        assertTrue(alice.fileM.existsProjectNoGUI(PROJECT1));
        alice.editM.deleteProjectNoGUI(PROJECT1);
        assertFalse(alice.fileM.existsProjectNoGUI(PROJECT1));
    }

    @Test
    public void testDeleteFileUsingGUI() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        alice.fileM.newClass(PROJECT1, "pkg", "Cls");
        assertTrue(alice.fileM.existsClassNoGUI(PROJECT1, "pkg", "Cls"));
        alice.pEV.selectClass(PROJECT1, "pkg", "Cls");
        alice.editM.deleteFile();
        assertFalse(alice.fileM.existsClassNoGUI(PROJECT1, "pkg", "Cls"));
    }

    @Test
    public void testCopyProject() throws RemoteException {
        alice.fileM.newProject(PROJECT1);
        assertFalse(alice.fileM.existsProjectNoGUI(PROJECT2));
        alice.pEV.selectProject(PROJECT1);
        alice.editM.copyProject(PROJECT2);
        assertTrue(alice.fileM.existsProjectNoGUI(PROJECT2));
    }

    @Test
    public void testDeleteFolder() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        alice.fileM.newFolder(VIEW_PACKAGE_EXPLORER, FOLDER1, PROJECT1);
        assertTrue(alice.fileM.existsFolderNoGUI(PROJECT1, FOLDER1));
        alice.editM.deleteFolderNoGUI(PROJECT1, FOLDER1);
        assertFalse(alice.fileM.existsFolderNoGUI(PROJECT1, FOLDER1));
    }

}
