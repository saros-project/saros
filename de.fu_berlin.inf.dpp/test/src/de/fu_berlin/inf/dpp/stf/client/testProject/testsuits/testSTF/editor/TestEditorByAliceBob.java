package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.editor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestEditorByAliceBob extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbench();
        setUpSaros();
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
        if (alice.bot().isEditorOpen(CLS1 + SUFFIX_JAVA))
            alice.bot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        if (alice.bot().isEditorOpen(CLS1 + SUFFIX_JAVA))
            alice.bot().editor(CLS1 + SUFFIX_JAVA).closeWithoutSave();
    }

    @Test
    public void isJavaEditorOpen() throws RemoteException {
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        assertTrue(alice.bot().isEditorOpen(CLS1_SUFFIX));
        alice.bot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        assertFalse(alice.bot().isEditorOpen(CLS1_SUFFIX));
    }

    @Test(expected = AssertionError.class)
    public void isJavaEditorOpenWithAssertError() throws RemoteException {
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        assertFalse(alice.bot().isEditorOpen(CLS1_SUFFIX));
    }

    @Test
    public void isEditorOpen() throws RemoteException {
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        assertTrue(alice.bot().isEditorOpen(CLS1 + SUFFIX_JAVA));
        alice.bot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        assertFalse(alice.bot().isEditorOpen(CLS1 + SUFFIX_JAVA));
    }

    @Test
    public void waitUntilBobJavaEditorOpen() throws RemoteException {

        bob.superBot().views().sarosView().selectParticipant(alice.getJID())
            .followParticipant();
        assertTrue(bob.superBot().views().sarosView()
            .selectParticipant(alice.getJID()).isFollowing());
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        bob.bot().waitUntilEditorOpen(CLS1_SUFFIX);
        assertTrue(bob.bot().isEditorOpen(CLS1_SUFFIX));
    }

    @Test
    public void waitUntilBobJavaEditorActive() throws RemoteException,
        InterruptedException {
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        setFollowMode(alice, bob);
        assertTrue(bob.superBot().views().sarosView()
            .selectParticipant(alice.getJID()).isFollowing());
        assertTrue(bob.bot().editor(CLS1_SUFFIX).isActive());

        alice.superBot().views().packageExplorerView().tree().newC()
            .cls(PROJECT1, PKG1, CLS2);
        bob.bot().editor(CLS2_SUFFIX).waitUntilIsActive();
        assertTrue(bob.bot().editor(CLS2_SUFFIX).isActive());
        assertFalse(bob.bot().editor(CLS1_SUFFIX).isActive());

        alice.bot().editor(CLS1_SUFFIX).show();
        alice.bot().editor(CLS1_SUFFIX).waitUntilIsActive();
        assertTrue(alice.bot().editor(CLS1_SUFFIX).isActive());
        bob.bot().editor(CLS1_SUFFIX).waitUntilIsActive();
        assertTrue(bob.bot().editor(CLS1_SUFFIX).isActive());
        assertFalse(bob.bot().editor(CLS2_SUFFIX).isActive());
    }

    @Test
    public void waitUntilBobJavaEditorClosed() throws RemoteException,
        InterruptedException {
        setFollowMode(alice, bob);
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        bob.bot().editor(CLS1_SUFFIX).waitUntilIsActive();
        assertTrue(bob.bot().editor(CLS1_SUFFIX).isActive());

        alice.bot().editor(CLS1_SUFFIX).closeWithoutSave();
        bob.bot().waitUntilEditorClosed(CLS1_SUFFIX);
        assertFalse(bob.bot().isEditorOpen(CLS1));

    }

}
