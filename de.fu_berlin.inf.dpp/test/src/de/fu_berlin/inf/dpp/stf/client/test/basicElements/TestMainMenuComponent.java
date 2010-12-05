package de.fu_berlin.inf.dpp.stf.client.test.basicElements;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestMainMenuComponent extends STFTest {
    private final static Logger log = Logger
        .getLogger(TestMainMenuComponent.class);

    @BeforeClass
    public static void initMusican() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        alice.workbench.resetWorkbench();
    }

    @After
    public void cleanup() throws RemoteException {
        alice.workbench.resetSaros();
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
