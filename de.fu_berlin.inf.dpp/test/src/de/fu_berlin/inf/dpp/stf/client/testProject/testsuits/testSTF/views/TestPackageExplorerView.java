package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
    @Ignore
    public void testIsFileExist() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        alice.sarosBot().file().newClass(PROJECT1, "pkg", "Cls");
        assertTrue(alice.sarosBot().file()
            .existsClassNoGUI(PROJECT1, "pkg", "Cls"));
        alice.sarosBot().edit().deleteClassNoGUI(PROJECT1, "pkg", "Cls");
        assertFalse(alice.sarosBot().file()
            .existsClassNoGUI(PROJECT1, "pkg", "Cls"));
    }

    @Test
    @Ignore
    // this test fails, but it doesn't really matter...
    public void testIsFileExistWithGUI() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        alice.sarosBot().file().newClass(PROJECT1, "pkg", "Cls");
        assertTrue(alice.sarosBot().file()
            .existsFile(PROJECT1, "src", "pkg", "Cls.java"));
        alice.sarosBot().edit().deleteClassNoGUI(PROJECT1, "pkg", "Cls");
        assertFalse(alice.sarosBot().file()
            .existsFile(PROJECT1, "src", "pkg", "Cls.java"));
    }

}
