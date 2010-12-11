package de.fu_berlin.inf.dpp.stf.client.test.helpers;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import de.fu_berlin.inf.dpp.stf.client.Musician;

public class STFTest {

    public enum TypeOfTester {
        ALICE, BOB, CARL, DAVE, EDNA
    }

    public enum TypeOfCreateProject {
        NEW_PROJECT, EXIST_PROJECT, EXIST_PROJECT_WITH_COPY, EXIST_PROJECT_WITH_COPY_AFTER_CANCEL_LOCAL_CHANGE
    }

    public enum TypeOfShareProject {
        SHARE_PROJECT, SHARE_PROJECT_PARTICALLY, ADD_SESSION
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

    public static List<Musician> activeTesters = new ArrayList<Musician>();

    public static List<Musician> initTesters(TypeOfTester... testers)
        throws RemoteException {
        List<Musician> result = new ArrayList<Musician>();
        for (TypeOfTester t : testers) {
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
        activeTesters = result;
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
     * </ul>
     * 
     * @throws RemoteException
     */
    public static void setUpWorkbenchs() throws RemoteException {
        for (Musician musician : activeTesters) {
            musician.workbench.activateEclipseShell();
            musician.workbench.setUpWorkbench();
            musician.workbench.closeWelcomeView();
            musician.mainMenu.openPerspectiveJava();
            musician.workbench.closeUnnecessaryViews();
        }
    }

    public static void setUpSaros() throws RemoteException {
        for (Musician musician : activeTesters) {
            musician.mainMenu.disableAutomaticReminder();
            musician.workbench.openSarosViews();
            musician.rosterV.connect(musician.jid, musician.password);
        }
    }

    public static void setUpSession(Musician host, Musician... invitees)
        throws RemoteException, InterruptedException {
        host.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        host.buildSessionDoneConcurrently(PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            invitees);
    }

    public static void reBuildSession(Musician host, Musician... invitees)
        throws RemoteException {
        if (!host.sessionV.isInSession()) {
            for (Musician musician : invitees) {
                host.buildSessionDoneSequentially(PROJECT1,
                    TypeOfShareProject.SHARE_PROJECT,
                    TypeOfCreateProject.EXIST_PROJECT, musician);
            }
        }
    }

    public static void createProjectByActiveTesters() throws RemoteException {
        for (Musician tester : activeTesters) {
            tester.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        }
    }

    public static void deleteProjectsByActiveTesters() throws RemoteException {
        for (Musician tester : activeTesters) {
            tester.state.deleteAllProjects();
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

    public static void resetSaros() throws RemoteException {
        for (Musician musician : activeTesters) {
            if (musician != null)
                musician.rosterV.resetAllBuddyName();
        }
        // host.leaveSessionHostFirstDone(invitees);
        for (Musician musician : activeTesters) {
            if (musician != null) {
                musician.rosterV.disconnectGUI();
                musician.state.deleteAllProjects();
            }
        }
        resetAllBots();
    }

    public static void resetWorkbenches() throws RemoteException {
        for (Musician musician : activeTesters) {
            if (musician != null)
                musician.workbench.resetWorkbench();
        }
    }

    public static void deleteFolders(String... folders) throws RemoteException {
        for (Musician tester : activeTesters) {
            for (String folder : folders) {
                if (tester.pEV.isFolderExist(PROJECT1, folder))
                    tester.pEV.deleteFolder(PROJECT1, folder);
            }
        }
    }

    public static void resetAllBots() {
        alice = bob = carl = dave = edna = null;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        resetSaros();
    }

    @Before
    public void before() throws Exception {
        resetWorkbenches();
    }

    @After
    public void after() throws Exception {
        resetWorkbenches();
        resetSaros();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        resetSaros();
    }

}
