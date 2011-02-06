package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.client.Tester;

public class STFTest extends STF {

    protected final static Logger log = Logger.getLogger(STFTest.class);

    @Rule
    public TestName name = new TestName();

    public enum TypeOfCreateProject {
        NEW_PROJECT, EXIST_PROJECT, EXIST_PROJECT_WITH_COPY, EXIST_PROJECT_WITH_COPY_AFTER_CANCEL_LOCAL_CHANGE
    }

    public enum TypeOfShareProject {
        SHARE_PROJECT, SHARE_PROJECT_PARTICALLY, ADD_SESSION
    }

    /**********************************************
     * 
     * Tester
     * 
     **********************************************/
    public static Tester alice;
    public static Tester bob;
    public static Tester carl;
    public static Tester dave;
    public static Tester edna;

    public enum TypeOfTester {
        ALICE, BOB, CARL, DAVE, EDNA
    }

    public static List<Tester> activeTesters = new ArrayList<Tester>();

    public static List<Tester> initTesters(TypeOfTester... testers)
        throws RemoteException {
        List<Tester> result = new ArrayList<Tester>();
        for (TypeOfTester t : testers) {
            switch (t) {
            case ALICE:
                alice = ConfigTester.newAlice();
                result.add(alice);
                break;
            case BOB:
                bob = ConfigTester.newBob();
                result.add(bob);
                break;
            case CARL:
                carl = ConfigTester.newCarl();
                result.add(carl);
                break;
            case DAVE:
                dave = ConfigTester.newDave();
                result.add(dave);
                break;
            case EDNA:
                edna = ConfigTester.newEdna();
                result.add(edna);
                break;
            default:
                break;
            }
        }
        activeTesters = result;
        return result;
    }

    /**********************************************
     * 
     * test data
     * 
     **********************************************/
    /* test data for modifying account */
    public final static String SERVER = "saros-con.imp.fu-berlin.de";
    public final static String NEW_USER_NAME = "new_alice_stf";

    public final static String REGISTERED_USER_NAME = "bob_stf";

    public static final String INVALID_SERVER_NAME = "saros-con";

    // need to change, if you want to test creatAccount
    public static JID JID_TO_CREATE = new JID(
        ("test3@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE));

    public final static JID JID_TO_ADD = new JID(
        ("bob_stf@" + SERVER + "/" + Saros.RESOURCE));

    public final static JID JID_TO_CHANGE = new JID((NEW_USER_NAME + "@"
        + SERVER + "/" + Saros.RESOURCE));

    public static String PASSWORD = "dddfffggg";
    public static String NO_MATCHED_REPEAT_PASSWORD = "dd";

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

    /* File */
    public static final String FILE1 = "MyFile.xml";
    public static final String FILE2 = "MyFile2.xml";
    public static final String FILE3 = "file.txt";
    public static final String[] path = { PROJECT1, FILE3 };

    /* Package name */
    public static final String PKG1 = "my.pkg";
    public static final String PKG2 = "my.pkg2";
    public static final String PKG3 = "my.pkg3";
    public static final String SRC = "src";

    /* class name */
    public static final String CLS1 = "MyClass";
    public static final String CLS2 = "MyClass2";
    public static final String CLS3 = "MyClass3";

    /* content path */
    public static final String CP1 = "test/STF/" + CLS1 + SUFIX_JAVA;
    public static final String CP2 = "test/STF/" + CLS2 + SUFIX_JAVA;
    public static final String CP3 = "test/STF/" + CLS3 + SUFIX_JAVA;
    public static final String CP1_CHANGE = "test/STF/" + CLS1 + "Change"
        + SUFIX_JAVA;
    public static final String CP2_CHANGE = "test/STF/" + CLS2 + "Change"
        + SUFIX_JAVA;

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

    /**********************************************
     * 
     * test conditions
     * 
     **********************************************/
    /**
     * bring workbench to a original state before beginning your tests
     * <ul>
     * <li>activate saros-instance workbench</li>
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
            tester.workbench.activateWorkbench();
            // tester.sarosBuddiesV.disconnectGUI();
            tester.workbench.setUpWorkbench();
            tester.view.closeViewByTitle("Welcome");
            tester.windowM.openPerspective();
            tester.workbench.closeUnnecessaryViews();
        }
    }

    /**
     * bring Saros to a original state before beginning your tests
     * <ul>
     * <li>make automaticReminder disable</li>
     * <li>open sarosViews</li>
     * <li>connect</li>
     * <li>check buddy lists, if all active testers are in contact</li>
     * </ul>
     * 
     * @throws RemoteException
     */
    public static void setUpSaros() throws RemoteException {
        for (Tester tester : activeTesters) {
            tester.sarosM.disableAutomaticReminderNoGUI();
            tester.workbench.openSarosViews();
            tester.sarosBuddiesV.connectNoGUI(tester.jid, tester.password);
        }
        // check buddy lists.
        for (Tester tester : activeTesters) {
            for (Tester otherTester : activeTesters) {
                if (tester != otherTester) {
                    tester.addBuddyGUIDone(otherTester);
                }
            }
        }
    }

    /**
     * A convenient function to quickly build a session with default value.
     * 
     * @param inviter
     * @param invitees
     * @throws RemoteException
     * @throws InterruptedException
     */
    public static void setUpSessionByDefault(Tester inviter, Tester... invitees)
        throws RemoteException, InterruptedException {
        inviter.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        inviter.buildSessionDoneConcurrently(VIEW_PACKAGE_EXPLORER, PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            invitees);
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
                tester.sarosBuddiesV.resetAllBuddyNameNoGUI();
                tester.sarosBuddiesV.disconnect();
                tester.workbench.deleteAllProjects();
            }
        }
        resetAllBots();
    }

    public static void resetWorkbenches() throws RemoteException {
        for (Tester tester : activeTesters) {
            tester.workbench.resetWorkbench();
        }
    }

    public static void resetAllBots() {
        alice = bob = carl = dave = edna = null;
        activeTesters.clear();
        assertTrue(activeTesters.isEmpty());
    }

    public static void resetDefaultAccount() throws RemoteException {
        for (Tester tester : activeTesters) {
            if (tester != null) {
                if (!tester.sarosM.isAccountExist(tester.jid))
                    tester.sarosM.addAccount(tester.jid, tester.password);
                if (!tester.sarosM.isAccountActive(tester.jid))
                    tester.sarosM.activateAccount(tester.jid);
                tester.sarosM.deleteAllNoActiveAccounts();

            }
        }
    }

    public static void reBuildSession(Tester host, Tester... invitees)
        throws RemoteException {
        if (!host.sarosSessionV.isInSessionNoGUI()) {
            for (Tester tester : invitees) {
                host.buildSessionDoneSequentially(VIEW_PACKAGE_EXPLORER,
                    PROJECT1, TypeOfShareProject.SHARE_PROJECT,
                    TypeOfCreateProject.EXIST_PROJECT, tester);
            }
        }
    }

    public static void createProjectWithClassByActiveTesters()
        throws RemoteException {
        for (Tester tester : activeTesters) {
            tester.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        }
    }

    public static void createProjectWithFileBy(Tester... testers)
        throws RemoteException {
        for (Tester tester : testers) {
            tester.fileM.newProject(PROJECT1);
            tester.fileM.newFile(path);
            tester.editor.waitUntilEditorOpen(FILE3);
        }
    }

    public static void deleteAllProjectsByActiveTesters()
        throws RemoteException {
        for (Tester tester : activeTesters) {
            tester.workbench.deleteAllProjects();
        }
    }

    public static void resetWriteAccess(Tester host, Tester... invitees)
        throws RemoteException {
        for (Tester tester : invitees) {
            if (tester.sarosSessionV.isInSession()
                && tester.sarosSessionV.hasReadOnlyAccess()) {
                host.sarosSessionV.grantWriteAccess(tester.jid);
            }
        }

        if (host.sarosSessionV.isInSessionNoGUI()
            && !host.sarosSessionV.hasWriteAccessNoGUI()) {
            host.sarosSessionV.grantWriteAccess(host.jid);
        }
    }

    public static void resetFollowMode(Tester... activeTesters)
        throws RemoteException {
        for (Tester tester : activeTesters) {
            if (tester.sarosSessionV.isInSessionNoGUI()
                && tester.sarosSessionV.isFollowing()) {
                tester.sarosSessionV.stopFollowing();
            }
        }
    }

    public static void disConnectByActiveTesters() throws RemoteException {
        for (Tester tester : activeTesters) {
            if (tester != null) {
                tester.sarosBuddiesV.disconnect();
            }
        }
    }

    public static void deleteFolders(String... folders) throws RemoteException {
        for (Tester tester : activeTesters) {
            for (String folder : folders) {
                if (tester.fileM.existsFolder(PROJECT1, folder))
                    tester.editM.deleteFolderNoGUI(PROJECT1, folder);
            }
        }
    }

    @Before
    public void before() throws Exception {
        resetWorkbenches();
    }

    @After
    public void after() throws Exception {
        resetWorkbenches();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        resetSaros();
    }

    public String[] getClassNodes(String projectName, String pkg,
        String className) {
        String[] nodes = { projectName, SRC, pkg, className + ".java" };
        return nodes;
    }

    public String[] changeToRegex(String... texts) {
        String[] matchTexts = new String[texts.length];
        for (int i = 0; i < texts.length; i++) {
            matchTexts[i] = texts[i] + "( .*)?";
        }
        return matchTexts;
    }

}
