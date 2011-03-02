package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestPackageExplorerView extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        deleteAllProjectsByActiveTesters();
    }

    @Test
    public void testIsFileExist() throws RemoteException {
        alice.sarosBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.sarosBot().views().packageExplorerView().tree().newC()
            .cls(PROJECT1, PKG1, CLS1);
        assertTrue(alice.sarosBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS1_SUFFIX));
        alice.noBot().deleteClassNoGUI(PROJECT1, PKG1, CLS1);
        assertFalse(alice.sarosBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS1_SUFFIX));
    }

    @Test
    // @Ignore
    // this test fails, but it doesn't really matter...
    public void testIsFileExistWithGUI() throws RemoteException {
        alice.sarosBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.sarosBot().views().packageExplorerView().tree().newC()
            .cls(PROJECT1, PKG1, CLS1);
        assertTrue(alice.sarosBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS1_SUFFIX));

        alice.noBot().deleteClassNoGUI(PROJECT1, PKG1, CLS1);
        assertFalse(alice.sarosBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS1_SUFFIX));

    }
}
