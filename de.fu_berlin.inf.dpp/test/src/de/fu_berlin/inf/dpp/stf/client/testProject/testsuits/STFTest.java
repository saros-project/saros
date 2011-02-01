package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import de.fu_berlin.inf.dpp.stf.SarosLabels;
import de.fu_berlin.inf.dpp.stf.client.Tester;

public class STFTest implements SarosLabels {

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
            tester.sarosM.disableAutomaticReminder();
            tester.workbench.openSarosViews();
            tester.sarosBuddiesV.connect(tester.jid, tester.password);
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
     * For all active testers, reset buddy names, disconnect, delete all
     * projects.
     * 
     * @throws RemoteException
     */
    public static void resetSaros() throws RemoteException {
        for (Tester tester : activeTesters) {
            if (tester != null) {
                tester.sarosBuddiesV.resetAllBuddyName();
                tester.sarosBuddiesV.disconnectGUI();
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
        inviter.buildSessionDoneConcurrently(PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            invitees);
    }

    public static void reBuildSession(Tester host, Tester... invitees)
        throws RemoteException {
        if (!host.sarosSessionV.isInSession()) {
            for (Tester tester : invitees) {
                host.buildSessionDoneSequentially(PROJECT1,
                    TypeOfShareProject.SHARE_PROJECT,
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
                host.sarosSessionV.grantWriteAccessGUI(tester.sarosSessionV);
            }
        }

        if (host.sarosSessionV.isInSession()
            && !host.sarosSessionV.hasWriteAccess()) {
            host.sarosSessionV.grantWriteAccessGUI(host.sarosSessionV);
        }
    }

    public static void resetFollowMode(Tester... activeTesters)
        throws RemoteException {
        for (Tester tester : activeTesters) {
            if (tester.sarosSessionV.isInSession()
                && tester.sarosSessionV.isInFollowMode()) {
                tester.sarosSessionV.stopFollowingGUI();
            }
        }
    }

    public static void disConnectByActiveTesters() throws RemoteException {
        for (Tester tester : activeTesters) {
            if (tester != null) {
                tester.sarosBuddiesV.disconnectGUI();
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
