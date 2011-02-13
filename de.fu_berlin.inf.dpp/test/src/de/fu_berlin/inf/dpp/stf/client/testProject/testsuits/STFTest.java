package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.client.ConfigTester;
import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.MakeOperationConcurrently;

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

    public static enum TypeOfTester {
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

    /* class name */
    public static final String CLS1 = "MyClass";
    public static final String CLS2 = "MyClass2";
    public static final String CLS3 = "MyClass3";

    /* class name with suffix */
    public static final String CLS1_SUFFIX = "MyClass" + SUFFIX_JAVA;
    public static final String CLS2_SUFFIX = "MyClass2" + SUFFIX_JAVA;
    public static final String CLS3_SUFFIX = "MyClass3" + SUFFIX_JAVA;

    /* content path */
    public static final String CP1 = "test/STF/" + CLS1 + SUFFIX_JAVA;
    public static final String CP2 = "test/STF/" + CLS2 + SUFFIX_JAVA;
    public static final String CP3 = "test/STF/" + CLS3 + SUFFIX_JAVA;
    public static final String CP1_CHANGE = "test/STF/" + CLS1 + "Change"
        + SUFFIX_JAVA;
    public static final String CP2_CHANGE = "test/STF/" + CLS2 + "Change"
        + SUFFIX_JAVA;

    /* SVN infos */
    protected static final String SVN_REPOSITORY_URL = "http://saros-build.imp.fu-berlin.de/svn/saros";
    protected static final String SVN_PROJECT = "stf_test_project";
    protected static final String SVN_PROJECT_COPY = "copy_of_stf_test_project";
    protected static String SVN_PROJECT_PATH = getOS() == TypeOfOS.MAC ? "stf_tests/stf_test_project"
        : "/stf_tests/stf_test_project";

    protected static final String SVN_PROJECT_URL_SWITCHED = SVN_REPOSITORY_URL
        + "/stf_tests/stf_test_project_copy";
    protected static final String SVN_PKG = "pkg";
    protected static final String SVN_CLS1 = "Test";
    protected static final String SVN_CLS1_SUFFIX = SVN_CLS1 + SUFFIX_JAVA;
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
     * Before/After conditions
     * 
     **********************************************/

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

    /**********************************************
     * 
     * often used to define preconditions
     * 
     **********************************************/

    /**
     * bring workbench to a original state before beginning your tests
     * <ul>
     * <li>activate saros-instance workbench</li>
     * <li>close all opened popUp windows</li>
     * <li>close all opened editors</li>
     * <li>delete all existed projects</li>
     * <li>close welcome view, if it is open</li>
     * <li>open java perspective</li>
     * <li>close all unnecessary views</li>
     * </ul>
     * 
     * @throws RemoteException
     */
    public static void setUpWorkbench() throws RemoteException {
        for (Tester tester : activeTesters) {
            tester.workbench.activateWorkbench();
            tester.workbench.setUpWorkbench();
            if (tester.bot.isViewOpen("Welcome"))
                tester.bot().view("Welcome").close();
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
            openSarosViews(tester);
            tester.sarosBuddiesV.connectNoGUI(tester.jid, tester.password);
        }
        resetBuddies();
    }

    public static void openSarosViews(Tester tester) throws RemoteException {
        if (!tester.bot().isViewOpen(VIEW_SAROS_BUDDIES)) {
            tester.windowM.showViewWithName(NODE_SAROS, VIEW_SAROS_BUDDIES);
        }
        if (!tester.bot().isViewOpen(VIEW_SAROS_SESSION))
            tester.windowM.showViewWithName(NODE_SAROS, VIEW_SAROS_SESSION);

        if (!tester.bot().isViewOpen(VIEW_REMOTE_SCREEN))
            tester.windowM.showViewWithName(NODE_SAROS, VIEW_REMOTE_SCREEN);

        if (!tester.bot().isViewOpen(VIEW_SAROS_CHAT))
            tester.windowM.showViewWithName(NODE_SAROS, VIEW_SAROS_CHAT);
    }

    /**
     * A convenient function to quickly build a session which share a java
     * project with a class.
     * 
     * @param inviter
     * @param invitees
     * @throws RemoteException
     * @throws InterruptedException
     */
    public static void setUpSessionWithAJavaProjectAndAClass(Tester inviter,
        Tester... invitees) throws RemoteException, InterruptedException {
        inviter.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        buildSessionConcurrently(VIEW_PACKAGE_EXPLORER, PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            inviter, invitees);
    }

    public static void setUpSessionWithJavaProjects(
        Map<String, List<String>> projectsPkgsClasses, Tester inviter,
        Tester... invitees) throws RemoteException {
        List<String> createdProjects = new ArrayList<String>();
        for (Iterator<String> i = projectsPkgsClasses.keySet().iterator(); i
            .hasNext();) {
            String key = i.next();
            if (!createdProjects.contains(key)) {
                createdProjects.add(key);
                inviter.fileM.newJavaProject(key);
                List<String> pkgAndclass = projectsPkgsClasses.get(key);
                inviter.fileM.newClass(key, pkgAndclass.get(0),
                    pkgAndclass.get(1));
            }
        }
    }

    /**********************************************
     * 
     * often used to define afterConditions
     * 
     **********************************************/
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
                tester.editM.deleteAllProjectsNoGUI();
            }
        }
        resetAllBots();
    }

    public static void resetSaros(Tester... testers) throws RemoteException {
        for (Tester tester : testers) {
            if (tester != null) {
                tester.sarosBuddiesV.resetAllBuddyNameNoGUI();
                tester.sarosBuddiesV.disconnect();
                tester.editM.deleteAllProjectsNoGUI();
            }
        }
        resetAllBots();
    }

    public static void resetWorkbenches() throws RemoteException {
        for (Tester tester : activeTesters) {
            tester.workbench.resetWorkbench();
        }
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

    public static void resetBuddiesName() throws RemoteException {
        for (Tester tester : activeTesters) {
            tester.sarosBuddiesV.resetAllBuddyNameNoGUI();
        }
    }

    public static void resetBuddies() throws RemoteException {
        // check buddy lists.
        for (Tester tester : activeTesters) {
            for (Tester otherTester : activeTesters) {
                if (tester != otherTester) {
                    addBuddies(tester, otherTester);
                }
            }
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

    public static void resetFollowModeSequentially(Tester... buddiesFollowing)
        throws RemoteException {
        for (Tester tester : buddiesFollowing) {
            if (tester.sarosSessionV.isInSessionNoGUI()
                && tester.sarosSessionV.isFollowing()) {
                tester.sarosSessionV.stopFollowing();
            }
        }
    }

    /**
     * stop the follow-mode of the buddies who are following the local user.
     * 
     * @param buddiesFollowing
     *            the list of the buddies who are following the local user.
     * @throws RemoteException
     * @throws InterruptedException
     */
    public static void resetFollowModeConcurrently(Tester... buddiesFollowing)
        throws RemoteException, InterruptedException {
        List<Callable<Void>> stopFollowTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < buddiesFollowing.length; i++) {
            final Tester tester = buddiesFollowing[i];
            stopFollowTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    tester.sarosSessionV.stopFollowing();
                    return null;
                }
            });
        }
        MakeOperationConcurrently.workAll(stopFollowTasks);
    }

    public static void resetSharedProject(Tester host) throws RemoteException {
        host.editM.deleteAllItemsOfJavaProject(VIEW_PACKAGE_EXPLORER, PROJECT1);
        host.fileM.newClass(PROJECT1, PKG1, CLS1);
    }

    /**********************************************
     * 
     * often used convenient functions
     * 
     **********************************************/

    public static void reBuildSession(Tester host, Tester... invitees)
        throws RemoteException {
        if (!host.sarosSessionV.isInSessionNoGUI()) {
            for (Tester tester : invitees) {
                buildSessionSequentially(VIEW_PACKAGE_EXPLORER, PROJECT1,
                    TypeOfShareProject.SHARE_PROJECT,
                    TypeOfCreateProject.EXIST_PROJECT, host, tester);
            }
        }
    }

    public static void createSameJavaProjectByActiveTesters()
        throws RemoteException {
        for (Tester tester : activeTesters) {
            tester.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
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
            tester.editM.deleteAllProjectsNoGUI();
        }
    }

    public static void disConnectByActiveTesters() throws RemoteException {
        for (Tester tester : activeTesters) {
            if (tester != null) {
                tester.sarosBuddiesV.disconnect();
            }
        }
    }

    public static void deleteFoldersByActiveTesters(String... folders)
        throws RemoteException {
        for (Tester tester : activeTesters) {
            for (String folder : folders) {
                if (tester.fileM.existsFolderNoGUI(PROJECT1, folder))
                    tester.editM.deleteFolderNoGUI(PROJECT1, folder);
            }
        }
    }

    public static void buildSessionSequentially(String viewTitle,
        String projectName, TypeOfShareProject howToShareProject,
        TypeOfCreateProject usingWhichProject, Tester inviter,
        Tester... invitees) throws RemoteException {
        String[] baseJIDOfInvitees = getPeersBaseJID(invitees);

        inviter.sarosC.shareProjectWith(viewTitle, projectName,
            howToShareProject, baseJIDOfInvitees);
        for (Tester invitee : invitees) {
            invitee.sarosC.confirmShellSessionnInvitation();
            invitee.sarosC.confirmShellAddProjectUsingWhichProject(projectName,
                usingWhichProject);
        }
    }

    // ********** Component, which consist of other simple functions ***********

    public static void buildSessionConcurrently(String viewTitle,
        final String projectName, TypeOfShareProject howToShareProject,
        final TypeOfCreateProject usingWhichProject, Tester inviter,
        Tester... invitees) throws RemoteException, InterruptedException {

        log.trace("alice.shareProjectParallel");
        inviter.sarosC.shareProjectWith(viewTitle, projectName,
            howToShareProject, getPeersBaseJID(invitees));

        List<Callable<Void>> joinSessionTasks = new ArrayList<Callable<Void>>();
        for (final Tester invitee : invitees) {
            joinSessionTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    invitee.sarosC.confirmShellSessionnInvitation();
                    invitee.sarosC.confirmShellAddProjectUsingWhichProject(
                        projectName, usingWhichProject);
                    invitee.sarosSessionV.waitUntilIsInSession();
                    return null;
                }
            });
        }
        log.trace("workAll(joinSessionTasks)");
        MakeOperationConcurrently.workAll(joinSessionTasks);
    }

    /**
     * Define the leave session with the following steps.
     * <ol>
     * <li>The host(alice) leave session first.</li>
     * <li>Then other invitees confirm the windonws "Closing the Session"
     * concurrently</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    public static void leaveSessionHostFirst() throws RemoteException,
        InterruptedException {
        Tester host = null;
        List<Callable<Void>> closeSessionTasks = new ArrayList<Callable<Void>>();
        for (final Tester tester : activeTesters) {
            if (tester.sarosSessionV.isHostNoGUI()) {
                host = tester;
            } else {
                if (tester.sarosSessionV.isInSessionNoGUI()) {
                    closeSessionTasks.add(new Callable<Void>() {
                        public Void call() throws Exception {
                            // Need to check for isDriver before leaving.
                            tester.sarosSessionV
                                .confirmShellClosingTheSession();
                            return null;
                        }
                    });
                }
            }
        }
        if (host != null)
            host.sarosSessionV.leaveTheSessionByHost();
        MakeOperationConcurrently.workAll(closeSessionTasks);
    }

    /**
     * Define the leave session with the following steps.
     * <ol>
     * <li>The musicians bob and carl leave the session first.(concurrently)</li>
     * <li>wait until bob and carl are really not in the session using
     * "waitUntilAllPeersLeaveSession", then leave the host alice.</li>
     * </ol>
     * make sure,
     * 
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    public static void leaveSessionPeersFirst() throws RemoteException,
        InterruptedException {
        Tester host = null;
        List<JID> peerJIDs = new ArrayList<JID>();
        List<Callable<Void>> leaveTasks = new ArrayList<Callable<Void>>();
        for (final Tester tester : activeTesters) {

            if (tester.sarosSessionV.isHostNoGUI()) {
                host = tester;
            } else {
                peerJIDs.add(tester.jid);
                leaveTasks.add(new Callable<Void>() {
                    public Void call() throws Exception {
                        tester.sarosSessionV.leaveTheSessionByPeer();
                        return null;
                    }
                });
            }
        }

        MakeOperationConcurrently.workAll(leaveTasks);
        if (host != null) {
            host.sarosSessionV.waitUntilAllPeersLeaveSession(peerJIDs);
            host.toolbarButton.clickToolbarButtonWithRegexTooltipOnView(
                VIEW_SAROS_SESSION, TB_LEAVE_THE_SESSION);
            host.sarosSessionV.waitUntilIsNotInSession();
        }

    }

    /**
     * the local user can be concurrently followed by many other users.
     * 
     * @param buddiesTofollow
     *            the list of the buddies who want to follow the local user.
     * @throws RemoteException
     * @throws InterruptedException
     */
    public static void setFollowMode(final Tester followedBuddy,
        Tester... buddiesTofollow) throws RemoteException, InterruptedException {
        List<Callable<Void>> followTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < buddiesTofollow.length; i++) {
            final Tester tester = buddiesTofollow[i];
            followTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    tester.sarosSessionV.followThisBuddy(followedBuddy.jid);
                    return null;
                }
            });
        }
        MakeOperationConcurrently.workAll(followTasks);
    }

    /**
     * add buddy with GUI, which should be performed by both the users.
     * 
     * @param peers
     *            the user, which should be added in your contact list
     * @throws RemoteException
     * 
     * 
     */
    public static void addBuddies(Tester host, Tester... peers)
        throws RemoteException {
        for (Tester peer : peers) {
            if (!host.sarosBuddiesV.hasBuddyNoGUI(peer.jid)) {
                host.sarosBuddiesV.addANewBuddy(peer.jid);
                peer.bot().shell(SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED)
                    .confirmShellAndWait(OK);
                host.bot().shell(SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED)
                    .confirmShellAndWait(OK);
            }
        }
    }

    /**
     * Remove given contact from Roster without GUI, if contact was added before
     * 
     * @throws XMPPException
     */
    public static void deleteBuddiesNoGUI(Tester buddy,
        Tester... deletedBuddies) throws RemoteException, XMPPException {
        for (Tester deletedBuddy : deletedBuddies) {
            if (!buddy.sarosBuddiesV.hasBuddyNoGUI(deletedBuddy.jid))
                return;
            buddy.sarosBuddiesV.deleteBuddyNoGUI(deletedBuddy.jid);
            deletedBuddy.sarosBuddiesV.confirmShellRemovelOfSubscription();
        }
    }

    /**
     * Remove given contact from Roster with GUI, if contact was added before.
     */
    public static void deleteBuddies(Tester buddy, Tester... deletedBuddies)
        throws RemoteException {
        for (Tester deletedBuddy : deletedBuddies) {
            if (!buddy.sarosBuddiesV.hasBuddyNoGUI(deletedBuddy.jid))
                return;
            buddy.sarosBuddiesV.deleteBuddy(deletedBuddy.jid);
            deletedBuddy.sarosBuddiesV.confirmShellRemovelOfSubscription();
        }

    }

    public static void shareYourScreen(Tester buddy, Tester selectedBuddy)
        throws RemoteException {
        buddy.sarosSessionV.shareYourScreenWithSelectedBuddy(selectedBuddy.jid);
        selectedBuddy.bot().shell(SHELL_INCOMING_SCREENSHARING_SESSION)
            .confirmShellAndWait(YES);
    }

    /**
     * This method is same as
     * 
     * . The difference to buildSessionConcurrently is that the invitation
     * process is activated by clicking the toolbarbutton
     * "open invitation interface" in the roster view.
     * 
     * @param projectName
     *            the name of the project which is in a session now.
     * @param invitees
     *            the user whom you want to invite to your session.
     * @throws RemoteException
     * @throws InterruptedException
     */
    public static void inviteBuddies(final String projectName,
        final TypeOfCreateProject usingWhichProject, Tester inviter,
        Tester... invitees) throws RemoteException, InterruptedException {
        inviter.sarosSessionV
            .openInvitationInterface(getPeersBaseJID(invitees));
        List<Callable<Void>> joinSessionTasks = new ArrayList<Callable<Void>>();
        for (final Tester tester : invitees) {
            joinSessionTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    tester.sarosC.confirmShellSessionnInvitation();
                    tester.sarosC.confirmShellAddProjectUsingWhichProject(
                        projectName, usingWhichProject);
                    return null;
                }
            });
        }
        log.trace("workAll(joinSessionTasks)");
        MakeOperationConcurrently.workAll(joinSessionTasks);
    }

    public void waitsUntilTransferedDataIsArrived(Tester buddy)
        throws RemoteException {
        buddy.workbench.sleep(500);
    }

    /**********************************************
     * 
     * inner functions
     * 
     **********************************************/
    private static void resetAllBots() {
        alice = bob = carl = dave = edna = null;
        activeTesters.clear();
        assertTrue(activeTesters.isEmpty());
    }

    private static String[] getPeersBaseJID(Tester... peers) {
        String[] peerBaseJIDs = new String[peers.length];
        for (int i = 0; i < peers.length; i++) {
            peerBaseJIDs[i] = peers[i].getBaseJid();
        }
        return peerBaseJIDs;
    }
}
