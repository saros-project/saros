package de.fu_berlin.inf.dpp.stf.client.test.helpers;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.dpp.stf.client.Musician;

public class STFTest {

    public enum tester {
        ALICE, BOB, CARL, DAVE, EDNA
    }

    /* Musicians */
    public static Musician alice;
    public static Musician bob;
    public static Musician carl;
    public static Musician dave;
    public static Musician edna;

    // views
    protected final static String SESSION_VIEW = "Shared Project Session";
    protected final static String CHAT_VIEW = "Chat View";
    protected final static String ROSTER_VIEW = "Roster";
    protected final static String REMOTE_SCREEN_VIEW = "Remote Screen";

    // Title of Buttons
    protected final static String YES = "Yes";
    protected final static String OK = "OK";
    protected final static String NO = "No";
    protected final static String CANCEL = "Cancel";
    protected final static String FINISH = "Finish";
    protected final static String NEXT = "Next >";

    /* Project name */
    public static final String PROJECT1 = "Foo_Saros1";
    protected static final String PROJECT1_COPY = "copy_of_FOO_Saros1";
    /** Name chosen by Saros if {@link STFTest#PROJECT1} already exists. */
    public static final String PROJECT1_NEXT = "Foo_Saros 1";
    public static final String PROJECT2 = "Foo_Saros2";
    public static final String PROJECT3 = "Foo_Saros3";

    /* Folder name */
    public static final String FOLDER1 = "MyFolder";
    public static final String FOLDER2 = "MyFolder2";

    /* File name */
    public static final String FILE1 = "MyFile.xml";
    public static final String FILE2 = "MyFile2.xml";

    /* Package name */
    public static final String PKG1 = "my.pkg";
    public static final String PKG2 = "my.pkg2";
    public static final String PKG3 = "my.pkg3";

    /* class name */
    public static final String CLS1 = "MyClass";
    public static final String CLS2 = "MyClass2";
    public static final String CLS3 = "MyClass3";

    /* content path */
    public static final String CP1 = "test/STF/" + CLS1 + ".java";
    public static final String CP2 = "test/STF/" + CLS2 + ".java";
    public static final String CP3 = "test/STF/" + CLS3 + ".java";
    public static final String CP1_CHANGE = "test/STF/" + CLS1 + "Change"
        + ".java";
    public static final String CP2_CHANGE = "test/STF/" + CLS2 + "Change"
        + ".java";

    public static final String ROLE_NAME = " (Driver)";
    public static final String OWN_CONTACT_NAME = "You";

    /* SVN infos */
    protected static final String SVN_REPOSITORY_URL = "http://saros-build.imp.fu-berlin.de/svn/saros";
    protected static final String SVN_PROJECT = "stf_test_project";
    protected static final String SVN_PROJECT_COPY = "copy_of_stf_test_project";
    protected static final String SVN_PROJECT_PATH = "/stf_tests/stf_test_project";
    protected static final String SVN_PROJECT_URL_SWITCHED = SVN_REPOSITORY_URL
        + "/stf_tests/stf_test_project_copy";
    protected static final String SVN_PKG = "pkg";
    protected static final String SVN_CLS1 = "Test";
    protected static final String SVN_CLS1_FULL_PATH = "/stf_test_project/src/pkg/Test.java";
    protected static final String SVN_CLS1_SWITCHED_URL = "http://saros-build.imp.fu-berlin.de/svn/saros/stf_tests/stf_test_project_copy/src/pkg/Test.java";
    /** Initial commit in stf_test_project. */
    protected static final String SVN_CLS1_REV1 = "2735";
    /** copy from stf_test_project to stf_test_project_copy */
    protected static final String SVN_CLS1_REV2 = "2736";
    /** modified in stf_test_project_copy */
    protected static final String SVN_CLS1_REV3 = "2737";
    /** modified in stf_test_project_copy */
    protected static final String SVN_CLS1_REV4 = "2767";

    /*
     * Contextmenu "Saros"
     */
    public final static String CONTEXT_MENU_SHARE_PROJECT_WITH_VCS = "Share project...";
    public final static String CONTEXT_MENU_SHARE_PROJECT = "Share project...";

    public final static String ID_JAVA_EDITOR = "org.eclipse.jdt.ui.CompilationUnitEditor";
    public final static String ID_TEXT_EDITOR = "org.eclipse.ui.texteditor";

    public final static int CREATE_NEW_PROJECT = 1;
    public final static int USE_EXISTING_PROJECT = 2;
    public final static int USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE = 3;
    public final static int USE_EXISTING_PROJECT_WITH_COPY = 4;

    public static List<Musician> acitveTesters = new ArrayList<Musician>();

    public static List<Musician> initTesters(tester... testers)
        throws RemoteException {
        List<Musician> result = new ArrayList<Musician>();
        for (tester t : testers) {
            switch (t) {
            case ALICE:
                alice = InitMusician.newAlice();
                result.add(alice);
                break;
            case BOB:
                bob = InitMusician.newBob();
                result.add(bob);
                break;
            case CARL:
                carl = InitMusician.newCarl();
                result.add(carl);
                break;
            case DAVE:
                dave = InitMusician.newDave();
                result.add(dave);
                break;
            case EDNA:
                edna = InitMusician.newEdna();
                result.add(edna);
                break;
            default:
                break;
            }
        }
        acitveTesters = result;
        return result;
    }

    /**
     * <ul>
     * <li>activate saros-instance</li>
     * <li>close all opened popup windows</li>
     * <li>close all opened editors</li>
     * <li>delete all existed projects</li>
     * <li>close welcome view, if it is open</li>
     * <li>open java perspective</li>
     * <li>close all unnecessary views</li>
     * <li>open saros views</li>
     * <li>xmpp connection</li>
     * </ul>
     * 
     * @throws RemoteException
     */
    public static void setUpWorkbenchs() throws RemoteException {
        for (Musician musician : acitveTesters) {
            musician.workbench.activateEclipseShell();
            musician.workbench.setUpWorkbench();
            musician.workbench.closeWelcomeView();
            musician.mainMenu.openPerspectiveJava();
            musician.workbench.closeUnnecessaryViews();

        }
    }

    public static void setUpSaros() throws RemoteException {
        for (Musician musician : acitveTesters) {
            musician.mainMenu.disableAutomaticReminder();
            musician.workbench.openSarosViews();
            musician.rosterV.connect(musician.jid, musician.password);
        }
    }

    public static void setUpSession(Musician host, Musician... invitees)
        throws RemoteException, InterruptedException {
        host.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        host.buildSessionConcurrentlyDone(PROJECT1, CONTEXT_MENU_SHARE_PROJECT,
            invitees);
    }

    public static void reBuildSession(Musician host, Musician... invitees)
        throws RemoteException {
        if (!host.sessionV.isInSession()) {
            for (Musician musician : invitees) {
                musician.typeOfSharingProject = USE_EXISTING_PROJECT;
                host.buildSessionSequentially(PROJECT1,
                    CONTEXT_MENU_SHARE_PROJECT, musician);
            }

        }
    }

    public static void resetDriverRole(Musician host, Musician... invitees)
        throws RemoteException {
        for (Musician musician : invitees) {
            if (musician.sessionV.isInSession() && musician.sessionV.isDriver()) {
                host.sessionV.removeDriverRoleGUI(musician.sessionV);
            }
        }

        if (host.sessionV.isInSession() && !host.sessionV.isDriver()) {
            host.sessionV.giveDriverRoleGUI(host.sessionV);
        }
    }

    public static void resetFollowModel(Musician... activeTesters)
        throws RemoteException {
        for (Musician tester : activeTesters) {
            if (tester.sessionV.isInSession()
                && tester.sessionV.isInFollowMode()) {
                tester.sessionV.stopFollowingGUI();
            }
        }
    }

    public static void resetSaros(Musician host, Musician... invitees)
        throws RemoteException, InterruptedException {
        for (Musician musician : acitveTesters) {
            if (musician != null)
                musician.rosterV.resetAllBuddyName();
        }
        // host.leaveSessionFirst(invitees);
        for (Musician musician : acitveTesters) {
            if (musician != null) {
                musician.rosterV.disconnectGUI();
                musician.state.deleteAllProjects();
            }
        }
    }

    public static void resetWorkbenches() throws RemoteException {
        for (Musician musician : acitveTesters) {
            if (musician != null)
                musician.workbench.resetWorkbench();
        }
    }

    public static void resetAllBots() {
        alice = bob = carl = dave = edna = null;
    }
}
