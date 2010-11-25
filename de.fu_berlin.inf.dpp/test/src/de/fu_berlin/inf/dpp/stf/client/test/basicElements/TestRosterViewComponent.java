package de.fu_berlin.inf.dpp.stf.client.test.basicElements;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestRosterViewComponent extends STFTest {
    private static final Logger log = Logger
        .getLogger(TestBasicSarosElements.class);

    @BeforeClass
    public static void initMusican() throws RemoteException {
        edna = InitMusician.newEdna();
        // dave = InitMusician.newDave();
    }

    @AfterClass
    public static void afterClass() throws RemoteException {
        edna.workbench.resetSaros();
        // dave.workbench.resetSaros();
    }

    @After
    public void cleanup() throws RemoteException {
        edna.workbench.resetWorkbench();
        // dave.workbench.resetWorkbench();
    }

    // @Test
    // @Ignore
    // public void testBuddy() throws RemoteException {
    // assertTrue(edna.rosterV.hasContactWith(dave.jid));
    // }

    @Test
    public void testConnect() throws RemoteException {

    }
}
