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
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class TestShareProjectUsingExistingProject {

    private static Musician alice;
    private static Musician bob;

    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String PROJECT2 = BotConfiguration.PROJECTNAME + " 2";
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String CLS2 = BotConfiguration.CLASSNAME2;

    @BeforeClass
    public static void initMusicians() {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
    }

    @Before
    public void setUpAlice() throws RemoteException {
        alice.mainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS);
    }

    @Before
    public void setUpBob() throws RemoteException {
        bob.mainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS2);
    }

    @After
    public void cleanUp() throws RemoteException, InterruptedException {
        alice.leaveSessionFirst(bob);
        alice.eclipseState.deleteProject(PROJECT);
        bob.eclipseState.deleteProject(PROJECT);
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
        assertTrue(bob.eclipseState.existsClass(PROJECT, PKG, CLS2));
        bob.typeOfSharingProject = SarosConstant.USE_EXISTING_PROJECT;
        alice.buildSessionSequential(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        assertFalse(bob.eclipseState.existsClass(PROJECT2, PKG, CLS));
        assertFalse(bob.eclipseState.existsProject(PROJECT2));

    }

    @Test
    public void testShareProjectUsingExistingProjectWithCancelLocalChange()
        throws RemoteException {
        bob.typeOfSharingProject = SarosConstant.USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE;
        alice.buildSessionSequential(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        assertTrue(bob.eclipseWindow
            .isShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
        bob.popupWindow
            .confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(PROJECT);

        assertTrue(bob.eclipseState.existsProject(PROJECT));
        assertTrue(bob.eclipseState.existsClass(PROJECT, PKG, CLS2));
        assertTrue(bob.eclipseState.existsProject(PROJECT2));
        assertTrue(bob.eclipseState.existsClass(PROJECT2, PKG, CLS));
        bob.eclipseState.deleteProject(PROJECT2);

    }

    @Test
    public void testShareProjectUsingExistingProjectWithCopy()
        throws RemoteException {
        bob.typeOfSharingProject = SarosConstant.USE_EXISTING_PROJECT_WITH_COPY;
        alice.buildSessionSequential(BotConfiguration.PROJECTNAME,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        assertTrue(bob.eclipseState.existsProject(PROJECT));
        assertTrue(bob.eclipseState.existsClass(PROJECT, PKG, CLS2));
        assertTrue(bob.eclipseState.existsProject(PROJECT2));
        assertTrue(bob.eclipseState.existsClass(PROJECT2, PKG, CLS));

        bob.eclipseState.deleteProject(PROJECT2);

    }
}
