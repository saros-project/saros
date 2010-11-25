package de.fu_berlin.inf.dpp.stf.client.test.testcases.fileFolderOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestSVNStateUpdates extends STFTest {

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
    public void testSwitch() throws RemoteException {
        alice.pEV.switchToAnotherBranchOrTag(SVN_PROJECT, SVN_TAG_URL);
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
    public void testDisconnectAndConnect() throws RemoteException {
        alice.pEV.disConnect(SVN_PROJECT);
        bob.pEV.waitUntilProjectNotInSVN(SVN_PROJECT);
        assertFalse(bob.pEV.isInSVN(SVN_PROJECT));
        alice.pEV.shareProjectWithSVNWhichIsConfiguredWithSVNInfos(SVN_PROJECT,
            SVN_URL);
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
        bob.pEV.waitUntilProjectInSVN(SVN_PROJECT);
        assertTrue(bob.pEV.isInSVN(SVN_PROJECT));
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
    public void testUpdate() throws RemoteException {
        alice.pEV.switchProjectToAnotherRevision(SVN_PROJECT, "115");
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
        assertTrue(alice.pEV.getURLOfRemoteResource(SVN_PROJECT).equals(
            bob.pEV.getURLOfRemoteResource(SVN_PROJECT)));
        alice.pEV.switchProjectToAnotherRevision(SVN_PROJECT, "116");
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
    public void testUpdateSingleFile() throws RemoteException {
        alice.pEV.switchClassToAnotherRevision(SVN_PROJECT, SVN_PKG, SVN_CLS,
            "102");
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
        assertTrue(alice.pEV.getReversion(SVN_CLS_PATH).equals("102"));
        bob.pEV.waitUntilReversionIsSame(SVN_CLS_PATH, "102");
        assertTrue(bob.pEV.getReversion(SVN_CLS_PATH).equals("102"));
        bob.pEV.waitUntilReversionIsSame(SVN_PROJECT, "116");
        assertTrue(bob.pEV.getReversion(SVN_PROJECT).equals("116"));
        alice.pEV.switchClassToAnotherRevision(SVN_PROJECT, SVN_PKG, SVN_CLS,
            "116");
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
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
    public void testRevert() throws RemoteException {
        alice.pEV.deleteProject(SVN_CLS_PATH);
        bob.pEV.waitUntilClassNotExist(SVN_PROJECT, SVN_PKG, SVN_CLS);
        assertFalse(bob.pEV.isFileExist(SVN_CLS_PATH));
        alice.pEV.revert(SVN_PROJECT);
        bob.pEV.waitUntilClassExist(SVN_PROJECT, SVN_PKG, SVN_CLS);
        assertTrue(bob.pEV.isFileExist(SVN_CLS_PATH));
    }

}
