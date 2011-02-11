package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.basicWidgets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestBasicWidgetsTable extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbench();
        setUpSaros();
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetWriteAccess(alice, bob);
    }

    @Test
    public void existsTableItemInView() throws RemoteException {
        alice.commonWidgets().view(VIEW_SAROS_SESSION).setFocus();
        assertTrue(alice.table.existsTableItemInView(VIEW_SAROS_SESSION,
            bob.getBaseJid()));
        assertTrue(alice.table.existsTableItemInView(VIEW_SAROS_SESSION,
            OWN_PARTICIPANT_NAME));
    }

    @Test
    public void selectTableItemInView() throws RemoteException {
        alice.commonWidgets().view(VIEW_SAROS_SESSION).setFocus();
        alice.table.selectTableItemInView(VIEW_SAROS_SESSION, bob.getBaseJid());
        assertTrue(alice.toolbarButton.isToolbarButtonOnViewEnabled(
            VIEW_SAROS_SESSION, TB_SHARE_SCREEN_WITH_BUDDY));
        alice.table.selectTableItemInView(VIEW_SAROS_SESSION,
            OWN_PARTICIPANT_NAME);
        assertFalse(alice.toolbarButton.isToolbarButtonOnViewEnabled(
            VIEW_SAROS_SESSION, TB_SHARE_SCREEN_WITH_BUDDY));
    }

    @Test
    public void clickContextMenuOfTableInView() throws RemoteException {
        alice.commonWidgets().view(VIEW_SAROS_SESSION).setFocus();

        alice.table.clickContextMenuOfTableItemInView(VIEW_SAROS_SESSION,
            bob.getBaseJid(), CM_RESTRICT_TO_READ_ONLY_ACCESS);
        bob.sarosSessionV.waitUntilHasReadOnlyAccess();
        assertTrue(bob.sarosSessionV.hasReadOnlyAccess());
    }

    @Test
    public void isContextMenuOfTableVisibleInView() throws RemoteException {
        alice.commonWidgets().view(VIEW_SAROS_SESSION).setFocus();
        assertTrue(alice.table.isContextMenuOfTableItemVisibleInView(
            VIEW_SAROS_SESSION, bob.getBaseJid(),
            CM_RESTRICT_TO_READ_ONLY_ACCESS));
        assertTrue(alice.table.isContextMenuOfTableItemVisibleInView(
            VIEW_SAROS_SESSION, bob.getBaseJid(), CM_CHANGE_COLOR));
    }

    @Test
    public void isContextMenuOfTableEnabledInView() throws RemoteException {
        alice.commonWidgets().view(VIEW_SAROS_SESSION).setFocus();
        assertTrue(alice.table.isContextMenuOfTableItemEnabledInView(
            VIEW_SAROS_SESSION, bob.getBaseJid(),
            CM_RESTRICT_TO_READ_ONLY_ACCESS));
        assertFalse(alice.table.isContextMenuOfTableItemEnabledInView(
            VIEW_SAROS_SESSION, bob.getBaseJid(), CM_CHANGE_COLOR));
    }
}
