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

    @BeforeClass
    public static void initMusicians() {
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

    @After
    public void cleanUp() throws RemoteException, InterruptedException {
        alice.leaveSessionFirst(bob);
        alice.pEV.deleteProject(PROJECT1);
        bob.pEV.deleteProject(PROJECT1);
        bob.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    @Test
    public void testShareProjectUsingExistingProject() throws RemoteException {
        assertTrue(bob.pEV.isFileExist(getClassPath(PROJECT1, PKG1, CLS2)));
        bob.typeOfSharingProject = USE_EXISTING_PROJECT;
        alice.shareProjectWithDone(PROJECT1, CONTEXT_MENU_SHARE_PROJECT, bob);
        assertFalse(bob.pEV.isFileExist(getClassPath(PROJECT2, PKG1, CLS1)));
        assertFalse(bob.pEV.isProjectExist(PROJECT2));

    }

    @Test
    public void testShareProjectUsingExistingProjectWithCancelLocalChange()
        throws RemoteException {
        bob.typeOfSharingProject = USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE;
        alice.shareProjectWithDone(PROJECT1, CONTEXT_MENU_SHARE_PROJECT, bob);
        assertTrue(bob.pEV.isWIndowSessionInvitationActive());
        bob.pEV
            .confirmPageTwoOfWizardSessionInvitationUsingExistProjectWithCopy(PROJECT1);

        assertTrue(bob.pEV.isProjectExist(PROJECT1));
        assertTrue(bob.pEV.isFileExist(getClassPath(PROJECT1, PKG1, CLS2)));
        assertTrue(bob.pEV.isProjectExist(PROJECT1 + " 2"));
        assertTrue(bob.pEV
            .isFileExist(getClassPath(PROJECT1 + " 2", PKG1, CLS1)));
        bob.pEV.deleteProject(PROJECT1 + " 2");

    }

    @Test
    public void testShareProjectUsingExistingProjectWithCopy()
        throws RemoteException {
        bob.typeOfSharingProject = USE_EXISTING_PROJECT_WITH_COPY;
        alice.shareProjectWithDone(PROJECT1, CONTEXT_MENU_SHARE_PROJECT, bob);
        assertTrue(bob.pEV.isProjectExist(PROJECT1));
        assertTrue(bob.pEV.isFileExist(getClassPath(PROJECT1, PKG1, CLS2)));
        assertTrue(bob.pEV.isProjectExist(PROJECT1 + " 2"));
        assertTrue(bob.pEV
            .isFileExist(getClassPath(PROJECT1 + " 2", PKG1, CLS1)));

        bob.pEV.deleteProject(PROJECT1 + " 2");

    }
}
