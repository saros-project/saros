package de.fu_berlin.inf.dpp.stf.client.test.testcases.fileFolderOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestFileOperations extends STFTest {

    @BeforeClass
    public static void initMusicians() throws RemoteException,
        InterruptedException {
        List<Musician> musicians = InitMusician.initAliceBobCarlConcurrently();
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);

        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.buildSessionConcurrently(PROJECT1, CONTEXT_MENU_SHARE_PROJECT,
            carl, bob);
        carl.sessionV.followThisUser(alice.state);
    }

    @Before
    public void setup() throws RemoteException {
        if (!alice.pEV.isFileExist(getClassPath(PROJECT1, PKG1, CLS1)))
            alice.pEV.newClass(PROJECT1, PKG1, CLS1);
        if (alice.pEV.isFileExist(getClassPath(PROJECT1, PKG1, CLS2)))
            alice.pEV.deleteClass(PROJECT1, PKG1, CLS2);
        if (alice.pEV.isPkgExist(PROJECT1, PKG2))
            alice.pEV.deletePkg(PROJECT1, PKG2);
        bob.workbench.resetWorkbench();
        carl.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    @After
    public void cleanUp() throws RemoteException {
        carl.workbench.resetWorkbench();
        bob.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        carl.workbench.resetSaros();
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    @Test
    public void testRenameFile() throws RemoteException {
        alice.pEV.renameClass(CLS2, PROJECT1, PKG1, CLS1);
        // bob.state.waitUntilClassExist(PROJECT, PKG, CLS2);
        // carl.state.waitUntilClassExist(PROJECT, PKG, CLS2);
        assertFalse(bob.pEV.isFileExist(getClassPath(PROJECT1, PKG1, CLS1)));
        assertTrue(bob.pEV.isFileExist(getClassPath(PROJECT1, PKG1, CLS2)));
        assertFalse(carl.pEV.isFileExist(getClassPath(PROJECT1, PKG1, CLS1)));
        assertTrue(carl.pEV.isFileExist(getClassPath(PROJECT1, PKG1, CLS2)));
    }

    @Test
    public void testDeleteFile() throws RemoteException {
        alice.pEV.deleteClass(PROJECT1, PKG1, CLS1);
        bob.pEV.waitUntilClassNotExist(PROJECT1, PKG1, CLS1);
        assertFalse(bob.pEV.isFileExist(getClassPath(PROJECT1, PKG1, CLS1)));
        carl.pEV.waitUntilClassNotExist(PROJECT1, PKG1, CLS1);
        assertFalse(carl.pEV.isFileExist(getClassPath(PROJECT1, PKG1, CLS1)));
    }

    @Test
    public void testNewPkgAndClass() throws CoreException, IOException {
        alice.pEV.newPackage(PROJECT1, PKG2);
        bob.pEV.waitUntilPkgExist(PROJECT1, PKG2);
        carl.pEV.waitUntilPkgExist(PROJECT1, PKG2);
        assertTrue(bob.pEV.isPkgExist(PROJECT1, PKG2));
        assertTrue(carl.pEV.isPkgExist(PROJECT1, PKG2));

        alice.pEV.newClass(PROJECT1, PKG2, CLS1);
        bob.pEV.waitUntilClassExist(PROJECT1, PKG2, CLS1);
        carl.pEV.waitUntilClassExist(PROJECT1, PKG2, CLS1);
        assertTrue(bob.pEV.isFileExist(getClassPath(PROJECT1, PKG2, CLS1)));
        assertTrue(carl.pEV.isFileExist(getClassPath(PROJECT1, PKG2, CLS1)));

        alice.editor.setTextInJavaEditorWithSave(CP1, PROJECT1, PKG2, CLS1);
        String clsContentOfAlice = alice.state.getClassContent(PROJECT1, PKG2,
            CLS1);
        carl.state.waitUntilClassContentsSame(PROJECT1, PKG2, CLS1,
            clsContentOfAlice);
        bob.state.waitUntilClassContentsSame(PROJECT1, PKG2, CLS1,
            clsContentOfAlice);
        String clsContentOfBob = bob.state
            .getClassContent(PROJECT1, PKG2, CLS1);
        String clsContentOfCarl = carl.state.getClassContent(PROJECT1, PKG2,
            CLS1);
        assertTrue(clsContentOfBob.equals(clsContentOfAlice));
        assertTrue(clsContentOfCarl.equals(clsContentOfAlice));
    }

    @Test
    public void testMoveClass() throws RemoteException {
        alice.pEV.newPackage(PROJECT1, PKG2);
        alice.pEV.newClass(PROJECT1, PKG2, CLS2);
        alice.pEV.moveClassTo(PROJECT1, PKG2, CLS2, PROJECT1, PKG1);
        bob.pEV.waitUntilClassExist(PROJECT1, PKG1, CLS2);
        carl.pEV.waitUntilClassExist(PROJECT1, PKG1, CLS2);
        assertTrue(bob.pEV.isFileExist(getClassPath(PROJECT1, PKG1, CLS2)));
        assertFalse(bob.pEV.isFileExist(getClassPath(PROJECT1, PKG2, CLS2)));
        assertTrue(carl.pEV.isFileExist(getClassPath(PROJECT1, PKG1, CLS2)));
        assertFalse(carl.pEV.isFileExist(getClassPath(PROJECT1, PKG2, CLS2)));
    }

    @Test
    public void testRenamePkg() throws RemoteException {
        alice.pEV.renamePkg(PKG2, PROJECT1, PKG1);
        // bob.state.waitUntilPkgExist(PROJECT, PKG2);
        // carl.state.waitUntilPkgExist(PROJECT, PKG2);
        assertFalse(bob.pEV.isPkgExist(PROJECT1, PKG1));
        assertTrue(bob.pEV.isPkgExist(PROJECT1, PKG2));
        assertFalse(carl.pEV.isPkgExist(PROJECT1, PKG1));
        assertTrue(carl.pEV.isPkgExist(PROJECT1, PKG2));
    }

    @Test
    public void testDeletePkg() throws RemoteException {
        alice.pEV.deletePkg(PROJECT1, PKG1);
        bob.pEV.waitUntilPkgNotExist(PROJECT1, PKG1);
        carl.pEV.waitUntilPkgNotExist(PROJECT1, PKG1);
        assertFalse(bob.pEV.isPkgExist(PROJECT1, PKG1));
        assertFalse(carl.pEV.isPkgExist(PROJECT1, PKG1));
    }
}
