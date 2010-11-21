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
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class TestFileOperations extends STFTest {
    private static Musician alice;
    private static Musician bob;
    private static Musician carl;

    @BeforeClass
    public static void initMusicians() throws RemoteException,
        InterruptedException {
        List<Musician> musicians = InitMusician.initAliceBobCarlConcurrently();
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);

        alice.pEV.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.buildSessionConcurrently(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, carl, bob);
        carl.sessionV.followThisUser(alice.state);
    }

    @Before
    public void setup() throws RemoteException {
        if (!alice.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS)))
            alice.pEV.newClass(PROJECT, PKG, CLS);
        if (alice.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS2)))
            alice.pEV.deleteClass(PROJECT, PKG, CLS2);
        if (alice.pEV.isPkgExist(PROJECT, PKG2))
            alice.pEV.deletePkg(PROJECT, PKG2);
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
        alice.pEV.renameClass(CLS2, PROJECT, PKG, CLS);
        // bob.state.waitUntilClassExist(PROJECT, PKG, CLS2);
        // carl.state.waitUntilClassExist(PROJECT, PKG, CLS2);
        assertFalse(bob.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS)));
        assertTrue(bob.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS2)));
        assertFalse(carl.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS)));
        assertTrue(carl.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS2)));
    }

    @Test
    public void testDeleteFile() throws RemoteException {
        alice.pEV.deleteClass(PROJECT, PKG, CLS);
        bob.pEV.waitUntilClassNotExist(PROJECT, PKG, CLS);
        assertFalse(bob.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS)));
        carl.pEV.waitUntilClassNotExist(PROJECT, PKG, CLS);
        assertFalse(carl.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS)));
    }

    @Test
    public void testNewPkgAndClass() throws CoreException, IOException {
        alice.pEV.newPackage(PROJECT, PKG2);
        bob.pEV.waitUntilPkgExist(PROJECT, PKG2);
        carl.pEV.waitUntilPkgExist(PROJECT, PKG2);
        assertTrue(bob.pEV.isPkgExist(PROJECT, PKG2));
        assertTrue(carl.pEV.isPkgExist(PROJECT, PKG2));

        alice.pEV.newClass(PROJECT, PKG2, CLS);
        bob.pEV.waitUntilClassExist(PROJECT, PKG2, CLS);
        carl.pEV.waitUntilClassExist(PROJECT, PKG2, CLS);
        assertTrue(bob.pEV.isFileExist(getClassPath(PROJECT, PKG2, CLS)));
        assertTrue(carl.pEV.isFileExist(getClassPath(PROJECT, PKG2, CLS)));

        alice.editor.setTextInJavaEditorWithSave(BotConfiguration.CONTENTPATH,
            PROJECT, PKG2, CLS);
        String clsContentOfAlice = alice.state.getClassContent(PROJECT, PKG2,
            CLS);
        carl.state.waitUntilClassContentsSame(PROJECT, PKG2, CLS,
            clsContentOfAlice);
        bob.state.waitUntilClassContentsSame(PROJECT, PKG2, CLS,
            clsContentOfAlice);
        String clsContentOfBob = bob.state.getClassContent(PROJECT, PKG2, CLS);
        String clsContentOfCarl = carl.state
            .getClassContent(PROJECT, PKG2, CLS);
        assertTrue(clsContentOfBob.equals(clsContentOfAlice));
        assertTrue(clsContentOfCarl.equals(clsContentOfAlice));
    }

    @Test
    public void testMoveClass() throws RemoteException {
        alice.pEV.newPackage(PROJECT, PKG2);
        alice.pEV.newClass(PROJECT, PKG2, CLS2);
        alice.pEV.moveClassTo(PROJECT, PKG2, CLS2, PROJECT, PKG);
        bob.pEV.waitUntilClassExist(PROJECT, PKG, CLS2);
        carl.pEV.waitUntilClassExist(PROJECT, PKG, CLS2);
        assertTrue(bob.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS2)));
        assertFalse(bob.pEV.isFileExist(getClassPath(PROJECT, PKG2, CLS2)));
        assertTrue(carl.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS2)));
        assertFalse(carl.pEV.isFileExist(getClassPath(PROJECT, PKG2, CLS2)));
    }

    @Test
    public void testRenamePkg() throws RemoteException {
        alice.pEV.renamePkg(PKG2, PROJECT, "src", PKG);
        // bob.state.waitUntilPkgExist(PROJECT, PKG2);
        // carl.state.waitUntilPkgExist(PROJECT, PKG2);
        assertFalse(bob.pEV.isPkgExist(PROJECT, PKG));
        assertTrue(bob.pEV.isPkgExist(PROJECT, PKG2));
        assertFalse(carl.pEV.isPkgExist(PROJECT, PKG));
        assertTrue(carl.pEV.isPkgExist(PROJECT, PKG2));
    }

    @Test
    public void testDeletePkg() throws RemoteException {
        alice.pEV.deletePkg(PROJECT, PKG);
        bob.pEV.waitUntilPkgNotExist(PROJECT, PKG);
        carl.pEV.waitUntilPkgNotExist(PROJECT, PKG);
        assertFalse(bob.pEV.isPkgExist(PROJECT, PKG));
        assertFalse(carl.pEV.isPkgExist(PROJECT, PKG));
    }
}
