package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.MakeOperationConcurrently;
import de.fu_berlin.inf.dpp.stf.client.tester.AbstractTester;
import de.fu_berlin.inf.dpp.stf.client.tester.DummyTester;
import de.fu_berlin.inf.dpp.stf.client.tester.TesterConfiguration;
import de.fu_berlin.inf.dpp.stf.stfMessages.STFMessages;

public class STFTest extends STFMessages {

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
    protected static AbstractTester alice = new DummyTester("alice");
    protected static AbstractTester bob = new DummyTester("bob");
    protected static AbstractTester carl = new DummyTester("carl");
    protected static AbstractTester dave = new DummyTester("dave");
    protected static AbstractTester edna = new DummyTester("edna");

    public static enum TypeOfTester {
        ALICE, BOB, CARL, DAVE, EDNA
    }

    public static List<AbstractTester> activeTesters = new ArrayList<AbstractTester>();

    public static List<AbstractTester> initTesters(TypeOfTester... testers)
        throws RemoteException {
        List<AbstractTester> result = new ArrayList<AbstractTester>();
        for (TypeOfTester t : testers) {
            switch (t) {
            case ALICE:
                alice = TesterConfiguration.newAlice();
                result.add(alice);
                break;
            case BOB:
                bob = TesterConfiguration.newBob();
                result.add(bob);
                break;
            case CARL:
                carl = TesterConfiguration.newCarl();
                result.add(carl);
                break;
            case DAVE:
                dave = TesterConfiguration.newDave();
                result.add(dave);
                break;
            case EDNA:
                edna = TesterConfiguration.newEdna();
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
    public final static JID TEST_JID = new JID(
        "edna_stf@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE);
    public final static JID TEST_JID2 = new JID(
        "dave_stf@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE);

    /* test data for modifying account */
    public final static String SERVER = "saros-con.imp.fu-berlin.de";
    public final static String NEW_XMPP_JABBER_ID = "new_alice_stf@" + SERVER;

    public final static String REGISTERED_USER_NAME = "bob_stf";

    public static final String INVALID_SERVER_NAME = "saros-con";

    // need to change, if you want to test creatAccount
    public static JID JID_TO_CREATE = new JID(
        ("test3@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE));

    public final static JID JID_TO_ADD = new JID(
        ("bob_stf@" + SERVER + "/" + Saros.RESOURCE));

    public final static JID JID_TO_CHANGE = new JID(
        (NEW_XMPP_JABBER_ID + "/" + Saros.RESOURCE));

    public static String PASSWORD = "dddfffggg";
    public static String NO_MATCHED_REPEAT_PASSWORD = "dddfffggggg";

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
    protected static final String SVN_SUFFIX = "[stf_test/stf_test_project]";
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
        closeAllShells();
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
        for (AbstractTester tester : activeTesters) {
            tester.remoteBot().activateWorkbench();
            if (tester.remoteBot().isViewOpen("Welcome"))
                tester.remoteBot().view("Welcome").close();
            tester.superBot().menuBar().window().openPerspective();
            closeUnnecessaryViews(tester);
            tester.remoteBot().resetWorkbench();
            deleteAllProjects(tester);

        }
    }

    public static void deleteAllProjects(AbstractTester tester)
        throws RemoteException {
        List<String> treeItems = tester.superBot().views()
            .packageExplorerView().tree().getTextOfTreeItems();
        for (String treeItem : treeItems) {
            tester.superBot().views().packageExplorerView()
                .selectProject(treeItem).delete();
        }

    }

    public static void closeUnnecessaryViews(AbstractTester tester)
        throws RemoteException {
        if (tester.remoteBot().isViewOpen("Problems"))
            tester.remoteBot().view("Problems").close();

        if (tester.remoteBot().isViewOpen("Javadoc"))
            tester.remoteBot().view("Javadoc").close();

        if (tester.remoteBot().isViewOpen("Declaration"))
            tester.remoteBot().view("Declaration").close();

        if (tester.remoteBot().isViewOpen("Task List"))
            tester.remoteBot().view("Task List").close();

        if (tester.remoteBot().isViewOpen("Outline"))
            tester.remoteBot().view("Outline").close();
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
        for (AbstractTester tester : activeTesters) {
            tester.superBot().menuBar().saros().preferences()
                .disableAutomaticReminder();
            openSarosViews(tester);
            tester.superBot().views().sarosView()
                .connectWith(tester.getJID(), tester.getPassword());
        }
        resetBuddies();
    }

    public static void openSarosViews(AbstractTester tester)
        throws RemoteException {
        if (!tester.remoteBot().isViewOpen(VIEW_SAROS)) {
            tester.superBot().menuBar().window()
                .showViewWithName(NODE_SAROS, VIEW_SAROS);
        }
        // if (!tester.bot().isViewOpen(VIEW_SAROS_SESSION))
        // tester.superBot().menuBar().window()
        // .showViewWithName(NODE_SAROS, VIEW_SAROS_SESSION);
        //
        // if (!tester.bot().isViewOpen(VIEW_REMOTE_SCREEN))
        // tester.superBot().menuBar().window()
        // .showViewWithName(NODE_SAROS, VIEW_REMOTE_SCREEN);
        //
        // if (!tester.bot().isViewOpen(VIEW_SAROS_CHAT))
        // tester.superBot().menuBar().window()
        // .showViewWithName(NODE_SAROS, VIEW_SAROS_CHAT);
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
    public static void setUpSessionWithAJavaProjectAndAClass(
        AbstractTester inviter, AbstractTester... invitees)
        throws RemoteException, InterruptedException {
        inviter.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS1);
        buildSessionConcurrently(PROJECT1, TypeOfCreateProject.NEW_PROJECT,
            inviter, invitees);
    }

    public static void setUpSessionWithJavaProjects(
        Map<String, List<String>> projectsPkgsClasses, AbstractTester inviter,
        AbstractTester... invitees) throws RemoteException {
        List<String> createdProjects = new ArrayList<String>();
        for (Iterator<String> i = projectsPkgsClasses.keySet().iterator(); i
            .hasNext();) {
            String key = i.next();
            if (!createdProjects.contains(key)) {
                createdProjects.add(key);
                inviter.superBot().views().packageExplorerView().tree().newC()
                    .javaProject(key);
                List<String> pkgAndclass = projectsPkgsClasses.get(key);
                inviter.superBot().views().packageExplorerView()
                    .selectPkg(key, pkgAndclass.get(0)).newC()
                    .cls(key, pkgAndclass.get(0), pkgAndclass.get(1));
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
        for (AbstractTester tester : activeTesters) {
            if (tester != null) {
                // tester.sarosBot().views().buddiesView().resetAllBuddyNameNoGUI();
                tester.superBot().views().sarosView().disconnect();
                deleteAllProjects(tester);
            }
        }
        resetAllBots();
    }

    public static void resetSaros(AbstractTester... testers)
        throws RemoteException {
        resetBuddiesName();
        for (AbstractTester tester : testers) {
            if (tester != null) {
                tester.superBot().views().sarosView().disconnect();
                deleteAllProjects(tester);
            }
        }
        resetAllBots();
    }

    public static void resetWorkbenches() throws RemoteException {
        for (AbstractTester tester : activeTesters) {
            tester.remoteBot().resetWorkbench();
        }
    }

    public static void closeAllShells() throws RemoteException {
        for (AbstractTester tester : activeTesters) {
            tester.remoteBot().closeAllShells();
        }
    }

    public static void resetDefaultAccount() throws RemoteException {
        for (AbstractTester tester : activeTesters) {
            if (tester != null) {
                if (!tester.superBot().menuBar().saros().preferences()
                    .existsAccount(tester.getJID()))
                    tester.superBot().menuBar().saros().preferences()
                        .addAccount(tester.getJID(), tester.getPassword());
                if (!tester.superBot().menuBar().saros().preferences()
                    .isAccountActive(tester.getJID()))
                    tester.superBot().menuBar().saros().preferences()
                        .activateAccount(tester.getJID());
                tester.superBot().menuBar().saros().preferences()
                    .deleteAllNoActiveAccounts();

            }
        }
    }

    public static void resetBuddiesName() throws RemoteException {
        // for (int i = 0; i < activeTesters.size(); i++) {
        // List<Tester> testers = activeTesters;
        // testers.remove(i);
        // for (Tester tester : testers) {
        // activeTesters.get(i).sarosBot().views().buddiesView()
        // .selectBuddy(tester.jid).rename(tester.jid.getBase());
        // }
        // // activeTesters.get(i).resetBuddiesName((Tester[])
        // // testers.toArray());
        // }
        for (int i = 0; i < activeTesters.size(); i++) {
            for (int j = i + 1; j < activeTesters.size(); j++) {
                activeTesters.get(i).superBot().views().sarosView()
                    .selectBuddy(activeTesters.get(j).getJID())
                    .rename(activeTesters.get(j).getBaseJid());
            }
        }
    }

    public static void resetBuddies() throws RemoteException {
        // check buddy lists.
        for (int i = 0; i < activeTesters.size(); i++) {
            for (int j = i + 1; j < activeTesters.size(); j++) {
                addBuddies(activeTesters.get(i), activeTesters.get(j));
            }
        }
    }

    public static void resetWriteAccess(AbstractTester host,
        AbstractTester... invitees) throws RemoteException {
        for (AbstractTester tester : invitees) {
            if (tester.superBot().views().sarosView().isInSession()
                && host.superBot().views().sarosView()
                    .selectParticipant(tester.getJID()).hasReadOnlyAccess()) {
                host.superBot().views().sarosView()
                    .selectParticipant(tester.getJID()).grantWriteAccess();
            }
        }

        // if (host.superBot().views().sarosView().isInSession()
        // && !host.superBot().views().sarosView()
        // .selectParticipant(host.getJID()).hasWriteAccess()) {
        // host.superBot().views().sarosView()
        // .selectParticipant(host.getJID()).grantWriteAccess();
        // }
    }

    public static void resetFollowModeSequentially(
        AbstractTester... buddiesFollowing) throws RemoteException {
        for (AbstractTester tester : buddiesFollowing) {
            if (tester.superBot().views().sarosView().isInSession()
                && tester.superBot().views().sarosView().isFollowing()) {
                JID followedBuddyJID = tester.superBot().views().sarosView()
                    .getFollowedBuddy();
                tester.superBot().views().sarosView()
                    .selectParticipant(followedBuddyJID).stopFollowing();
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
    public static void resetFollowModeConcurrently(
        AbstractTester... buddiesFollowing) throws RemoteException,
        InterruptedException {
        List<Callable<Void>> stopFollowTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < buddiesFollowing.length; i++) {
            final AbstractTester tester = buddiesFollowing[i];
            stopFollowTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    JID followedBuddyJID = tester.superBot().views()
                        .sarosView().getFollowedBuddy();
                    tester.superBot().views().sarosView()
                        .selectParticipant(followedBuddyJID).stopFollowing();
                    return null;
                }
            });
        }
        MakeOperationConcurrently.workAll(stopFollowTasks);
    }

    // public static void resetSharedProject(Tester host) throws RemoteException
    // {
    // host.sarosBot().views().packageExplorerView().selectSrc(PROJECT1)
    // .deleteAllItems();
    // host.sarosBot().views().packageExplorerView().tree().newC()
    // .cls(PROJECT1, PKG1, CLS1);
    // // host.bot().sleep(1000);
    // }

    /**********************************************
     * 
     * often used convenient functions
     * 
     **********************************************/

    public static void reBuildSession(AbstractTester host,
        AbstractTester... invitees) throws RemoteException {
        if (!host.superBot().views().sarosView().isInSession()) {
            for (AbstractTester tester : invitees) {
                buildSessionSequentially(PROJECT1,
                    TypeOfCreateProject.EXIST_PROJECT, host, tester);
            }
        }
    }

    public static void createSameJavaProjectByActiveTesters()
        throws RemoteException {
        for (AbstractTester tester : activeTesters) {
            tester.superBot().views().packageExplorerView().tree().newC()
                .javaProjectWithClasses(PROJECT1, PKG1, CLS1);
        }
    }

    public static void createProjectWithFileBy(AbstractTester... testers)
        throws RemoteException {
        for (AbstractTester tester : testers) {
            tester.superBot().views().packageExplorerView().tree().newC()
                .project(PROJECT1);
            tester.superBot().views().packageExplorerView()
                .selectFolder(PROJECT1).newC().file(FILE3);
            tester.remoteBot().waitUntilEditorOpen(FILE3);
        }
    }

    public static void deleteAllProjectsByActiveTesters()
        throws RemoteException {
        for (AbstractTester tester : activeTesters) {
            deleteAllProjects(tester);
        }
    }

    public static void disConnectByActiveTesters() throws RemoteException {
        for (AbstractTester tester : activeTesters) {
            if (tester != null) {
                tester.superBot().views().sarosView().disconnect();
            }
        }
    }

    public static void deleteFoldersByActiveTesters(String... folders)
        throws RemoteException {
        for (AbstractTester tester : activeTesters) {
            for (String folder : folders) {
                if (tester.superBot().views().packageExplorerView()
                    .selectProject(PROJECT1).existsWithRegex(folder))
                    tester.superBot().views().packageExplorerView()
                        .selectFolder(PROJECT1, folder).delete();
            }
        }
    }

    public static void buildSessionSequentially(String projectName,
        TypeOfCreateProject usingWhichProject, AbstractTester inviter,
        AbstractTester... invitees) throws RemoteException {
        JID[] inviteesJID = getPeerJID(invitees);
        inviter.superBot().menuBar().saros()
            .shareProjects(projectName, inviteesJID);
        for (AbstractTester invitee : invitees) {
            invitee.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(FINISH);
            invitee.superBot().confirmShellAddProjectUsingWhichProject(
                projectName, usingWhichProject);
        }
    }

    // ********** Component, which consist of other simple functions ***********

    public static void buildSessionConcurrently(final String projectName,
        final TypeOfCreateProject usingWhichProject, AbstractTester inviter,
        AbstractTester... invitees) throws RemoteException,
        InterruptedException {

        log.trace("alice.shareProjectParallel");
        inviter.superBot().menuBar().saros()
            .shareProjects(projectName, getPeerJID(invitees));

        List<Callable<Void>> joinSessionTasks = new ArrayList<Callable<Void>>();
        for (final AbstractTester invitee : invitees) {
            joinSessionTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    invitee.superBot()
                        .confirmShellSessionInvitationAndShellAddProject(
                            projectName, usingWhichProject);
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
    public static void leaveSessionHostFirst(AbstractTester host)
        throws RemoteException, InterruptedException {

        host.superBot().views().sarosView().leaveSession();
        for (final AbstractTester tester : activeTesters) {
            if (tester != host) {
                tester.superBot().views().sarosView().waitUntilIsNotInSession();

            }
        }

        // MakeOperationConcurrently.workAll(closeSessionTasks);
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
        AbstractTester host = null;
        List<JID> peerJIDs = new ArrayList<JID>();
        List<Callable<Void>> leaveTasks = new ArrayList<Callable<Void>>();
        for (final AbstractTester tester : activeTesters) {

            if (tester.superBot().views().sarosView().isHost()) {
                host = tester;
            } else {
                peerJIDs.add(tester.getJID());
                leaveTasks.add(new Callable<Void>() {
                    public Void call() throws Exception {
                        tester.superBot().views().sarosView().leaveSession();
                        return null;
                    }
                });
            }
        }

        MakeOperationConcurrently.workAll(leaveTasks);
        if (host != null) {
            host.superBot().views().sarosView()
                .waitUntilAllPeersLeaveSession(peerJIDs);
            host.remoteBot().view(VIEW_SAROS).toolbarButton(TB_STOP_SESSION).click();
            host.superBot().views().sarosView().waitUntilIsNotInSession();
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
    public static void setFollowMode(final AbstractTester followedBuddy,
        AbstractTester... buddiesTofollow) throws RemoteException,
        InterruptedException {
        List<Callable<Void>> followTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < buddiesTofollow.length; i++) {
            final AbstractTester tester = buddiesTofollow[i];
            followTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    tester.superBot().views().sarosView()
                        .selectParticipant(followedBuddy.getJID())
                        .followParticipant();
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
    public static void addBuddies(AbstractTester host, AbstractTester... peers)
        throws RemoteException {
        for (AbstractTester peer : peers) {
            if (!host.superBot().views().sarosView().hasBuddy(peer.getJID())) {
                host.superBot().views().sarosView().addANewBuddy(peer.getJID());
                peer.superBot().confirmShellRequestOfSubscriptionReceived();
            }
        }
    }

    // /**
    // * Remove given contact from Roster without GUI, if contact was added
    // before
    // *
    // * @throws XMPPException
    // */
    // public static void deleteBuddiesNoGUI(Tester buddy,
    // Tester... deletedBuddies) throws RemoteException, XMPPException {
    // for (Tester deletedBuddy : deletedBuddies) {
    // if (!buddy.sarosBot().views().buddiesView()
    // .hasBuddyNoGUI(deletedBuddy.jid))
    // return;
    // buddy.sarosBot().views().buddiesView()
    // .selectBuddy(deletedBuddy.jid).delete();
    // deletedBuddy.sarosBot().views().buddiesView()
    // .confirmShellRemovelOfSubscription();
    // }
    // }

    /**
     * Remove given contact from Roster with GUI, if contact was added before.
     */
    public static void deleteBuddies(AbstractTester buddy,
        AbstractTester... deletedBuddies) throws RemoteException {
        for (AbstractTester deletedBuddy : deletedBuddies) {
            if (!buddy.superBot().views().sarosView()
                .hasBuddy(deletedBuddy.getJID()))
                return;
            buddy.superBot().views().sarosView()
                .selectBuddy(deletedBuddy.getJID()).delete();
            deletedBuddy.superBot().confirmShellRemovelOfSubscription();
        }

    }

    public static void shareYourScreen(AbstractTester buddy,
        AbstractTester selectedBuddy) throws RemoteException {
        buddy.superBot().views().sarosView()
            .shareYourScreenWithSelectedBuddy(selectedBuddy.getJID());
        selectedBuddy.remoteBot().waitUntilShellIsOpen(
            SHELL_INCOMING_SCREENSHARING_SESSION);
        selectedBuddy.remoteBot().shell(SHELL_INCOMING_SCREENSHARING_SESSION).bot()
            .button(YES).click();
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
        final TypeOfCreateProject usingWhichProject, AbstractTester inviter,
        AbstractTester... invitees) throws RemoteException,
        InterruptedException {
        inviter.superBot().menuBar().saros()
            .addBuddies(getPeersBaseJID(invitees));
        List<Callable<Void>> joinSessionTasks = new ArrayList<Callable<Void>>();
        for (final AbstractTester tester : invitees) {
            joinSessionTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    tester.remoteBot().shell(SHELL_SESSION_INVITATION)
                        .confirm(FINISH);
                    tester.superBot().confirmShellAddProjectUsingWhichProject(
                        projectName, usingWhichProject);
                    return null;
                }
            });
        }
        log.trace("workAll(joinSessionTasks)");
        MakeOperationConcurrently.workAll(joinSessionTasks);
    }

    public void waitsUntilTransferedDataIsArrived(AbstractTester buddy)
        throws RemoteException {
        buddy.remoteBot().sleep(500);
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

    private static String[] getPeersBaseJID(AbstractTester... peers) {
        String[] peerBaseJIDs = new String[peers.length];
        for (int i = 0; i < peers.length; i++) {
            peerBaseJIDs[i] = peers[i].getBaseJid();
        }
        return peerBaseJIDs;
    }

    private static JID[] getPeerJID(AbstractTester... peers) {
        JID[] peerBaseJIDs = new JID[peers.length];
        for (int i = 0; i < peers.length; i++) {
            peerBaseJIDs[i] = peers[i].getJID();
        }
        return peerBaseJIDs;
    }

    public static AbstractTester getAlice() {
        return alice;
    }

    public static void setAlice(AbstractTester alice) {
        STFTest.alice = alice;
    }

    public static AbstractTester getBob() {
        return bob;
    }

    public static void setBob(AbstractTester bob) {
        STFTest.bob = bob;
    }

    public static AbstractTester getCarl() {
        return carl;
    }

    public static void setCarl(AbstractTester carl) {
        STFTest.carl = carl;
    }

    public static AbstractTester getDave() {
        return dave;
    }

    public static void setDave(AbstractTester dave) {
        STFTest.dave = dave;
    }

    public static AbstractTester getEdna() {
        return edna;
    }

    public static void setEdna(AbstractTester edna) {
        STFTest.edna = edna;
    }

}
