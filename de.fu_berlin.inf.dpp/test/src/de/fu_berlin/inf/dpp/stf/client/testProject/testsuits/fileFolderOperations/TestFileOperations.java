package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.fileFolderOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestFileOperations extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>alice (Host, Write Access), aclice share a java project with bob and
     * carl.</li>
     * <li>bob (Read-Only Access)</li>
     * <li>carl (Read-Only Access)</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbench();
        setUpSaros();
        setUpSessionWithAJavaProjectAndAClass(alice, bob, carl);
        setFollowMode(alice, carl);
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {

        resetSharedProject(alice);

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

        assertTrue(bob.pEV.existsClassNoGUI(PROJECT1, PKG1, CLS1));
        alice.pEV.selectClass(PROJECT1, PKG1, CLS1);
        alice.refactorM.renameClass(CLS2);

        bob.pEV.waitUntilClassExists(PROJECT1, PKG1, CLS2);
        assertFalse(bob.pEV.existsClassNoGUI(PROJECT1, PKG1, CLS1));
        assertTrue(bob.pEV.existsClassNoGUI(PROJECT1, PKG1, CLS2));

        carl.pEV.waitUntilClassExists(PROJECT1, PKG1, CLS2);
        assertFalse(carl.pEV.existsClassNoGUI(PROJECT1, PKG1, CLS1));
        assertTrue(carl.pEV.existsClassNoGUI(PROJECT1, PKG1, CLS2));
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
        alice.editM.deleteClassNoGUI(PROJECT1, PKG1, CLS1);
        bob.pEV.waitUntilClassNotExists(PROJECT1, PKG1, CLS1);
        assertFalse(bob.pEV.existsClassNoGUI(PROJECT1, PKG1, CLS1));
        carl.pEV.waitUntilClassNotExists(PROJECT1, PKG1, CLS1);
        assertFalse(carl.pEV.existsClassNoGUI(PROJECT1, PKG1, CLS1));
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
        bob.pEV.waitUntilPkgExists(PROJECT1, PKG2);
        carl.pEV.waitUntilPkgExists(PROJECT1, PKG2);
        assertTrue(bob.pEV.existsPkgNoGUI(PROJECT1, PKG2));
        assertTrue(carl.pEV.existsPkgNoGUI(PROJECT1, PKG2));

        alice.fileM.newClass(PROJECT1, PKG2, CLS1);
        bob.pEV.waitUntilClassExists(PROJECT1, PKG2, CLS1);
        carl.pEV.waitUntilClassExists(PROJECT1, PKG2, CLS1);
        assertTrue(bob.pEV.existsClassNoGUI(PROJECT1, PKG2, CLS1));
        assertTrue(carl.pEV.existsClassNoGUI(PROJECT1, PKG2, CLS1));

        alice.bot().editor(CLS1_SUFFIX).setTextInEditorWithSave(CP1);
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
        alice.pEV.selectClass(PROJECT1, PKG2, CLS2);
        alice.refactorM.moveClassTo(PROJECT1, PKG1);
        bob.pEV.waitUntilClassExists(PROJECT1, PKG1, CLS2);
        carl.pEV.waitUntilClassExists(PROJECT1, PKG1, CLS2);
        assertTrue(bob.pEV.existsClassNoGUI(PROJECT1, PKG1, CLS2));
        assertFalse(bob.pEV.existsClassNoGUI(PROJECT1, PKG2, CLS2));
        assertTrue(carl.pEV.existsClassNoGUI(PROJECT1, PKG1, CLS2));
        assertFalse(carl.pEV.existsClassNoGUI(PROJECT1, PKG2, CLS2));
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
        alice.pEV.selectPkg(PROJECT1, PKG1);
        alice.refactorM.renamePkg(PKG2);
        bob.pEV.waitUntilPkgExists(PROJECT1, PKG2);
        bob.pEV.waitUntilPkgNotExists(PROJECT1, PKG1);
        assertFalse(bob.pEV.existsPkgNoGUI(PROJECT1, PKG1));
        assertTrue(bob.pEV.existsPkgNoGUI(PROJECT1, PKG2));

        carl.pEV.waitUntilPkgExists(PROJECT1, PKG2);
        carl.pEV.waitUntilPkgNotExists(PROJECT1, PKG1);
        assertFalse(carl.pEV.existsPkgNoGUI(PROJECT1, PKG1));
        assertTrue(carl.pEV.existsPkgNoGUI(PROJECT1, PKG2));
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
        alice.editM.deletePkgNoGUI(PROJECT1, PKG1);
        bob.pEV.waitUntilPkgNotExists(PROJECT1, PKG1);
        carl.pEV.waitUntilPkgNotExists(PROJECT1, PKG1);
        assertFalse(bob.pEV.existsPkgNoGUI(PROJECT1, PKG1));
        assertFalse(carl.pEV.existsPkgNoGUI(PROJECT1, PKG1));
    }
}
