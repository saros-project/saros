package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.versionManagement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestSVN extends STFTest {
    private static final String REPOSITORY = "http://saros-build.imp.fu-berlin.de/svn/saros/stf_tests";
    private static final String PROJECT = "stf_test_project";
    private static final String PROJECT_COPY = "stf_test_project_copy";
    /* SVN infos */
    protected static String SVN_PROJECT = "/stf_test_project";
    protected static String SVN_PKG = "org.eclipsecon.swtbot.example";
    protected static String SVN_CLS = "MyFirstTest01";
    protected static String SVN_URL = "http://saros-build.imp.fu-berlin.de/svn/saros/trunk/dpp";
    protected static String SVN_TAG_URL = "http://swtbot-examples.googlecode.com/svn/tags/eclipsecon2009";
    protected static final String SVN_CLS_PATH = PROJECT
        + "/src/org/eclipsecon/swtbot/example/MyFirstTest01.java";

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Alice shares project "test" with VCS support with Bob</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void initMusicians() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        if (!alice.pEV.isProjectExist(PROJECT_COPY)) {
            if (!alice.pEV.isProjectExist(PROJECT)) {
                alice.mainMenu.importProjectFromSVN(REPOSITORY, SVN_PROJECT);
            }
            alice.pEV.renameJavaProject(PROJECT_COPY, PROJECT + ".*");
        }
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    @Before
    public void setUp() throws RemoteException {

        // TODO implement this method
        // alice.pEV.copyProject(PROJECT_COPY, PROJECT);
    }

    @After
    public void cleanUp() throws RemoteException {
        alice.workbench.resetWorkbench();
        bob.workbench.resetWorkbench();
    }

    /**
     * Steps: Alice has a
     * 
     * Result:
     * <ol>
     * <li>alice is driver</li>
     * <li>bob is participant</li>
     * <li>bob is in SVN</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testSimpleCheckout() throws RemoteException {
        alice.shareProjectWithDone(PROJECT,
            CONTEXT_MENU_SHARE_PROJECT_WITH_VCS, bob);
        alice.sessionV.waitUntilSessionOpenBy(bob.state);

        assertTrue(alice.state.isDriver(alice.jid));
        assertTrue(alice.state.isParticipant(bob.jid));
        assertTrue(bob.state.isObserver(bob.jid));
        assertTrue(bob.pEV.isInSVN(PROJECT));
    }

    @Test
    public void testCheckoutWithUpdate() throws RemoteException {
        // alice.pEV.switchClassToAnotherRevision(projectName, pkg, className,
        // versionID);
        alice.shareProjectWithDone(PROJECT,
            CONTEXT_MENU_SHARE_PROJECT_WITH_VCS, bob);
        alice.sessionV.waitUntilSessionOpenBy(bob.state);

        assertTrue(alice.state.isDriver(alice.jid));
        assertTrue(alice.state.isParticipant(bob.jid));
        assertTrue(bob.state.isObserver(bob.jid));
        assertTrue(bob.pEV.isInSVN(PROJECT));
    }

    // FIXME The other tests need to go to another Test class

    /**
     * Steps:
     * <ol>
     * <li>Alice switches to branch "testing".</li>
     * </ol>
     * Result:
     * <ol>
     * <li>Make sure Bob is switched to branch "testing".</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    @Ignore
    public void testSwitch() throws RemoteException {
        alice.pEV.switchToAnotherBranchOrTag(PROJECT, SVN_TAG_URL);
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
        assertTrue(alice.pEV.getURLOfRemoteResource(SVN_CLS_PATH).equals(
            bob.pEV.getURLOfRemoteResource(SVN_CLS_PATH)));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice disconnects project "test" from SVN.</li>
     * </ol>
     * Result:
     * <ol>
     * <li>Make sure Bob is disconnected.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    @Ignore
    public void testDisconnectAndConnect() throws RemoteException {
        alice.pEV.disConnect(PROJECT);
        bob.pEV.waitUntilProjectNotInSVN(PROJECT);
        assertFalse(bob.pEV.isInSVN(PROJECT));
        alice.pEV.shareProjectWithSVN(PROJECT, SVN_URL);
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
        bob.pEV.waitUntilProjectInSVN(PROJECT);
        assertTrue(bob.pEV.isInSVN(PROJECT));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice updates the entire project to the older revision Y (< HEAD)..</li>
     * </ol>
     * Result:
     * <ol>
     * <li>Bob's revision of "test" is Y</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    @Ignore
    public void testUpdate() throws RemoteException {
        alice.pEV.switchProjectToAnotherRevision(PROJECT, "115");
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
        assertTrue(alice.pEV.getURLOfRemoteResource(PROJECT).equals(
            bob.pEV.getURLOfRemoteResource(PROJECT)));
        alice.pEV.switchProjectToAnotherRevision(PROJECT, "116");
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice updates the file Main.java to the older revision Y (< HEAD)</li>
     * </ol>
     * Result:
     * <ol>
     * <li>Bob's revision of file "src/main/Main.java" is Y and Bob's revision
     * of project "test" is HEAD.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    @Ignore
    public void testUpdateSingleFile() throws RemoteException {
        // alice.pEV.switchClassToAnotherRevision(SVN_PROJECT, SVN_PKG, SVN_CLS,
        // "102");
        // bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
        // assertTrue(alice.pEV.getReversion(SVN_CLS_PATH).equals("102"));
        // bob.pEV.waitUntilReversionIsSame(SVN_CLS_PATH, "102");
        // assertTrue(bob.pEV.getReversion(SVN_CLS_PATH).equals("102"));
        // bob.pEV.waitUntilReversionIsSame(SVN_PROJECT, "116");
        // assertTrue(bob.pEV.getReversion(SVN_PROJECT).equals("116"));
        // alice.pEV.switchClassToAnotherRevision(SVN_PROJECT, SVN_PKG, SVN_CLS,
        // "116");
        // bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice deletes the file SVN_CLS_PATH</li>
     * <li>Alice reverts the project</li>
     * </ol>
     * Result:
     * <ol>
     * <li>Bob has no file SVN_CLS_PATH</li>
     * <li>Bob has the file SVN_CLS_PATH</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    @Ignore
    public void testRevert() throws RemoteException {
        alice.pEV.deleteProject(SVN_CLS_PATH);
        bob.pEV.waitUntilClassNotExist(PROJECT, SVN_PKG, SVN_CLS);
        assertFalse(bob.pEV.isFileExist(SVN_CLS_PATH));
        alice.pEV.revert(PROJECT);
        bob.pEV.waitUntilClassExist(PROJECT, SVN_PKG, SVN_CLS);
        assertTrue(bob.pEV.isFileExist(SVN_CLS_PATH));
    }
}
