package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.contextMenu;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestContextMenuOpen extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        deleteAllProjectsByActiveTesters();
    }

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "Open"
     * 
     **********************************************/
    @Test
    public void testOpenFile() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.superBot().views().packageExplorerView().selectProject(PROJECT1)
            .newC().folder(FOLDER1);
        alice.superBot().views().packageExplorerView()
            .selectFolder(PROJECT1, FOLDER1).newC().file(FILE1);
        assertTrue(alice.remoteBot().isEditorOpen(FILE1));
        alice.remoteBot().editor(FILE1).closeWithSave();
        assertFalse(alice.remoteBot().isEditorOpen(FILE1));
        alice.superBot().views().packageExplorerView()
            .selectFile(PROJECT1, FOLDER1, FILE1).open();

        assertTrue(alice.remoteBot().isEditorOpen(FILE1));

        alice.superBot().views().packageExplorerView()
            .selectFile(PROJECT1, FOLDER1, FILE1).delete();
        assertFalse(alice.remoteBot().isEditorOpen(FILE1));
    }

    @Test
    public void testOpenClass() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS1);
        assertTrue(alice.remoteBot().isEditorOpen(CLS1_SUFFIX));
        alice.remoteBot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        assertFalse(alice.remoteBot().isEditorOpen(CLS1_SUFFIX));
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        assertTrue(alice.remoteBot().isEditorOpen(CLS1_SUFFIX));

        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).delete();
        assertFalse(alice.remoteBot().isEditorOpen(CLS1_SUFFIX));
    }

    @Test
    public void testOpenClassWith() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS1);
        assertTrue(alice.remoteBot().isEditorOpen(CLS1_SUFFIX));
        alice.remoteBot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        assertFalse(alice.remoteBot().isEditorOpen(CLS1_SUFFIX));
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1)
            .openWith(CM_OPEN_WITH_TEXT_EDITOR);

        assertTrue(alice.remoteBot().isEditorOpen(CLS1_SUFFIX));

        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).delete();
        assertFalse(alice.remoteBot().isEditorOpen(CLS1_SUFFIX));
    }

    @Test
    public void testOpenFileWith() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.superBot().views().packageExplorerView().selectProject(PROJECT1)
            .newC().folder(FOLDER1);
        alice.superBot().views().packageExplorerView()
            .selectFolder(PROJECT1, FOLDER1).newC().file(FILE1);
        alice.remoteBot().editor(FILE1).closeWithSave();
        alice.superBot().views().packageExplorerView()
            .selectFile(PROJECT1, FOLDER1, FILE1)
            .openWith(CM_OPEN_WITH_TEXT_EDITOR);

        assertTrue(alice.remoteBot().isEditorOpen(FILE1));

        alice.superBot().views().packageExplorerView()
            .selectFile(PROJECT1, FOLDER1, FILE1).delete();
        assertFalse(alice.remoteBot().isEditorOpen(FILE1));
    }

    @Test
    @Ignore("Can't close the external editor")
    public void testOpenFileWithSystemEditor() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1)
            .openWith(CM_OPEN_WITH_TEXT_EDITOR);
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1)
            .openWith(CM_OPEN_WITH_SYSTEM_EDITOR);

    }
}
