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

    public static String VIEW_SESSION = "Shared Project Session";
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
        if (bob.sessionV.isDriver())
            alice.sessionV.removeDriverRoleGUI(bob.sessionV);
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
        assertTrue(alice.table.existsTableItemInView(VIEW_SESSION,
            bob.getBaseJid()));
        assertTrue(alice.table.existsTableItemInView(VIEW_SESSION,
            OWN_CONTACT_NAME + ROLE_NAME));
    }

    @Test
    public void selectTableItemInView() throws RemoteException {
        alice.sessionV.setFocusOnSessionView();
        alice.table.selectTableItemInView(VIEW_SESSION, bob.getBaseJid());
        assertTrue(alice.toolbarButton.isToolbarButtonInViewEnabled(
            VIEW_SESSION, "Share your screen with selected user"));
        alice.table.selectTableItemInView(VIEW_SESSION, OWN_CONTACT_NAME
            + ROLE_NAME);
        assertFalse(alice.toolbarButton.isToolbarButtonInViewEnabled(
            VIEW_SESSION, "Share your screen with selected user"));
    }

    @Test
    public void clickContextMenuOfTableInView() throws RemoteException {
        alice.sessionV.setFocusOnSessionView();
        alice.table.clickContextMenuOfTableItemInView(VIEW_SESSION,
            bob.getBaseJid(), "Give driver role");
        alice.sessionV.giveDriverRoleGUI(bob.sessionV);
        bob.sessionV.waitUntilIsDriver();
        assertTrue(bob.sessionV.isDriver());
    }

    @Test
    public void isContextMenuOfTableVisibleInView() throws RemoteException {
        alice.sessionV.setFocusOnSessionView();
        assertTrue(alice.table.isContextMenuOfTableItemVisibleInView(VIEW_SESSION,
            bob.getBaseJid(), "Give driver role"));
        assertTrue(alice.table.isContextMenuOfTableItemVisibleInView(VIEW_SESSION,
            bob.getBaseJid(), "Change Color"));
    }

    @Test
    public void isContextMenuOfTableEnabledInView() throws RemoteException {
        alice.sessionV.setFocusOnSessionView();
        assertTrue(alice.table.isContextMenuOfTableItemEnabledInView(VIEW_SESSION,
            bob.getBaseJid(), "Give driver role"));
        assertFalse(alice.table.isContextMenuOfTableItemEnabledInView(VIEW_SESSION,
            bob.getBaseJid(), "Change Color"));
    }
}
