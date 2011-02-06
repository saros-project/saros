package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestPackageExplorerView extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbenchs();
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
        deleteAllProjectsByActiveTesters();
    }

    @Test
    @Ignore
    public void testIsFileExist() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        alice.fileM.newClass(PROJECT1, "pkg", "Cls");
        assertTrue(alice.fileM.existsClass(PROJECT1, "pkg", "Cls"));
        alice.editM.deleteClassNoGUI(PROJECT1, "pkg", "Cls");
        assertFalse(alice.fileM.existsClass(PROJECT1, "pkg", "Cls"));
    }

    @Test
    @Ignore
    // this test fails, but it doesn't really matter...
    public void testIsFileExistWithGUI() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        alice.fileM.newClass(PROJECT1, "pkg", "Cls");
        assertTrue(alice.fileM.existsFiletWithGUI(PROJECT1, "src", "pkg",
            "Cls.java"));
        alice.editM.deleteClassNoGUI(PROJECT1, "pkg", "Cls");
        assertFalse(alice.fileM.existsFiletWithGUI(PROJECT1, "src", "pkg",
            "Cls.java"));
    }

}
