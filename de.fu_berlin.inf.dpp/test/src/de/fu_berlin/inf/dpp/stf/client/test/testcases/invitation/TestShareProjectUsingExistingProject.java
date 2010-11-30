package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestShareProjectUsingExistingProject extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void initMusicians() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
    }

    @Before
    public void setUpAlice() throws RemoteException {
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
    }

    @Before
    public void setUpBob() throws RemoteException {
        bob.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS2);
    }

    /**
     * Alice and bob leave the session<br/>
     * alice and bob delete the PROJECT1<br/>
     * Closes all opened popup windows and editor.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException, InterruptedException {
        alice.leaveSessionFirst(bob);
        alice.pEV.deleteProject(PROJECT1);
        bob.pEV.deleteProject(PROJECT1);
        bob.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    /**
     * Closes all opened xmppConnects, popup windows and editor.<br/>
     * Delete all existed projects.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    @Test
    public void shareProjectUsingExistingProject() throws RemoteException {
        assertFalse(bob.pEV.isClassExist(PROJECT1, PKG1, CLS1));
        assertTrue(bob.pEV.isClassExist(PROJECT1, PKG1, CLS2));
        bob.typeOfSharingProject = USE_EXISTING_PROJECT;
        alice.buildSessionSequentially(PROJECT1, CONTEXT_MENU_SHARE_PROJECT,
            bob);
        assertTrue(bob.pEV.isClassExist(PROJECT1, PKG1, CLS1));
        assertFalse(bob.pEV.isClassExist(PROJECT1, PKG1, CLS2));
    }

    @Test
    public void testShareProjectFirstUsingExistingProjectWithCancelLocalChangeThenWithCopy()
        throws RemoteException {
        assertFalse(bob.pEV.isClassExist(PROJECT1, PKG1, CLS1));
        assertTrue(bob.pEV.isClassExist(PROJECT1, PKG1, CLS2));
        bob.typeOfSharingProject = USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE;
        alice.buildSessionSequentially(PROJECT1, CONTEXT_MENU_SHARE_PROJECT,
            bob);
        assertTrue(bob.pEV.isWIndowSessionInvitationActive());
        bob.pEV
            .confirmPageTwoOfWizardSessionInvitationUsingExistProjectWithCopy(PROJECT1);

        assertTrue(bob.pEV.isProjectExist(PROJECT1));
        assertTrue(bob.pEV.isClassExist(PROJECT1, PKG1, CLS2));
        assertTrue(bob.pEV.isProjectExist(PROJECT1_NEXT));
        assertTrue(bob.pEV.isClassExist(PROJECT1_NEXT, PKG1, CLS1));
        bob.pEV.deleteProject(PROJECT1_NEXT);
    }

    @Test
    public void testShareProjectUsingExistingProjectWithCopy()
        throws RemoteException {
        bob.typeOfSharingProject = USE_EXISTING_PROJECT_WITH_COPY;
        alice.buildSessionSequentially(PROJECT1, CONTEXT_MENU_SHARE_PROJECT,
            bob);
        assertTrue(bob.pEV.isProjectExist(PROJECT1));
        assertTrue(bob.pEV.isClassExist(PROJECT1, PKG1, CLS2));
        assertTrue(bob.pEV.isProjectExist(PROJECT1_NEXT));
        assertTrue(bob.pEV.isClassExist(PROJECT1_NEXT, PKG1, CLS1));
        bob.pEV.deleteProject(PROJECT1_NEXT);

    }
}
