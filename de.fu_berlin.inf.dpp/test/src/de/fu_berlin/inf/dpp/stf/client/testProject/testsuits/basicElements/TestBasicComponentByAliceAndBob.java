package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements;

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

public class TestBasicComponentByAliceAndBob extends STFTest {

    private static final Logger log = Logger
        .getLogger(TestBasicComponentByAliceAndBob.class);

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbenchs();
        setUpSaros();
        setUpSessionByDefault(alice, bob);
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
        if (bob.sessionV.hasWriteAccess())
            alice.sessionV.restrictToReadOnlyAccessGUI(bob.sessionV);
    }

    // @Test
    // public void existsTableItemInShell() throws RemoteException {
    // alice.pEV.newJavaProject(PROJECT1);
    // alice.pEV.clickContextMenushareProject(PROJECT1);
    // assertTrue(alice.basic.existsTableItem(bob.getBaseJid()));
    // }

    @Test
    public void existsTableItemInView() throws RemoteException {
        alice.sessionV.setFocusOnSessionView();
        assertTrue(alice.table.existsTableItemInView(SESSION_VIEW,
            bob.getBaseJid()));
        assertTrue(alice.table.existsTableItemInView(SESSION_VIEW,
            OWN_CONTACT_NAME + PERMISSION_NAME));
    }

    @Test
    public void selectTableItemInView() throws RemoteException {
        alice.sessionV.setFocusOnSessionView();
        alice.table.selectTableItemInView(SESSION_VIEW, bob.getBaseJid());
        assertTrue(alice.toolbarButton.isToolbarButtonInViewEnabled(
            SESSION_VIEW, "Share your screen with selected user"));
        alice.table.selectTableItemInView(SESSION_VIEW, OWN_CONTACT_NAME
            + PERMISSION_NAME);
        assertFalse(alice.toolbarButton.isToolbarButtonInViewEnabled(
            SESSION_VIEW, "Share your screen with selected user"));
    }

    @Test
    public void clickContextMenuOfTableInView() throws RemoteException {
        alice.sessionV.setFocusOnSessionView();
        alice.table.clickContextMenuOfTableItemInView(SESSION_VIEW,
            bob.getBaseJid(), "Grant write access");
        alice.sessionV.grantWriteAccessGUI(bob.sessionV);
        bob.sessionV.waitUntilHasWriteAccess();
        assertTrue(bob.sessionV.hasWriteAccess());
    }

    @Test
    public void isContextMenuOfTableVisibleInView() throws RemoteException {
        alice.sessionV.setFocusOnSessionView();
        assertTrue(alice.table.isContextMenuOfTableItemVisibleInView(
            SESSION_VIEW, bob.getBaseJid(), "Grant write access"));
        assertTrue(alice.table.isContextMenuOfTableItemVisibleInView(
            SESSION_VIEW, bob.getBaseJid(), "Change Color"));
    }

    @Test
    public void isContextMenuOfTableEnabledInView() throws RemoteException {
        alice.sessionV.setFocusOnSessionView();
        assertTrue(alice.table.isContextMenuOfTableItemEnabledInView(
            SESSION_VIEW, bob.getBaseJid(), "Grant write access"));
        assertFalse(alice.table.isContextMenuOfTableItemEnabledInView(
            SESSION_VIEW, bob.getBaseJid(), "Change Color"));
    }
}
