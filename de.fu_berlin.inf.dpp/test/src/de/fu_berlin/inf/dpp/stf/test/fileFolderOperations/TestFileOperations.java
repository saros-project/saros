package de.fu_berlin.inf.dpp.stf.test.fileFolderOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;

public class TestFileOperations {
    protected static Musician alice;
    protected static Musician carl;
    protected static Musician bob;

    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String PKG2 = BotConfiguration.PACKAGENAME2;
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String CLS2 = BotConfiguration.CLASSNAME2;

    @BeforeClass
    public static void configureAlice() throws RemoteException,
        NotBoundException {
        alice = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        alice.initBot();
        String projectName = BotConfiguration.PROJECTNAME;
        alice.bot.newJavaProjectWithClass(projectName,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);

        bob = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        bob.initBot();

        carl = new Musician(new JID(BotConfiguration.JID_CARL),
            BotConfiguration.PASSWORD_CARL, BotConfiguration.HOST_CARL,
            BotConfiguration.PORT_CARL);
        carl.initBot();

        List<String> musicians = new LinkedList<String>();
        musicians.add(carl.getPlainJid());
        musicians.add(bob.getPlainJid());
        alice.bot.shareProjectParallel(BotConfiguration.PROJECTNAME, musicians);
        carl.bot.confirmSessionInvitationWizard(alice.getPlainJid(),
            BotConfiguration.PROJECTNAME);
        bob.bot.confirmSessionInvitationWizard(alice.getPlainJid(),
            BotConfiguration.PROJECTNAME);
        carl.followUser(alice);
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

    @AfterClass
    public static void cleanupAlice() throws RemoteException {
        carl.bot.xmppDisconnect();
        carl.bot.deleteProject(BotConfiguration.PROJECTNAME);
        bob.bot.xmppDisconnect();
        bob.bot.deleteProject(BotConfiguration.PROJECTNAME);
        alice.bot.xmppDisconnect();
        alice.bot.deleteProject(BotConfiguration.PROJECTNAME);
        carl.bot.resetWorkbench();
    }

    @Test
    public void testRenameFile() throws RemoteException {
        // alice.renameFile(
        alice.bot.renameFile(CLS2, PROJECT, "src", PKG, CLS);
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
    public void testNewPkgAndClass() throws RemoteException {
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

        carl.bot.openClass(PROJECT, PKG2, CLS);
        carl.bot.waitUntilJavaEditorActive(CLS);
        bob.bot.openClass(PROJECT, PKG2, CLS);
        bob.bot.waitUntilJavaEditorActive(CLS);
        alice.bot.setTextInJavaEditor(BotConfiguration.CONTENTPATH, PROJECT,
            PKG2, CLS);
        String textFromAlice = alice.bot
            .getTextOfJavaEditor(PROJECT, PKG2, CLS);
        carl.bot.waitUntilFileEqualWithFile(PROJECT, PKG2, CLS, textFromAlice);
        bob.bot.waitUntilFileEqualWithFile(PROJECT, PKG2, CLS, textFromAlice);
        String textFromCarl = carl.bot.getTextOfJavaEditor(PROJECT, PKG2, CLS);
        String textFromBob = bob.bot.getTextOfJavaEditor(PROJECT, PKG2, CLS);
        assertTrue(textFromCarl.equals(textFromAlice));
        assertTrue(textFromBob.equals(textFromAlice));
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
