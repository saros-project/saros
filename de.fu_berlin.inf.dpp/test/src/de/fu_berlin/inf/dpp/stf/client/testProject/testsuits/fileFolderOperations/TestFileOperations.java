package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.fileFolderOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestFileOperations extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>alice (Host, Driver), aclice share a java project with bob and carl.</li>
     * <li>bob (Observer)</li>
     * <li>carl (Observer)</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbenchs();
        setUpSaros();
        setUpSession(alice, bob, carl);
        carl.sessionV.followThisUserGUI(alice.jid);
    }

    @AfterClass
    public static void runAfterClass() throws RemoteException,
        InterruptedException {
        alice.leaveSessionHostFirstDone(bob, carl);
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {
        // Make sure CLS1 always has the same content
        if (alice.fileM.existsClass(PROJECT1, PKG1, CLS1))
            alice.editM.deleteClass(PROJECT1, PKG1, CLS1);
        alice.fileM.newClass(PROJECT1, PKG1, CLS1);
        if (alice.fileM.existsClass(PROJECT1, PKG1, CLS2))
            alice.editM.deleteClass(PROJECT1, PKG1, CLS2);
        if (alice.fileM.existsPkg(PROJECT1, PKG2))
            alice.editM.deletePkg(PROJECT1, PKG2);
        // FIXME This method assumes that all the file operations (like
        // deleteClass) work...
        // Need to make sure that the preconditions are met by bob and carl too.
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        alice.addBuddyGUIDone(bob);
        bob.addBuddyGUIDone(alice);
    }

    /**
     * Steps:
     * <ol>
     * <li>alice rename the class "CLS1" to "CLS2"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>the class'name are renamed by bob and carl too</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testRenameFile() throws RemoteException {
        /*
         * TODO, sometimes i get the assertError exception by performing the
         * following assertTrue, it seems that the building-session isn't
         * finished yet.
         */
        bob.workbench.sleep(1000);
        assertTrue(bob.fileM.existsClass(PROJECT1, PKG1, CLS1));
        alice.refactorM.renameClass(CLS2, PROJECT1, PKG1, CLS1);

        bob.fileM.waitUntilClassExisted(PROJECT1, PKG1, CLS2);
        assertFalse(bob.fileM.existsClass(PROJECT1, PKG1, CLS1));
        assertTrue(bob.fileM.existsClass(PROJECT1, PKG1, CLS2));

        carl.fileM.waitUntilClassExisted(PROJECT1, PKG1, CLS2);
        assertFalse(carl.fileM.existsClass(PROJECT1, PKG1, CLS1));
        assertTrue(carl.fileM.existsClass(PROJECT1, PKG1, CLS2));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice delete the class "CLS1"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>the class are deleted by bob and carl too</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testDeleteFile() throws RemoteException {
        alice.editM.deleteClass(PROJECT1, PKG1, CLS1);
        bob.fileM.waitUntilClassNotExist(PROJECT1, PKG1, CLS1);
        assertFalse(bob.fileM.existsClass(PROJECT1, PKG1, CLS1));
        carl.fileM.waitUntilClassNotExist(PROJECT1, PKG1, CLS1);
        assertFalse(carl.fileM.existsClass(PROJECT1, PKG1, CLS1));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice create a new package "PKG2"</li>
     * <li>alice create a new class "CLS1" under package "PKG2"</li>
     * <li>alice set text in the class "CLS1"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>the package "PKG2" are created by bob and carl too</li>
     * <li>the new class "CLS1" are created by bob and carl too</li>
     * <li>carl and bob should see the change by alice</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testNewPkgAndClass() throws CoreException, IOException {
        alice.fileM.newPackage(PROJECT1, PKG2);
        bob.fileM.waitUntilPkgExisted(PROJECT1, PKG2);
        carl.fileM.waitUntilPkgExisted(PROJECT1, PKG2);
        assertTrue(bob.fileM.existsPkg(PROJECT1, PKG2));
        assertTrue(carl.fileM.existsPkg(PROJECT1, PKG2));

        alice.fileM.newClass(PROJECT1, PKG2, CLS1);
        bob.fileM.waitUntilClassExisted(PROJECT1, PKG2, CLS1);
        carl.fileM.waitUntilClassExisted(PROJECT1, PKG2, CLS1);
        assertTrue(bob.fileM.existsClass(PROJECT1, PKG2, CLS1));
        assertTrue(carl.fileM.existsClass(PROJECT1, PKG2, CLS1));

        alice.editor.setTextInJavaEditorWithSave(CP1, PROJECT1, PKG2, CLS1);
        String clsContentOfAlice = alice.editor.getClassContent(PROJECT1, PKG2,
            CLS1);
        carl.editor.waitUntilClassContentsSame(PROJECT1, PKG2, CLS1,
            clsContentOfAlice);
        bob.editor.waitUntilClassContentsSame(PROJECT1, PKG2, CLS1,
            clsContentOfAlice);
        String clsContentOfBob = bob.editor.getClassContent(PROJECT1, PKG2,
            CLS1);
        String clsContentOfCarl = carl.editor.getClassContent(PROJECT1, PKG2,
            CLS1);
        assertTrue(clsContentOfBob.equals(clsContentOfAlice));
        assertTrue(clsContentOfCarl.equals(clsContentOfAlice));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice create a new package "PKG2" and under it create a new class
     * "CLS2"</li>
     * <li>alice move the class "CLS2" to the package "PKG1"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li></li>
     * <li>the class "CLS2" should be moved into the package "PKG1" by carl and
     * bob</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testMoveClass() throws RemoteException {
        alice.fileM.newPackage(PROJECT1, PKG2);
        alice.fileM.newClass(PROJECT1, PKG2, CLS2);
        alice.refactorM.moveClassTo(PROJECT1, PKG2, CLS2, PROJECT1, PKG1);
        bob.fileM.waitUntilClassExisted(PROJECT1, PKG1, CLS2);
        carl.fileM.waitUntilClassExisted(PROJECT1, PKG1, CLS2);
        assertTrue(bob.fileM.existsClass(PROJECT1, PKG1, CLS2));
        assertFalse(bob.fileM.existsClass(PROJECT1, PKG2, CLS2));
        assertTrue(carl.fileM.existsClass(PROJECT1, PKG1, CLS2));
        assertFalse(carl.fileM.existsClass(PROJECT1, PKG2, CLS2));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice rename the package "PKG1" to "PKG2"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>the package should be renamed by carl and bob</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testRenamePkg() throws RemoteException {
        alice.refactorM.renamePkg(PKG2, PROJECT1, PKG1);

        bob.fileM.waitUntilPkgExisted(PROJECT1, PKG2);
        bob.fileM.waitUntilPkgNotExist(PROJECT1, PKG1);
        assertFalse(bob.fileM.existsPkg(PROJECT1, PKG1));
        assertTrue(bob.fileM.existsPkg(PROJECT1, PKG2));

        carl.fileM.waitUntilPkgExisted(PROJECT1, PKG2);
        carl.fileM.waitUntilPkgNotExist(PROJECT1, PKG1);
        assertFalse(carl.fileM.existsPkg(PROJECT1, PKG1));
        assertTrue(carl.fileM.existsPkg(PROJECT1, PKG2));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice delete the package "PKG1"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>the package should be deleted by carl and bob</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testDeletePkg() throws RemoteException {
        alice.editM.deletePkg(PROJECT1, PKG1);
        bob.fileM.waitUntilPkgNotExist(PROJECT1, PKG1);
        carl.fileM.waitUntilPkgNotExist(PROJECT1, PKG1);
        assertFalse(bob.fileM.existsPkg(PROJECT1, PKG1));
        assertFalse(carl.fileM.existsPkg(PROJECT1, PKG1));
    }
}
