package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestMainMenuComponent extends STFTest {
    private final static Logger log = Logger
        .getLogger(TestMainMenuComponent.class);

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbenchs();
        setUpSaros();
    }

}
