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
        setUpWorkbenchs();
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
        alice.fileM.newJavaProject(PROJECT1);
        alice.fileM.newFolder(PROJECT1, FOLDER1);
        alice.fileM.newFile(VIEW_PACKAGE_EXPLORER, PROJECT1, FOLDER1, FILE1);
        assertTrue(alice.editor.isEditorOpen(FILE1));
        alice.editor.closeEditorWithSave(FILE1);
        assertFalse(alice.editor.isEditorOpen(FILE1));
        alice.openC.openFile(VIEW_PACKAGE_EXPLORER, PROJECT1, FOLDER1, FILE1);
        assertTrue(alice.editor.isEditorOpen(FILE1));
        alice.pEV.selectFile(PROJECT1, FOLDER1, FILE1);
        alice.editM.deleteFile();
        assertFalse(alice.editor.isEditorOpen(FILE1));
    }

    @Test
    public void testOpenClass() throws RemoteException {
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        assertTrue(alice.editor.isJavaEditorOpen(CLS1));
        alice.editor.closeJavaEditorWithSave(CLS1);
        assertFalse(alice.editor.isJavaEditorOpen(CLS1));
        alice.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        assertTrue(alice.editor.isJavaEditorOpen(CLS1));
        alice.pEV.selectClass(PROJECT1, PKG1, CLS1);
        alice.editM.deleteFile();
        assertFalse(alice.editor.isJavaEditorOpen(CLS1));
    }

    @Test
    public void testOpenClassWith() throws RemoteException {
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        assertTrue(alice.editor.isJavaEditorOpen(CLS1));
        alice.editor.closeJavaEditorWithSave(CLS1);
        assertFalse(alice.editor.isJavaEditorOpen(CLS1));
        alice.openC.openClassWith(VIEW_PACKAGE_EXPLORER,
            CM_OPEN_WITH_TEXT_EDITOR, PROJECT1, PKG1, CLS1);
        assertTrue(alice.editor.isJavaEditorOpen(CLS1));
        alice.pEV.selectClass(PROJECT1, PKG1, CLS1);
        alice.editM.deleteFile();
        assertFalse(alice.editor.isJavaEditorOpen(CLS1));
    }

    @Test
    public void testOpenFileWith() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        alice.fileM.newFolder(PROJECT1, FOLDER1);
        alice.fileM.newFile(VIEW_PACKAGE_EXPLORER, PROJECT1, FOLDER1, FILE1);
        alice.editor.closeEditorWithSave(FILE1);
        alice.openC.openFileWith(VIEW_PACKAGE_EXPLORER,
            CM_OPEN_WITH_TEXT_EDITOR, PROJECT1, FOLDER1, FILE1);
        assertTrue(alice.editor.isEditorOpen(FILE1));
        alice.pEV.selectFile(PROJECT1, FOLDER1, FILE1);
        alice.editM.deleteFile();
        assertFalse(alice.editor.isEditorOpen(FILE1));
    }

    @Test
    @Ignore("Can't close the external editor")
    public void testOpenFileWithSystemEditor() throws RemoteException {
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.openC.openClassWith(VIEW_PACKAGE_EXPLORER,
            CM_OPEN_WITH_TEXT_EDITOR, PROJECT1, PKG1, CLS1);
        alice.openC.openClassWithSystemEditorNoGUI(PROJECT1, PKG1, CLS1);
    }

}
