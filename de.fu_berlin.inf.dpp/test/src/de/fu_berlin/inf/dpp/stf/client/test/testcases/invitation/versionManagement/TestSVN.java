package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.versionManagement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class TestSVN {

    protected static Musician alice;
    protected static Musician bob;

    protected static String PROJECT = BotConfiguration.PROJECTNAME_SVN;
    protected static String URL_TAG = "http://swtbot-examples.googlecode.com/svn/tags/eclipsecon2009";

    private static final String CLS_PATH = BotConfiguration.PROJECTNAME_SVN
        + "/src/org/eclipsecon/swtbot/example/MyFirstTest01.java";

    @BeforeClass
    public static void initMusicians() throws Exception {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        alice.mainMenu.importProjectFromSVN(BotConfiguration.SVN_URL);
        alice.shareProjectWithDone(BotConfiguration.PROJECTNAME_SVN_TRUNK,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT_WITH_VCS, bob);
        alice.sessionV.waitUntilSessionOpenBy(bob.state);
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    @After
    public void cleanUp() throws RemoteException {
        alice.workbench.resetWorkbench();
        bob.workbench.resetWorkbench();
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
        assertTrue(bob.pEV.isInSVN());

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
        alice.pEV.switchToAnotherBranchOrTag(PROJECT, URL_TAG);
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
        assertTrue(alice.state.getURLOfRemoteResource(CLS_PATH).equals(
            bob.state.getURLOfRemoteResource(CLS_PATH)));

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
        alice.pEV.disConnect(PROJECT);
        bob.pEV.waitUntilProjectNotInSVN(PROJECT);
        assertFalse(bob.pEV.isInSVN());

        alice.pEV.shareProject(PROJECT);
        bob.pEV.waitUntilProjectInSVN(PROJECT);
        assertTrue(bob.pEV.isInSVN());
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
        alice.pEV.switchToAnotherRevision(PROJECT, "115");
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
        assertTrue(alice.state.getURLOfRemoteResource(CLS_PATH).equals(
            bob.state.getURLOfRemoteResource(CLS_PATH)));
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
        alice.pEV.switchToAnotherRevision(CLS_PATH, "116");
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
        assertTrue(alice.state.getURLOfRemoteResource(CLS_PATH).equals(
            bob.state.getURLOfRemoteResource(CLS_PATH)));
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
        alice.pEV.deleteProject(CLS_PATH);
        bob.basic.sleep(1000);
        assertFalse(bob.state.isResourceExist(CLS_PATH));
        alice.pEV.revert(PROJECT);
        bob.basic.sleep(1000);
        assertTrue(bob.state.isResourceExist(CLS_PATH));
    }
}
