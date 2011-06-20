package de.fu_berlin.inf.dpp.stf.test.stf.editor;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SUFFIX_JAVA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class EditorByAliceBobTest extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE, BOB);
        setUpWorkbench();
        setUpSaros();
        Util.setUpSessionWithAJavaProjectAndAClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1, ALICE, BOB);
        if (ALICE.remoteBot().isEditorOpen(Constants.CLS1 + SUFFIX_JAVA))
            ALICE.remoteBot().editor(Constants.CLS1 + SUFFIX_JAVA)
                .closeWithSave();
    }

    @Test
    public void isJavaEditorOpen() throws RemoteException {
        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();
        assertTrue(ALICE.remoteBot().isEditorOpen(Constants.CLS1_SUFFIX));
        ALICE.remoteBot().editor(Constants.CLS1 + SUFFIX_JAVA).closeWithSave();
        assertFalse(ALICE.remoteBot().isEditorOpen(Constants.CLS1_SUFFIX));
    }

    @Test
    public void isEditorOpen() throws RemoteException {
        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();
        assertTrue(ALICE.remoteBot().isEditorOpen(Constants.CLS1 + SUFFIX_JAVA));
        ALICE.remoteBot().editor(Constants.CLS1 + SUFFIX_JAVA).closeWithSave();
        assertFalse(ALICE.remoteBot()
            .isEditorOpen(Constants.CLS1 + SUFFIX_JAVA));
    }

    @Test
    public void waitUntilBobsJavaEditorIsOpen() throws RemoteException {

        BOB.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .followParticipant();
        assertTrue(BOB.superBot().views().sarosView()
            .selectParticipant(ALICE.getJID()).isFollowing());
        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();
        BOB.remoteBot().waitUntilEditorOpen(Constants.CLS1_SUFFIX);
        assertTrue(BOB.remoteBot().isEditorOpen(Constants.CLS1_SUFFIX));
    }

    @Test
    public void waitUntilBobsJavaEditorIsActive() throws RemoteException {
        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();
        Util.setFollowMode(ALICE, BOB);
        assertTrue(BOB.superBot().views().sarosView()
            .selectParticipant(ALICE.getJID()).isFollowing());
        assertTrue(BOB.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());

        ALICE.superBot().views().packageExplorerView().tree().newC()
            .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS2);
        BOB.remoteBot().editor(Constants.CLS2_SUFFIX).waitUntilIsActive();
        assertTrue(BOB.remoteBot().editor(Constants.CLS2_SUFFIX).isActive());
        assertFalse(BOB.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());

        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).show();
        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsActive();
        assertTrue(ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());
        BOB.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsActive();
        assertTrue(BOB.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());
        assertFalse(BOB.remoteBot().editor(Constants.CLS2_SUFFIX).isActive());
    }

    @Test
    public void waitUntilBobsJavaEditorIsClosed() throws RemoteException {
        Util.setFollowMode(ALICE, BOB);
        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();
        BOB.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsActive();
        assertTrue(BOB.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());

        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).closeWithoutSave();
        BOB.remoteBot().waitUntilEditorClosed(Constants.CLS1_SUFFIX);
        assertFalse(BOB.remoteBot().isEditorOpen(Constants.CLS1));

    }

}
