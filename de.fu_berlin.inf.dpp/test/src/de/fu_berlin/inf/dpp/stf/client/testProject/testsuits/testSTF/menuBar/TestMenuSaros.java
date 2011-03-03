package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.menuBar;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestMenuSaros extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
        setUpSaros();
    }

    // @After
    // public void runAfterEveryTest() throws RemoteException {
    // deleteAllProjectsByActiveTesters();
    // }

    @Test
    public void toolbarButton() throws RemoteException {

        alice.sarosBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.bot().menu(MENU_SAROS).menu("Share Project(s)...").click();
    }
}
