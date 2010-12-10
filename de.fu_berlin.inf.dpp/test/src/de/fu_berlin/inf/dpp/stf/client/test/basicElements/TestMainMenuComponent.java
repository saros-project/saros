package de.fu_berlin.inf.dpp.stf.client.test.basicElements;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestMainMenuComponent extends STFTest {
    private final static Logger log = Logger
        .getLogger(TestMainMenuComponent.class);

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbenchs();
    }

    @AfterClass
    public static void runAfterClass() throws RemoteException {
        resetSaros();
        resetWorkbenches();
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {
        resetWorkbenches();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetSaros();
        resetWorkbenches();
    }

    /**********************************************
     * 
     * 
     * 
     **********************************************/
    @Test
    public void testCreateAccountWithMainMenu() throws RemoteException {
        alice.mainMenu.creatAccountGUI(bob.jid, bob.password);
    }
}
