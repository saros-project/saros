package de.fu_berlin.inf.dpp.stf.test.invitation.versionManagement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.RMISwtbot.saros.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.RMISwtbot.saros.Musician;
import de.fu_berlin.inf.dpp.stf.RMISwtbot.saros.SarosConstant;
import de.fu_berlin.inf.dpp.stf.test.InitMusician;

public class TestSVN {

    protected static Musician alice;
    protected static Musician bob;

    protected static String PROJECT = BotConfiguration.PROJECTNAME_SVN;

    private static final String CLS_PATH = BotConfiguration.PROJECTNAME_SVN
        + "/src/org/eclipsecon/swtbot/example/MyFirstTest01.java";

    @BeforeClass
    public static void initMusicians() throws Exception {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        alice.bot.importProjectFromSVN(BotConfiguration.SVN_URL);
        alice.buildSessionSequential(BotConfiguration.PROJECTNAME_SVN_TRUNK,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT_WITH_VCS, bob);
        alice.bot.waitUntilSessionOpenBy(bob.state);
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.bot.resetSaros();
        alice.bot.resetSaros();
    }

    @After
    public void cleanUp() throws RemoteException {
        alice.bot.resetWorkbench();
        bob.bot.resetWorkbench();
    }

    /**
     * <ol>
     * <li>Alice shares project "test" with VCS support with Bob.</li>
     * <li>Bob accepts invitation, new project "test".</li>
     * <li>Make sure Bob has joined the session.</li>
     * <li>Make sure Bob has checked out project test from SVN.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testCheckout() throws RemoteException {
        alice.state.isDriver(alice.jid);
        alice.state.isParticipant(bob.jid);
        bob.state.isObserver(bob.jid);
        assertTrue(bob.bot.isInSVN());

    }

    /**
     * <ol>
     * <li>Alice shares project "test" with VCS support with Bob.</li>
     * <li>Bob accepts invitation, new project "test".</li>
     * <li>Make sure Bob has joined the session.</li>
     * <li>Alice switches to branch "testing".</li>
     * <li>Make sure Bob is switched to branch "testing".</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testSwitch() throws RemoteException {

        alice.bot.switchToTag();
        bob.bot.waitUntilShellCloses("Saros running VCS operation");
        assertTrue(alice.bot.getURLOfRemoteResource(CLS_PATH).equals(
            bob.bot.getURLOfRemoteResource(CLS_PATH)));

    }

    /**
     * <ol>
     * <li>Alice shares project "test" with VCS support with Bob.</li>
     * <li>Bob accepts invitation, new project "test".</li>
     * <li>Make sure Bob has joined the session.</li>
     * <li>Alice disconnects project "test" from SVN.</li>
     * <li>Make sure Bob is disconnected.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testDisconnectAndConnect() throws RemoteException {
        alice.bot.disConnectSVN();
        bob.bot.waitUntilProjectNotInSVN(BotConfiguration.PROJECTNAME_SVN);
        assertFalse(bob.bot.isInSVN());

        alice.bot.connectSVN();
        bob.bot.waitUntilProjectInSVN(BotConfiguration.PROJECTNAME_SVN);
        assertTrue(bob.bot.isInSVN());
    }

    /**
     * <ol>
     * <li>Alice shares project "test" with VCS support with Bob.</li>
     * <li>Bob accepts invitation, new project "test".</li>
     * <li>Make sure Bob has joined the session.</li>
     * <li>Alice updates the entire project to the older revision Y (< HEAD).</li>
     * <li>Make sure Bob's revision of "test" is Y.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testUpdate() throws RemoteException {
        alice.bot.switchToOtherRevision();
        bob.bot.waitUntilShellCloses("Saros running VCS operation");
        assertTrue(alice.bot.getURLOfRemoteResource(CLS_PATH).equals(
            bob.bot.getURLOfRemoteResource(CLS_PATH)));
    }

    /**
     * <ol>
     * <li>Alice shares project "test" with VCS support with Bob.</li>
     * <li>Bob accepts invitation, new project "test".</li>
     * <li>Make sure Bob has joined the session.</li>
     * <li>Alice updates the file Main.java to the older revision Y (< HEAD).</li>
     * <li>Make sure Bob's revision of file "src/main/Main.java" is Y.</li>
     * <li>Make sure Bob's revision of project "test" is HEAD.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testUpdateSingleFile() throws RemoteException {
        alice.bot.switchToOtherRevision(CLS_PATH);
        bob.bot.waitUntilShellCloses("Saros running VCS operation");
        assertTrue(alice.bot.getURLOfRemoteResource(CLS_PATH).equals(
            bob.bot.getURLOfRemoteResource(CLS_PATH)));
    }

    /**
     * <ol>
     * <li>Alice shares project "test" with VCS support with Bob.</li>
     * <li>Bob accepts invitation, new project "test".</li>
     * <li>Make sure Bob has joined the session.</li>
     * <li>Alice deletes the file Main.java.</li>
     * <li>Make sure Bob has no file Main.java.</li>
     * <li>Alice reverts the project "test".</li>
     * <li>Make sure Bob has the file Main.java.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testRevert() throws RemoteException {
        alice.bot.deleteProject(CLS_PATH);
        bob.bot.sleep(1000);
        assertFalse(bob.bot.isResourceExist(CLS_PATH));
        alice.bot.revert();
        bob.bot.sleep(1000);
        assertTrue(bob.bot.isResourceExist(CLS_PATH));
    }
}
