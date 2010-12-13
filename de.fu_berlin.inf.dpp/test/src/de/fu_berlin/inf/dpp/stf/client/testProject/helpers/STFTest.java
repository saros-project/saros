package de.fu_berlin.inf.dpp.stf.client.testProject.helpers;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import de.fu_berlin.inf.dpp.stf.client.Tester;

public class STFTest {
    @Rule
    public TestName name = new TestName();

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
    public static Tester alice;
    public static Tester bob;
    public static Tester carl;
    public static Tester dave;
    public static Tester edna;

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

    /*
     * Shell title
     */
    protected final static String SESSION_INVITATION = "Session Invitation";
    protected final static String INVITATIONCANCELLED = "Invitation Cancelled";

    public final static String ID_JAVA_EDITOR = "org.eclipse.jdt.ui.CompilationUnitEditor";
    public final static String ID_TEXT_EDITOR = "org.eclipse.ui.texteditor";

    public static List<Tester> activeTesters = new ArrayList<Tester>();

    public static List<Tester> initTesters(TypeOfTester... testers)
        throws RemoteException {
        List<Tester> result = new ArrayList<Tester>();
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
        for (Tester tester : activeTesters) {
            tester.workbench.activateEclipseShell();
            tester.workbench.setUpWorkbench();
            tester.workbench.closeWelcomeView();
            tester.mainMenu.openPerspectiveJava();
            tester.workbench.closeUnnecessaryViews();
        }
    }

    public static void setUpSaros() throws RemoteException {
        for (Tester tester : activeTesters) {
            tester.mainMenu.disableAutomaticReminder();
            tester.workbench.openSarosViews();
            tester.rosterV.connect(tester.jid, tester.password);
        }
    }

    public static void setUpSession(Tester host, Tester... invitees)
        throws RemoteException, InterruptedException {
        host.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        host.buildSessionDoneConcurrently(PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            invitees);
    }

    public static void reBuildSession(Tester host, Tester... invitees)
        throws RemoteException {
        if (!host.sessionV.isInSession()) {
            for (Tester tester : invitees) {
                host.buildSessionDoneSequentially(PROJECT1,
                    TypeOfShareProject.SHARE_PROJECT,
                    TypeOfCreateProject.EXIST_PROJECT, tester);
            }
        }
    }

    public static void createProjectByActiveTesters() throws RemoteException {
        for (Tester tester : activeTesters) {
            tester.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        }
    }

    public static void deleteProjectsByActiveTesters() throws RemoteException {
        for (Tester tester : activeTesters) {
            tester.state.deleteAllProjects();
        }
    }

    public static void resetDriverRole(Tester host, Tester... invitees)
        throws RemoteException {
        for (Tester tester : invitees) {
            if (tester.sessionV.isInSession() && tester.sessionV.isDriver()) {
                host.sessionV.removeDriverRoleGUI(tester.sessionV);
            }
        }

        if (host.sessionV.isInSession() && !host.sessionV.isDriver()) {
            host.sessionV.giveDriverRoleGUI(host.sessionV);
        }
    }

    public static void resetFollowModel(Tester... activeTesters)
        throws RemoteException {
        for (Tester tester : activeTesters) {
            if (tester.sessionV.isInSession()
                && tester.sessionV.isInFollowMode()) {
                tester.sessionV.stopFollowingGUI();
            }
        }
    }

    /**
     * For all active testers, reset buddy names, disconnect, delete all
     * projects.
     * 
     * @throws RemoteException
     */
    public static void resetSaros() throws RemoteException {
        for (Tester tester : activeTesters) {
            if (tester != null) {
                tester.rosterV.resetAllBuddyName();
                tester.rosterV.disconnectGUI();
                tester.state.deleteAllProjects();
            }
        }
        resetAllBots();
    }

    public static void resetWorkbenches() throws RemoteException {
        for (Tester tester : activeTesters) {
            if (tester != null)
                tester.workbench.resetWorkbench();
        }
    }

    public static void deleteFolders(String... folders) throws RemoteException {
        for (Tester tester : activeTesters) {
            for (String folder : folders) {
                if (tester.pEV.isFolderExist(PROJECT1, folder))
                    tester.pEV.deleteFolder(PROJECT1, folder);
            }
        }
    }

    public static void resetAllBots() {
        alice = bob = carl = dave = edna = null;
        activeTesters.clear();
        assertTrue(activeTesters.isEmpty());
    }

    // @BeforeClass
    // public static void beforeClass() throws Exception {
    // setUpWorkbenchs();
    // setUpSaros();
    // }

    @Before
    public void before() throws Exception {
        //
        for (Tester m : activeTesters) {
            m.state
                .debug("\n---------------------------------------------------"
                    + "\nExecuting @Test " + name.getMethodName() + " in "
                    + getClass().getSimpleName()
                    + "\n---------------------------------------------------");
        }
    }

    @After
    public void after() throws Exception {
        resetWorkbenches();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        resetSaros();
    }

}
