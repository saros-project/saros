package de.fu_berlin.inf.dpp.stf.test.saving;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.test.InitMusician;

public class TestDriverSavesFiles {
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;

    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String CLS2 = BotConfiguration.CLASSNAME2;
    private static final String CLS3 = BotConfiguration.CLASSNAME3;

    private static final String CP = BotConfiguration.CONTENTPATH;
    private static final String CP2 = BotConfiguration.CONTENTPATH2;
    private static final String CP3 = BotConfiguration.CONTENTPATH3;

    private static Musician alice;
    private static Musician bob;
    private static Musician carl;
    private static Musician dave;
    private static Musician edna;

    // private static Musician edna;

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Carl (Observer)</li>
     * <li>Dave (Observer in Follow-Mode)</li>
     * <li>Edna (Observer in Follow-Mode)</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void initMusican() throws AccessException, RemoteException,
        InterruptedException {
        /*
         * initialize the musicians simultaneously
         */
        List<Musician> musicians = InitMusician.initMusiciansConcurrently(
            BotConfiguration.PORT_ALICE, BotConfiguration.PORT_BOB,
            BotConfiguration.PORT_CARL, BotConfiguration.PORT_DAVE,
            BotConfiguration.PORT_EDNA);
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);
        dave = musicians.get(3);
        edna = musicians.get(4);

        // if (!alice.bot.hasContactWith(bob.jid))
        // alice.bot.addContact(bob.jid, bob.bot);
        // if (!alice.bot.hasContactWith(carl.jid))
        // alice.bot.addContact(carl.jid, carl.bot);
        // if (!alice.bot.hasContactWith(dave.jid))
        // alice.bot.addContact(dave.jid, dave.bot);
        // if (!alice.bot.hasContactWith(edna.jid))
        // alice.bot.addContact(edna.jid, edna.bot);
        //
        // if (!bob.bot.hasContactWith(alice.jid))
        // bob.bot.addContact(alice.jid, alice.bot);
        // if (!bob.bot.hasContactWith(carl.jid))
        // bob.bot.addContact(carl.jid, carl.bot);
        // if (!bob.bot.hasContactWith(dave.jid))
        // bob.bot.addContact(dave.jid, dave.bot);
        // if (!bob.bot.hasContactWith(edna.jid))
        // bob.bot.addContact(edna.jid, edna.bot);
        //
        // if (!carl.bot.hasContactWith(alice.jid))
        // carl.bot.addContact(alice.jid, alice.bot);
        // if (!carl.bot.hasContactWith(bob.jid))
        // carl.bot.addContact(bob.jid, bob.bot);
        // if (!carl.bot.hasContactWith(dave.jid))
        // carl.bot.addContact(dave.jid, dave.bot);
        // if (!carl.bot.hasContactWith(edna.jid))
        // carl.bot.addContact(edna.jid, edna.bot);
        //
        // if (!dave.bot.hasContactWith(alice.jid))
        // dave.bot.addContact(alice.jid, alice.bot);
        // if (!dave.bot.hasContactWith(bob.jid))
        // dave.bot.addContact(bob.jid, bob.bot);
        // if (!dave.bot.hasContactWith(carl.jid))
        // dave.bot.addContact(carl.jid, carl.bot);
        // if (!dave.bot.hasContactWith(edna.jid))
        // dave.bot.addContact(edna.jid, edna.bot);
        //
        // if (!edna.bot.hasContactWith(alice.jid))
        // edna.bot.addContact(alice.jid, alice.bot);
        // if (!edna.bot.hasContactWith(bob.jid))
        // edna.bot.addContact(bob.jid, bob.bot);
        // if (!edna.bot.hasContactWith(carl.jid))
        // edna.bot.addContact(carl.jid, carl.bot);
        // if (!edna.bot.hasContactWith(dave.jid))
        // edna.bot.addContact(dave.jid, dave.bot);

        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.bot.newClass(PROJECT, PKG, CLS2);
        alice.bot.newClass(PROJECT, PKG, CLS3);

        /*
         * build session with bob and carl simultaneously
         */
        alice.buildSessionConcurrently(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, edna, bob, carl, dave);
        alice.bot.waitUntilNoInvitationProgress();
        dave.bot.followUser(alice.state, alice.jid);
        edna.bot.followUser(alice.state, alice.jid);
    }

    /**
     * make sure, all opened xmppConnects, popup windows and editor should be
     * closed. make sure, all existed projects should be deleted. if you need
     * some more after class condition for your tests, please add it.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.bot.resetSaros();
        carl.bot.resetSaros();
        dave.bot.resetSaros();
        edna.bot.resetSaros();
        alice.bot.resetSaros();
    }

    /**
     * make sure,all opened popup windows and editor should be closed. if you
     * need some more after condition for your tests, please add it.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.bot.resetWorkbench();
        carl.bot.resetWorkbench();
        dave.bot.resetWorkbench();
        // edna.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    /**
     * <ol>
     * <li>Alice makes changes in multiple files without saving the files.</li>
     * <li>Bob opens one of the edited files in an external editor.</li>
     * <li>Carl opens one of the edited files with the Eclipse editor.</li>
     * <li>Carl closes the file in Eclipse.</li>
     * <li>Alice makes more changes to the file that was opened by Carl and
     * saves it.</li>
     * <li>Alice closes a changed (dirty) file and confirms that it will saved.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Dave and Edna check that the modified files have a dirty flag (*).</li>
     * <li>Bob should see no changes from Alice in the external opened file.</li>
     * <li>Carl should see the changes from Alice and the dirty flag on the
     * file.</li>
     * <li>Dave and Edna verify that the dirty flag disappears. Bob verifies
     * that the file content has changed with an external editor. Carl opens the
     * file in Eclipse, verifies the correct content and closes the file.</li>
     * <li>Dave and Edna verify that the dirty flag disappears. Bob verifies
     * that the file content has changed with an external editor. Carl opens the
     * file in Eclipse, verifies the correct content and closes the file.</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     */

    @Test
    public void test1() throws IOException, CoreException {
        assertFalse(dave.bot.isClassDirty(PROJECT, PKG, CLS));
        assertFalse(dave.bot.isClassDirty(PROJECT, PKG, CLS2));
        assertFalse(dave.bot.isClassDirty(PROJECT, PKG, CLS3));
        assertFalse(edna.bot.isClassDirty(PROJECT, PKG, CLS));
        assertFalse(edna.bot.isClassDirty(PROJECT, PKG, CLS2));
        assertFalse(edna.bot.isClassDirty(PROJECT, PKG, CLS3));

        alice.bot.setTextInJavaEditor(CP, PROJECT, PKG, CLS);
        alice.bot.sleep(1000);
        // alice.bot.setTextInJavaEditor(CP2, PROJECT, PKG, CLS2);
        // alice.bot.setTextInJavaEditor(CP3, PROJECT, PKG, CLS3);
        dave.bot.waitUntilJavaEditorActive(CLS);
        String clsPkgProjectOfAlice = alice.bot.getClassContent(PROJECT, PKG,
            CLS);
        System.out.println(clsPkgProjectOfAlice);
        // String cls2PkgProjectOfAlice = alice.bot.getClassContent(PROJECT,
        // PKG,
        // CLS2);
        // String cls3PkgProjectOfAlice = alice.bot.getClassContent(PROJECT,
        // PKG,
        // CLS3);
        dave.bot.waitUntilClassContentsSame(PROJECT, PKG, CLS,
            clsPkgProjectOfAlice);
        System.out.println(dave.bot.getClassContent(PROJECT, PKG, CLS));
        assertTrue(dave.bot.isClassDirty(PROJECT, PKG, CLS));

        // dave.bot.waitUntilClassContentsSame(PROJECT, PKG, CLS2,
        // cls2PkgProjectOfAlice);
        // assertTrue(dave.bot.isClassDirty(PROJECT, PKG, CLS2));
        //
        // dave.bot.waitUntilClassContentsSame(PROJECT, PKG, CLS3,
        // cls3PkgProjectOfAlice);
        // assertTrue(dave.bot.isClassDirty(PROJECT, PKG, CLS3));

        edna.bot.waitUntilClassContentsSame(PROJECT, PKG, CLS,
            clsPkgProjectOfAlice);
        assertTrue(edna.bot.isClassDirty(PROJECT, PKG, CLS));

        // edna.bot.waitUntilClassContentsSame(PROJECT, PKG, CLS2,
        // cls2PkgProjectOfAlice);
        // assertTrue(edna.bot.isClassDirty(PROJECT, PKG, CLS2));
        //
        // edna.bot.waitUntilClassContentsSame(PROJECT, PKG, CLS3,
        // cls3PkgProjectOfAlice);
        // assertTrue(edna.bot.isClassDirty(PROJECT, PKG, CLS3));

        // bob.bot.waitUntilClassContentsSame(PROJECT, PKG, CLS,
        // clsPkgProjectOfAlice);
        // bob.bot.waitUntilClassContentsSame(PROJECT, PKG, CLS2,
        // cls2PkgProjectOfAlice);
        // bob.bot.waitUntilClassContentsSame(PROJECT, PKG, CLS3,
        // cls3PkgProjectOfAlice);
        // String clsPkgProjectOfBob = bob.bot.getClassContent(PROJECT, PKG,
        // CLS);
        // String cls2PkgProjectOfBob = bob.bot
        // .getClassContent(PROJECT, PKG, CLS2);
        // String cls3PkgProjectOfBob = bob.bot
        // .getClassContent(PROJECT, PKG, CLS3);
        // assertTrue(clsPkgProjectOfBob.equals(clsPkgProjectOfAlice));
        // assertTrue(cls2PkgProjectOfBob.equals(cls2PkgProjectOfAlice));
        // assertTrue(cls3PkgProjectOfBob.equals(cls3PkgProjectOfAlice));
        //
        bob.bot.waitUntilClassExist(PROJECT, PKG, CLS);
        bob.bot.openClassWithSystemEditor(PROJECT, PKG, CLS);
        //
        // carl.bot.waitUntilClassExist(PROJECT, PKG, CLS2);
        // carl.bot.openClassWith("Text Editor", PROJECT, PKG, CLS2);
        // carl.bot.closeJavaEditor(CLS2);
        // alice.bot.typeTextInJavaEditor(CP2, PROJECT, PKG, CLS2);
        // alice.bot.closeJavaEditor(CLS);
    }

    /**
     * please describe the test using the following form and change the
     * test'name. /** Steps:
     * <ol>
     * <li>step1.</li>
     * <li>step2.</li>
     * <li>step3.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>result1.</li>
     * <li>result2.</li>
     * <li>result3.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void test2() throws RemoteException {

    }
}
