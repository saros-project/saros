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
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class TestFileOperations {
    private static Musician alice;
    private static Musician bob;
    private static Musician carl;

    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String PKG2 = BotConfiguration.PACKAGENAME2;
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String CLS2 = BotConfiguration.CLASSNAME2;

    @BeforeClass
    public static void initMusicians() throws RemoteException,
        InterruptedException {
        List<Musician> musicians = InitMusician.initAliceBobCarlConcurrently();
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);

        alice.mainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.buildSessionConcurrently(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, carl, bob);
        carl.sessionV.followThisUser(alice.state);
    }

    @Before
    public void setup() throws RemoteException {
        if (!alice.state.existsClass(PROJECT, PKG, CLS))
            alice.mainMenu.newClass(PROJECT, PKG, CLS);
        if (alice.state.existsClass(PROJECT, PKG, CLS2))
            alice.state.deleteClass(PROJECT, PKG, CLS2);
        if (alice.state.isPkgExist(PROJECT, PKG2))
            alice.state.deletePkg(PROJECT, PKG2);
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
        alice.packageExplorerV.renameClass(CLS2, PROJECT, PKG, CLS);
        // bob.state.waitUntilClassExist(PROJECT, PKG, CLS2);
        // carl.state.waitUntilClassExist(PROJECT, PKG, CLS2);
        assertFalse(bob.state.existsClass(PROJECT, PKG, CLS));
        assertTrue(bob.state.existsClass(PROJECT, PKG, CLS2));
        assertFalse(carl.state.existsClass(PROJECT, PKG, CLS));
        assertTrue(carl.state.existsClass(PROJECT, PKG, CLS2));
    }

    @Test
    public void testDeleteFile() throws RemoteException {
        alice.state.deleteClass(PROJECT, PKG, CLS);
        bob.state.waitUntilClassNotExist(PROJECT, PKG, CLS);
        assertFalse(bob.state.existsClass(PROJECT, PKG, CLS));
        carl.state.waitUntilClassNotExist(PROJECT, PKG, CLS);
        assertFalse(carl.state.existsClass(PROJECT, PKG, CLS));
    }

    @Test
    public void testNewPkgAndClass() throws CoreException, IOException {
        alice.mainMenu.newPackage(PROJECT, PKG2);
        bob.state.waitUntilPkgExist(PROJECT, PKG2);
        carl.state.waitUntilPkgExist(PROJECT, PKG2);
        assertTrue(bob.state.isPkgExist(PROJECT, PKG2));
        assertTrue(carl.state.isPkgExist(PROJECT, PKG2));

        alice.mainMenu.newClass(PROJECT, PKG2, CLS);
        bob.state.waitUntilClassExist(PROJECT, PKG2, CLS);
        carl.state.waitUntilClassExist(PROJECT, PKG2, CLS);
        assertTrue(bob.state.existsClass(PROJECT, PKG2, CLS));
        assertTrue(carl.state.existsClass(PROJECT, PKG2, CLS));

        alice.eclipseEditor.setTextInJavaEditorWithSave(
            BotConfiguration.CONTENTPATH, PROJECT, PKG2, CLS);
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
        alice.mainMenu.newPackage(PROJECT, PKG2);
        alice.mainMenu.newClass(PROJECT, PKG2, CLS2);
        alice.packageExplorerV.moveClassTo(PROJECT, PKG2, CLS2, PROJECT, PKG);
        bob.state.waitUntilClassExist(PROJECT, PKG, CLS2);
        carl.state.waitUntilClassExist(PROJECT, PKG, CLS2);
        assertTrue(bob.state.existsClass(PROJECT, PKG, CLS2));
        assertFalse(bob.state.existsClass(PROJECT, PKG2, CLS2));
        assertTrue(carl.state.existsClass(PROJECT, PKG, CLS2));
        assertFalse(carl.state.existsClass(PROJECT, PKG2, CLS2));
    }

    @Test
    public void testRenamePkg() throws RemoteException {
        alice.packageExplorerV.renamePkg(PKG2, PROJECT, "src", PKG);
        // bob.state.waitUntilPkgExist(PROJECT, PKG2);
        // carl.state.waitUntilPkgExist(PROJECT, PKG2);
        assertFalse(bob.state.isPkgExist(PROJECT, PKG));
        assertTrue(bob.state.isPkgExist(PROJECT, PKG2));
        assertFalse(carl.state.isPkgExist(PROJECT, PKG));
        assertTrue(carl.state.isPkgExist(PROJECT, PKG2));
    }

    @Test
    public void testDeletePkg() throws RemoteException {
        alice.state.deletePkg(PROJECT, PKG);
        bob.state.waitUntilPkgNotExist(PROJECT, PKG);
        carl.state.waitUntilPkgNotExist(PROJECT, PKG);
        assertFalse(bob.state.isPkgExist(PROJECT, PKG));
        assertFalse(carl.state.isPkgExist(PROJECT, PKG));
    }
}
