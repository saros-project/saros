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
    public void testDeleteProjectUsingGUI() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        assertTrue(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
        alice.noBot().deleteProjectNoGUI(PROJECT1);
        assertFalse(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
    }

    @Test
    public void testDeleteFileUsingGUI() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        alice.sarosBot().file().newClass(PROJECT1, "pkg", "Cls");
        assertTrue(alice.sarosBot().state()
            .existsClassNoGUI(PROJECT1, "pkg", "Cls"));
        alice.sarosBot().packageExplorerView()
            .selectClass(PROJECT1, "pkg", "Cls").delete();
        assertFalse(alice.sarosBot().state()
            .existsClassNoGUI(PROJECT1, "pkg", "Cls"));
    }

    @Test
    public void testCopyProject() throws RemoteException {
        alice.sarosBot().file().newProject(PROJECT1);
        assertFalse(alice.sarosBot().state().existsProjectNoGUI(PROJECT2));
        alice.sarosBot().packageExplorerView().selectProject(PROJECT1).copy();
        alice.sarosBot().packageExplorerView().tree().paste(PROJECT2);
        assertTrue(alice.sarosBot().state().existsProjectNoGUI(PROJECT2));
    }

    @Test
    public void testDeleteFolder() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        alice.sarosBot().file().newFolder(PROJECT1, FOLDER1);
        assertTrue(alice.sarosBot().state()
            .existsFolderNoGUI(PROJECT1, FOLDER1));
        alice.noBot().deleteFolderNoGUI(PROJECT1, FOLDER1);
        assertFalse(alice.sarosBot().state()
            .existsFolderNoGUI(PROJECT1, FOLDER1));
    }

}
