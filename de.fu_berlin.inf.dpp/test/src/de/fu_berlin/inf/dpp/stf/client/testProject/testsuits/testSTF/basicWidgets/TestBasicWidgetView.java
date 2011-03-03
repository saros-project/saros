package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.basicWidgets;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestBasicWidgetView extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        // setUpWorkbench();
        // setUpSaros();
    }

    // @After
    // public void runAfterEveryTest() throws RemoteException {
    // deleteAllProjectsByActiveTesters();
    // }

    @Test
    public void toolbarButton() throws RemoteException {
        alice.bot().view(VIEW_SAROS_SESSION).show();
        assertTrue(alice.bot().view(VIEW_SAROS_SESSION)
            .existsToolbarButton(TB_LEAVE_THE_SESSION));
        // alice.bot().view(VIEW_SAROS_BUDDIES).show();
        assertTrue(alice.bot().view(VIEW_SAROS_SESSION)
            .toolbarButton(TB_LEAVE_THE_SESSION).isEnabled());

    }
}
