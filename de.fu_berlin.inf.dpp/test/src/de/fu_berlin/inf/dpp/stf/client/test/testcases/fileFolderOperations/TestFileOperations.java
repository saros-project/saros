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

        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.buildSessionConcurrently(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, carl, bob);
        carl.bot.followUser(alice.state, alice.jid);
    }

    @Before
    public void setup() throws RemoteException {
        if (!alice.bot.isClassExist(PROJECT, PKG, CLS))
            alice.bot.newClass(PROJECT, PKG, CLS);
        if (alice.bot.isClassExist(PROJECT, PKG, CLS2))
            alice.bot.deleteClass(PROJECT, PKG, CLS2);
        if (alice.bot.isPkgExist(PROJECT, PKG2))
            alice.bot.deletePkg(PROJECT, PKG2);
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
        alice.bot.renameClass(CLS2, PROJECT, PKG, CLS);
        bob.bot.waitUntilClassExist(PROJECT, PKG, CLS2);
        carl.bot.waitUntilClassExist(PROJECT, PKG, CLS2);
        assertFalse(bob.bot.isClassExist(PROJECT, PKG, CLS));
        assertTrue(bob.bot.isClassExist(PROJECT, PKG, CLS2));
        assertFalse(carl.bot.isClassExist(PROJECT, PKG, CLS));
        assertTrue(carl.bot.isClassExist(PROJECT, PKG, CLS2));
    }

    @Test
    public void testDeleteFile() throws RemoteException {
        alice.bot.deleteClass(PROJECT, PKG, CLS);
        bob.bot.waitUntilClassNotExist(PROJECT, PKG, CLS);
        assertFalse(bob.bot.isClassExist(PROJECT, PKG, CLS));
        carl.bot.waitUntilClassNotExist(PROJECT, PKG, CLS);
        assertFalse(carl.bot.isClassExist(PROJECT, PKG, CLS));
    }

    @Test
    public void testNewPkgAndClass() throws CoreException, IOException {
        alice.bot.newPackage(PROJECT, PKG2);
        bob.bot.waitUntilPkgExist(PROJECT, PKG2);
        carl.bot.waitUntilPkgExist(PROJECT, PKG2);
        assertTrue(bob.bot.isPkgExist(PROJECT, PKG2));
        assertTrue(carl.bot.isPkgExist(PROJECT, PKG2));

        alice.bot.newClass(PROJECT, PKG2, CLS);
        bob.bot.waitUntilClassExist(PROJECT, PKG2, CLS);
        carl.bot.waitUntilClassExist(PROJECT, PKG2, CLS);
        assertTrue(bob.bot.isClassExist(PROJECT, PKG2, CLS));
        assertTrue(carl.bot.isClassExist(PROJECT, PKG2, CLS));

        alice.bot.setTextInJavaEditorWithSave(BotConfiguration.CONTENTPATH, PROJECT,
            PKG2, CLS);
        String clsContentOfAlice = alice.bot
            .getClassContent(PROJECT, PKG2, CLS);
        carl.bot.waitUntilClassContentsSame(PROJECT, PKG2, CLS,
            clsContentOfAlice);
        bob.bot.waitUntilClassContentsSame(PROJECT, PKG2, CLS,
            clsContentOfAlice);
        String clsContentOfBob = bob.bot.getClassContent(PROJECT, PKG2, CLS);
        String clsContentOfCarl = carl.bot.getClassContent(PROJECT, PKG2, CLS);
        assertTrue(clsContentOfBob.equals(clsContentOfAlice));
        assertTrue(clsContentOfCarl.equals(clsContentOfAlice));
    }

    @Test
    public void testMoveClass() throws RemoteException {
        alice.bot.newPackage(PROJECT, PKG2);
        alice.bot.newClass(PROJECT, PKG2, CLS2);
        alice.bot.moveClassTo(PROJECT, PKG2, CLS2, PROJECT, PKG);
        bob.bot.waitUntilClassExist(PROJECT, PKG, CLS2);
        carl.bot.waitUntilClassExist(PROJECT, PKG, CLS2);
        assertTrue(bob.bot.isClassExist(PROJECT, PKG, CLS2));
        assertFalse(bob.bot.isClassExist(PROJECT, PKG2, CLS2));
        assertTrue(carl.bot.isClassExist(PROJECT, PKG, CLS2));
        assertFalse(carl.bot.isClassExist(PROJECT, PKG2, CLS2));
    }

    @Test
    public void testRenamePkg() throws RemoteException {
        alice.bot.renamePkg(PKG2, PROJECT, "src", PKG);
        bob.bot.waitUntilPkgExist(PROJECT, PKG2);
        carl.bot.waitUntilPkgExist(PROJECT, PKG2);
        assertFalse(bob.bot.isPkgExist(PROJECT, PKG));
        assertTrue(bob.bot.isPkgExist(PROJECT, PKG2));
        assertFalse(carl.bot.isPkgExist(PROJECT, PKG));
        assertTrue(carl.bot.isPkgExist(PROJECT, PKG2));
    }

    @Test
    public void testDeletePkg() throws RemoteException {
        // alice.renameFile(
        alice.bot.deletePkg(PROJECT, PKG);
        bob.bot.waitUntilPkgNotExist(PROJECT, PKG);
        carl.bot.waitUntilPkgNotExist(PROJECT, PKG);
        assertFalse(bob.bot.isPkgExist(PROJECT, PKG));
        assertFalse(carl.bot.isPkgExist(PROJECT, PKG));
    }
}
