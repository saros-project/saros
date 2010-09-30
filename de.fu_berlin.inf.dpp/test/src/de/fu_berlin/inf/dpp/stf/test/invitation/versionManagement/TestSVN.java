package de.fu_berlin.inf.dpp.stf.test.invitation.versionManagement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.test.InitMusician;

public class TestSVN {

    protected static Musician alice = InitMusician.newAlice();
    protected static Musician bob = InitMusician.newBob();
    protected static String PROJECT = BotConfiguration.PROJECTNAME_SVN;
    private static final String CLS_PATH = BotConfiguration.PROJECTNAME_SVN
        + "/src/org/eclipsecon/swtbot/example/MyFirstTest01.java";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        alice.bot.importProjectFromSVN(BotConfiguration.SVN_URL);
        alice.buildSessionSequential(BotConfiguration.PROJECTNAME_SVN_TRUNK,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT_WITH_VCS, bob);
        alice.bot.waitUntilSessionOpenBy(bob.state);
    }

    @AfterClass
    public static void tearDownBob() throws RemoteException {
        bob.bot.resetSaros();
    }

    @AfterClass
    public static void tearDownAlice() throws RemoteException {
        alice.bot.resetSaros();
    }

    @After
    public void resetWorkbench() throws RemoteException {
        alice.bot.resetWorkbench();
        bob.bot.resetWorkbench();
    }

    @Test
    public void testCheckout() throws RemoteException {
        // Alice shares project "examples" with VCS support with Bob.
        // Bob accepts invitation, new project "examples".
        // Make sure Bob has joined the session.
        // Make sure Bob has checked out project test from SVN.
        alice.state.isDriver(alice.jid);
        alice.state.isParticipant(bob.jid);
        bob.state.isObserver(bob.jid);
        assertTrue(bob.bot.isInSVN());

    }

    @Test
    public void testSwitch() throws RemoteException {
        // Alice shares project "test" with VCS support with Bob
        // Bob accepts invitation, new project "test".
        // Make sure Bob has joined the session.
        // Alice switches to branch "testing".
        // Make sure Bob is switched to branch "testing".

        alice.bot.switchToTag();
        bob.bot.waitUntilShellCloses("Saros running VCS operation");
        assertTrue(alice.bot.getURLOfRemoteResource(CLS_PATH).equals(
            bob.bot.getURLOfRemoteResource(CLS_PATH)));

    }

    @Test
    public void testDisconnectAndConnect() throws RemoteException {

        // Alice shares project "test" with VCS support with Bob
        // Bob accepts invitation, new project "test".
        // Make sure Bob has joined the session.
        // Alice disconnects project "test" from SVN.
        // Make sure Bob is disconnected.
        alice.bot.disConnectSVN();
        bob.bot.waitUntilProjectNotInSVN(BotConfiguration.PROJECTNAME_SVN);
        assertFalse(bob.bot.isInSVN());

        alice.bot.connectSVN();
        bob.bot.waitUntilProjectInSVN(BotConfiguration.PROJECTNAME_SVN);
        assertTrue(bob.bot.isInSVN());

    }

    @Test
    public void testUpdate() throws RemoteException {
        // Alice shares project "test" with VCS support with Bob
        // Bob accepts invitation, new project "test".
        // Make sure Bob has joined the session.
        // Alice updates the entire project to the older revision Y (< HEAD).
        // Make sure Bob's revision of "test" is Y.

        alice.bot.switchToOtherRevision();
        bob.bot.waitUntilShellCloses("Saros running VCS operation");
        assertTrue(alice.bot.getURLOfRemoteResource(CLS_PATH).equals(
            bob.bot.getURLOfRemoteResource(CLS_PATH)));
    }

    @Test
    public void testUpdateSingleFile() throws RemoteException {
        // Alice shares project "test" with VCS support with Bob
        // Bob accepts invitation, new project "test".
        // Make sure Bob has joined the session.
        // Alice updates the file Main.java to the older revision Y (< HEAD).
        // Make sure Bob's revision of file "src/main/Main.java" is Y.
        // Make sure Bob's revision of project "test" is HEAD.
        alice.bot.switchToOtherRevision(CLS_PATH);
        bob.bot.waitUntilShellCloses("Saros running VCS operation");
        assertTrue(alice.bot.getURLOfRemoteResource(CLS_PATH).equals(
            bob.bot.getURLOfRemoteResource(CLS_PATH)));
    }

    @Test
    public void testRevert() throws RemoteException {
        // Alice shares project "test" with VCS support with Bob
        // Bob accepts invitation, new project "test".
        // Make sure Bob has joined the session.
        // Alice deletes the file Main.java.
        // Make sure Bob has no file Main.java.
        // Alice reverts the project "test".
        // Make sure Bob has the file Main.java.
        alice.bot.deleteProject(CLS_PATH);
        bob.bot.sleep(1000);
        assertFalse(bob.bot.isResourceExist(CLS_PATH));
        alice.bot.revert();
        bob.bot.sleep(1000);
        assertTrue(bob.bot.isResourceExist(CLS_PATH));
    }
}
