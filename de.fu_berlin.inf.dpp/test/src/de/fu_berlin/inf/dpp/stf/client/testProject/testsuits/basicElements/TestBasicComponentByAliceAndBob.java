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
        resetWriteAccess(alice, bob);
    }

    // @Test
    // public void existsTableItemInShell() throws RemoteException {
    // alice.pEV.newJavaProject(PROJECT1);
    // alice.pEV.clickContextMenushareProject(PROJECT1);
    // assertTrue(alice.basic.existsTableItem(bob.getBaseJid()));
    // }

    @Test
    public void existsTableItemInView() throws RemoteException {
        alice.view.setFocusOnViewByTitle(VIEW_SAROS_SESSION);
        assertTrue(alice.table.existsTableItemInView(VIEW_SAROS_SESSION,
            bob.getBaseJid()));
        assertTrue(alice.table.existsTableItemInView(VIEW_SAROS_SESSION,
            OWN_PARTICIPANT_NAME));
    }

    @Test
    public void selectTableItemInView() throws RemoteException {
        alice.view.setFocusOnViewByTitle(VIEW_SAROS_SESSION);
        alice.table.selectTableItemInView(VIEW_SAROS_SESSION, bob.getBaseJid());
        assertTrue(alice.toolbarButton.isToolbarButtonInViewEnabled(
            VIEW_SAROS_SESSION, "Share your screen with selected buddy"));
        alice.table.selectTableItemInView(VIEW_SAROS_SESSION,
            OWN_PARTICIPANT_NAME);
        assertFalse(alice.toolbarButton.isToolbarButtonInViewEnabled(
            VIEW_SAROS_SESSION, "Share your screen with selected buddy"));
    }

    @Test
    public void clickContextMenuOfTableInView() throws RemoteException {
        alice.view.setFocusOnViewByTitle(VIEW_SAROS_SESSION);

        alice.table.clickContextMenuOfTableItemInView(VIEW_SAROS_SESSION,
            bob.getBaseJid(), "Restrict to read-only access");
        bob.sarosSessionV.waitUntilHasReadOnlyAccess();
        assertTrue(bob.sarosSessionV.hasReadOnlyAccess());
    }

    @Test
    public void isContextMenuOfTableVisibleInView() throws RemoteException {
        alice.view.setFocusOnViewByTitle(VIEW_SAROS_SESSION);
        assertTrue(alice.table.isContextMenuOfTableItemVisibleInView(
            VIEW_SAROS_SESSION, bob.getBaseJid(),
            "Restrict to read-only access"));
        assertTrue(alice.table.isContextMenuOfTableItemVisibleInView(
            VIEW_SAROS_SESSION, bob.getBaseJid(), "Change Color"));
    }

    @Test
    public void isContextMenuOfTableEnabledInView() throws RemoteException {
        alice.view.setFocusOnViewByTitle(VIEW_SAROS_SESSION);
        assertTrue(alice.table.isContextMenuOfTableItemEnabledInView(
            VIEW_SAROS_SESSION, bob.getBaseJid(),
            "Restrict to read-only access"));
        assertFalse(alice.table.isContextMenuOfTableItemEnabledInView(
            VIEW_SAROS_SESSION, bob.getBaseJid(), "Change Color"));
    }
}
