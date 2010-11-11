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
        if (!alice.eclipseState.existsClass(PROJECT, PKG, CLS))
            alice.mainMenu.newClass(PROJECT, PKG, CLS);
        if (alice.eclipseState.existsClass(PROJECT, PKG, CLS2))
            alice.eclipseState.deleteClass(PROJECT, PKG, CLS2);
        if (alice.eclipseState.isPkgExist(PROJECT, PKG2))
            alice.eclipseState.deletePkg(PROJECT, PKG2);
        bob.bot.resetWorkbench();
        carl.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    @After
    public void cleanUp() throws RemoteException {
        carl.bot.resetWorkbench();
        bob.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        carl.bot.resetSaros();
        bob.bot.resetSaros();
        alice.bot.resetSaros();
    }

    @Test
    public void testRenameFile() throws RemoteException {
        // alice.renameFile(
        alice.packageExplorerV.renameClass(CLS2, PROJECT, PKG, CLS);
        bob.eclipseState.waitUntilClassExist(PROJECT, PKG, CLS2);
        carl.eclipseState.waitUntilClassExist(PROJECT, PKG, CLS2);
        assertFalse(bob.eclipseState.existsClass(PROJECT, PKG, CLS));
        assertTrue(bob.eclipseState.existsClass(PROJECT, PKG, CLS2));
        assertFalse(carl.eclipseState.existsClass(PROJECT, PKG, CLS));
        assertTrue(carl.eclipseState.existsClass(PROJECT, PKG, CLS2));
    }

    @Test
    public void testDeleteFile() throws RemoteException {
        alice.eclipseState.deleteClass(PROJECT, PKG, CLS);
        bob.eclipseState.waitUntilClassNotExist(PROJECT, PKG, CLS);
        assertFalse(bob.eclipseState.existsClass(PROJECT, PKG, CLS));
        carl.eclipseState.waitUntilClassNotExist(PROJECT, PKG, CLS);
        assertFalse(carl.eclipseState.existsClass(PROJECT, PKG, CLS));
    }

    @Test
    public void testNewPkgAndClass() throws CoreException, IOException {
        alice.mainMenu.newPackage(PROJECT, PKG2);
        bob.eclipseState.waitUntilPkgExist(PROJECT, PKG2);
        carl.eclipseState.waitUntilPkgExist(PROJECT, PKG2);
        assertTrue(bob.eclipseState.isPkgExist(PROJECT, PKG2));
        assertTrue(carl.eclipseState.isPkgExist(PROJECT, PKG2));

        alice.mainMenu.newClass(PROJECT, PKG2, CLS);
        bob.eclipseState.waitUntilClassExist(PROJECT, PKG2, CLS);
        carl.eclipseState.waitUntilClassExist(PROJECT, PKG2, CLS);
        assertTrue(bob.eclipseState.existsClass(PROJECT, PKG2, CLS));
        assertTrue(carl.eclipseState.existsClass(PROJECT, PKG2, CLS));

        alice.eclipseEditor.setTextInJavaEditorWithSave(
            BotConfiguration.CONTENTPATH, PROJECT, PKG2, CLS);
        String clsContentOfAlice = alice.eclipseState.getClassContent(PROJECT,
            PKG2, CLS);
        carl.eclipseState.waitUntilClassContentsSame(PROJECT, PKG2, CLS,
            clsContentOfAlice);
        bob.eclipseState.waitUntilClassContentsSame(PROJECT, PKG2, CLS,
            clsContentOfAlice);
        String clsContentOfBob = bob.eclipseState.getClassContent(PROJECT,
            PKG2, CLS);
        String clsContentOfCarl = carl.eclipseState.getClassContent(PROJECT,
            PKG2, CLS);
        assertTrue(clsContentOfBob.equals(clsContentOfAlice));
        assertTrue(clsContentOfCarl.equals(clsContentOfAlice));
    }

    @Test
    public void testMoveClass() throws RemoteException {
        alice.mainMenu.newPackage(PROJECT, PKG2);
        alice.mainMenu.newClass(PROJECT, PKG2, CLS2);
        alice.packageExplorerV.moveClassTo(PROJECT, PKG2, CLS2, PROJECT, PKG);
        bob.eclipseState.waitUntilClassExist(PROJECT, PKG, CLS2);
        carl.eclipseState.waitUntilClassExist(PROJECT, PKG, CLS2);
        assertTrue(bob.eclipseState.existsClass(PROJECT, PKG, CLS2));
        assertFalse(bob.eclipseState.existsClass(PROJECT, PKG2, CLS2));
        assertTrue(carl.eclipseState.existsClass(PROJECT, PKG, CLS2));
        assertFalse(carl.eclipseState.existsClass(PROJECT, PKG2, CLS2));
    }

    @Test
    public void testRenamePkg() throws RemoteException {
        alice.packageExplorerV.renamePkg(PKG2, PROJECT, "src", PKG);
        bob.eclipseState.waitUntilPkgExist(PROJECT, PKG2);
        carl.eclipseState.waitUntilPkgExist(PROJECT, PKG2);
        assertFalse(bob.eclipseState.isPkgExist(PROJECT, PKG));
        assertTrue(bob.eclipseState.isPkgExist(PROJECT, PKG2));
        assertFalse(carl.eclipseState.isPkgExist(PROJECT, PKG));
        assertTrue(carl.eclipseState.isPkgExist(PROJECT, PKG2));
    }

    @Test
    public void testDeletePkg() throws RemoteException {
        // alice.renameFile(
        alice.eclipseState.deletePkg(PROJECT, PKG);
        bob.eclipseState.waitUntilPkgNotExist(PROJECT, PKG);
        carl.eclipseState.waitUntilPkgNotExist(PROJECT, PKG);
        assertFalse(bob.eclipseState.isPkgExist(PROJECT, PKG));
        assertFalse(carl.eclipseState.isPkgExist(PROJECT, PKG));
    }
}
