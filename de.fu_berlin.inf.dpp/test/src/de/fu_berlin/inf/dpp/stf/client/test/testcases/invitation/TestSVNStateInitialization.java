package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestSVNStateInitialization extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Alice has the project {@link STFTest#SVN_PROJECT_COPY}, which is
     * checked out from SVN</li>
     * <li>repository: {@link STFTest#SVN_REPOSITORY_URL}</li>
     * <li>path: {@link STFTest#SVN_PROJECT_PATH}</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void initMusicians() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        if (!alice.pEV.isProjectExist(SVN_PROJECT_COPY)) {
            alice.pEV.newJavaProject(SVN_PROJECT_COPY);
            alice.pEV.shareProjectWithSVNUsingSpecifiedFolderName(
                SVN_PROJECT_COPY, SVN_REPOSITORY_URL, SVN_PROJECT_PATH);
        }
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    /**
     * Preconditions:
     * <ol>
     * <li>Alice copied {@link STFTest#SVN_PROJECT_COPY} to
     * {@link STFTest#SVN_PROJECT}.</li>
     * </ol>
     * Only SVN_PROJECT is used in the tests. Copying from SVN_PROJECT_COPY is
     * faster than checking out the project for every test.
     * 
     * @throws RemoteException
     */
    @Before
    public void setUp() throws RemoteException {
        alice.pEV.copyProject(SVN_PROJECT_COPY, SVN_PROJECT);
    }

    @After
    public void cleanUp() throws RemoteException {
        alice.workbench.resetWorkbench();
        bob.workbench.resetWorkbench();
    }

    /**
     * Steps: <li>Alice invites Bob.</li>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of SVN_PROJECT is managed with SVN.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testSimpleCheckout() throws RemoteException {
        alice.shareProjectWithDone(SVN_PROJECT,
            CONTEXT_MENU_SHARE_PROJECT_WITH_VCS, bob);
        alice.sessionV.waitUntilSessionOpenBy(bob.state);
        assertTrue(alice.state.isDriver(alice.jid));
        assertTrue(alice.state.isParticipant(bob.jid));
        assertTrue(bob.state.isObserver(bob.jid));
        assertTrue(bob.pEV.isInSVN(SVN_PROJECT));
    }

    @Test
    public void testCheckoutWithUpdate() throws RemoteException {
        alice.shareProjectWithDone(SVN_PROJECT,
            CONTEXT_MENU_SHARE_PROJECT_WITH_VCS, bob);
        alice.sessionV.waitUntilSessionOpenBy(bob.state);
        assertTrue(alice.state.isDriver(alice.jid));
        assertTrue(alice.state.isParticipant(bob.jid));
        assertTrue(bob.state.isObserver(bob.jid));
        assertTrue(bob.pEV.isInSVN(SVN_PROJECT));
    }

}
