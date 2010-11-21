package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class TestShareProjectUsingExistingProject extends STFTest {

    private static Musician alice;
    private static Musician bob;

    @BeforeClass
    public static void initMusicians() {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
    }

    @Before
    public void setUpAlice() throws RemoteException {
        alice.pEV.newJavaProjectWithClass(PROJECT, PKG, CLS);
    }

    @Before
    public void setUpBob() throws RemoteException {
        bob.pEV.newJavaProjectWithClass(PROJECT, PKG, CLS2);
    }

    @After
    public void cleanUp() throws RemoteException, InterruptedException {
        alice.leaveSessionFirst(bob);
        alice.pEV.deleteProject(PROJECT);
        bob.pEV.deleteProject(PROJECT);
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
        assertTrue(bob.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS2)));
        bob.typeOfSharingProject = SarosConstant.USE_EXISTING_PROJECT;
        alice.shareProjectWithDone(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        assertFalse(bob.pEV.isFileExist(getClassPath(PROJECT2, PKG, CLS)));
        assertFalse(bob.pEV.isProjectExist(PROJECT2));

    }

    @Test
    public void testShareProjectUsingExistingProjectWithCancelLocalChange()
        throws RemoteException {
        bob.typeOfSharingProject = SarosConstant.USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE;
        alice.shareProjectWithDone(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        assertTrue(bob.pEV.isWIndowSessionInvitationActive());
        bob.pEV
            .confirmPageTwoOfWizardSessionInvitationUsingExistProjectWithCopy(PROJECT);

        assertTrue(bob.pEV.isProjectExist(PROJECT));
        assertTrue(bob.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS2)));
        assertTrue(bob.pEV.isProjectExist(PROJECT + " 2"));
        assertTrue(bob.pEV.isFileExist(getClassPath(PROJECT + " 2", PKG, CLS)));
        bob.pEV.deleteProject(PROJECT + " 2");

    }

    @Test
    public void testShareProjectUsingExistingProjectWithCopy()
        throws RemoteException {
        bob.typeOfSharingProject = SarosConstant.USE_EXISTING_PROJECT_WITH_COPY;
        alice.shareProjectWithDone(BotConfiguration.PROJECTNAME,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        assertTrue(bob.pEV.isProjectExist(PROJECT));
        assertTrue(bob.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS2)));
        assertTrue(bob.pEV.isProjectExist(PROJECT + " 2"));
        assertTrue(bob.pEV.isFileExist(getClassPath(PROJECT + " 2", PKG, CLS)));

        bob.pEV.deleteProject(PROJECT + " 2");

    }
}
