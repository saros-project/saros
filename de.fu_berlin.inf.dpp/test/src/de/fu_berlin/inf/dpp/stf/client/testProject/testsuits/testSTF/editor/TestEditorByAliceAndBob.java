package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.editor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestEditorByAliceAndBob extends STFTest {
    private static final Logger log = Logger
        .getLogger(TestEditorByAliceAndBob.class);

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbenchs();
        setUpSaros();
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.editor.closeJavaEditorWithSave(CLS1);
        setUpSessionByDefault(alice, bob);
    }

    @AfterClass
    public static void runAfterClass() throws RemoteException,
        InterruptedException {
        alice.leaveSessionHostFirstDone(bob);
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {
        // resetFollowModel(bob);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        alice.editor.closejavaEditorWithoutSave(CLS1);
    }

    @Test
    public void isJavaEditorOpen() throws RemoteException {
        alice.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        assertTrue(alice.editor.isJavaEditorOpen(CLS1));
        alice.editor.closeJavaEditorWithSave(CLS1);
        assertFalse(alice.editor.isJavaEditorOpen(CLS1));
    }

    @Test(expected = AssertionError.class)
    public void isJavaEditorOpenWithAssertError() throws RemoteException {
        alice.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        assertFalse(alice.editor.isJavaEditorOpen(CLS1));
    }

    @Test
    public void isEditorOpen() throws RemoteException {
        alice.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        assertTrue(alice.editor.isEditorOpen(CLS1 + SUFIX_JAVA));
        alice.editor.closeEditorWithSave(CLS1 + SUFIX_JAVA);
        assertFalse(alice.editor.isEditorOpen(CLS1 + SUFIX_JAVA));
    }

    @Test
    public void waitUntilBobJavaEditorOpen() throws RemoteException,
        InterruptedException {
        alice.followedBy(bob);
        assertTrue(bob.sarosSessionV.isInFollowModeNoGUI());
        alice.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        bob.editor.waitUntilJavaEditorOpen(CLS1);
        assertTrue(bob.editor.isJavaEditorOpen(CLS1));
    }

    @Test
    public void waitUntilBobJavaEditorActive() throws RemoteException,
        InterruptedException {
        alice.followedBy(bob);
        alice.fileM.newClass(PROJECT1, PKG1, CLS2);
        assertTrue(bob.sarosSessionV.isInFollowModeNoGUI());
        bob.editor.waitUntilJavaEditorActive(CLS2);
        assertTrue(bob.editor.isJavaEditorActive(CLS2));
        assertFalse(bob.editor.isJavaEditorActive(CLS1));
        alice.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        bob.editor.waitUntilJavaEditorActive(CLS1);
        assertTrue(bob.editor.isJavaEditorActive(CLS1));
        assertFalse(bob.editor.isJavaEditorActive(CLS2));
    }

    @Test
    public void waitUntilBobJavaEditorClosed() throws RemoteException,
        InterruptedException {
        alice.followedBy(bob);
        alice.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        bob.editor.waitUntilJavaEditorActive(CLS1);
        assertTrue(bob.editor.isJavaEditorActive(CLS1));
        assertFalse(bob.editor.isJavaEditorActive(CLS2));
        alice.editor.closejavaEditorWithoutSave(CLS1);
        bob.editor.waitUntilJavaEditorClosed(CLS1);
        assertFalse(bob.editor.isJavaEditorOpen(CLS1));

    }

}
