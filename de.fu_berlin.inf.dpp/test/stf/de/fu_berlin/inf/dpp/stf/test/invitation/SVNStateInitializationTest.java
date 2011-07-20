package de.fu_berlin.inf.dpp.stf.test.invitation;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;

public class SVNStateInitializationTest extends StfTestCase {

    // @BeforeClass
    // public static void initMusicians() throws Exception {
    // initTesters(ALICE, BOB);
    // setUpWorkbench();
    // setUpSaros();
    // if (!ALICE.superBot().views().packageExplorerView().tree()
    // .existsWithRegex(Constants.SVN_PROJECT_COPY)) {
    // ALICE.superBot().views().packageExplorerView().tree().newC()
    // .javaProject(Constants.SVN_PROJECT_COPY);
    // ALICE
    // .superBot()
    // .views()
    // .packageExplorerView()
    // .selectProject(Constants.SVN_PROJECT_COPY)
    // .team()
    // .shareProjectUsingSpecifiedFolderName(
    // Constants.SVN_REPOSITORY_URL, Constants.SVN_PROJECT_PATH);
    // }
    // }
    //
    // @Before
    // public void beforeEveryTest() throws RemoteException {
    // ALICE
    // .superBot()
    // .views()
    // .packageExplorerView()
    // .selectProject(
    // Constants.SVN_PROJECT_COPY + " " + Constants.SVN_SUFFIX).copy();
    // ALICE.superBot().views().packageExplorerView().tree()
    // .paste(Constants.SVN_PROJECT);
    //
    // ALICE.remoteBot().sleep(2000);
    //
    // assertTrue(ALICE
    // .superBot()
    // .views()
    // .packageExplorerView()
    // .tree()
    // .existsWithRegex(".*" + Pattern.quote(Constants.SVN_PROJECT) + ".*"));
    // assertTrue(ALICE.superBot().views().packageExplorerView()
    // .isProjectManagedBySVN(Constants.SVN_PROJECT));
    // assertTrue(ALICE.superBot().views().packageExplorerView()
    // .selectPkg("stf_test_project", "pkg")
    // .existsWithRegex(Pattern.quote("Test.java") + ".*"));
    // }
    //
    // @After
    // public void afterEveryTest() throws RemoteException {
    // leaveSessionHostFirst(ALICE);
    //
    // if (ALICE.superBot().views().packageExplorerView().tree()
    // .existsWithRegex(Constants.SVN_PROJECT))
    // ALICE.superBot().views().packageExplorerView()
    // .selectJavaProject(Constants.SVN_PROJECT).delete();
    // BOB.superBot().internal().clearWorkspace();
    // }
    //
    // @Test
    // public void testSimpleCheckout() throws RemoteException {
    //
    // /**
    // * FIXME: by shareProjects Wizard there are tableItem need to be
    // * selected, which can not be found with regexText by SWTBot
    // */
    // // buildSessionSequentially(SVN_PROJECT,
    // // TypeOfCreateProject.NEW_PROJECT,
    // // ALICE, BOB);
    //
    // ALICE.superBot().views().packageExplorerView()
    // .selectJavaProject(Constants.SVN_PROJECT).shareWith()
    // .buddy(BOB.getJID());
    // BOB.superBot().confirmShellSessionInvitationAndShellAddProject(
    // Constants.SVN_PROJECT, TypeOfCreateProject.NEW_PROJECT);
    //
    // ALICE.superBot().views().sarosView()
    // .waitUntilIsInviteeInSession(BOB.superBot());
    // BOB.superBot().views().packageExplorerView()
    // .waitUntilResourceIsShared(Constants.SVN_PROJECT);
    // assertTrue(BOB.superBot().views().packageExplorerView()
    // .isProjectManagedBySVN(Constants.SVN_PROJECT));
    //
    // // assertTrue(ALICE.superBot().views().sarosView()
    // // .existsParticipant(BOB.getJID()));
    // assertTrue(ALICE.superBot().views().sarosView()
    // .selectParticipant(BOB.getJID()).hasWriteAccess());
    // }
    //
    // @Test
    // public void testCheckoutWithUpdate() throws RemoteException {
    // ALICE
    // .superBot()
    // .views()
    // .packageExplorerView()
    // .selectClass(Constants.SVN_PROJECT, Constants.SVN_PKG,
    // Constants.SVN_CLS1).team().update(Constants.SVN_CLS1_REV1);
    // assertEquals(Constants.SVN_CLS1_REV1, ALICE.superBot().views()
    // .packageExplorerView().getRevision(Constants.SVN_CLS1_FULL_PATH));
    // // buildSessionSequentially(SVN_PROJECT,
    // // TypeOfCreateProject.NEW_PROJECT,
    // // ALICE, BOB);
    // ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
    // .workTogetherOn().project(Constants.SVN_PROJECT);
    //
    // BOB.superBot().confirmShellSessionInvitationAndShellAddProject(
    // Constants.SVN_PROJECT, TypeOfCreateProject.NEW_PROJECT);
    //
    // ALICE.superBot().views().sarosView()
    // .waitUntilIsInviteeInSession(BOB.superBot());
    //
    // assertTrue(BOB.superBot().views().packageExplorerView()
    // .isProjectManagedBySVN(Constants.SVN_PROJECT));
    // assertEquals(Constants.SVN_CLS1_REV1, BOB.superBot().views()
    // .packageExplorerView().getRevision(Constants.SVN_CLS1_FULL_PATH));
    // }
    //
    // @Test
    // public void testCheckoutWithSwitch() throws RemoteException {
    // ALICE
    // .superBot()
    // .views()
    // .packageExplorerView()
    // .tree()
    // .team()
    // .switchResource(Constants.SVN_CLS1_FULL_PATH,
    // Constants.SVN_CLS1_SWITCHED_URL);
    // assertEquals(Constants.SVN_CLS1_SWITCHED_URL,
    // ALICE.superBot().views().packageExplorerView()
    // .getURLOfRemoteResource(Constants.SVN_CLS1_FULL_PATH));
    // // buildSessionSequentially(SVN_PROJECT,
    // // TypeOfCreateProject.NEW_PROJECT,
    // // ALICE, BOB);
    // ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
    // .workTogetherOn().project(Constants.SVN_PROJECT);
    // BOB.superBot().confirmShellSessionInvitationAndShellAddProject(
    // Constants.SVN_PROJECT, TypeOfCreateProject.NEW_PROJECT);
    //
    // ALICE.superBot().views().sarosView()
    // .waitUntilIsInviteeInSession(BOB.superBot());
    // BOB.superBot().views().sarosView().waitUntilIsInSession();
    //
    // assertTrue(BOB.superBot().views().packageExplorerView()
    // .isProjectManagedBySVN(Constants.SVN_PROJECT));
    // assertEquals(Constants.SVN_CLS1_SWITCHED_URL,
    // BOB.superBot().views().packageExplorerView()
    // .getURLOfRemoteResource(Constants.SVN_CLS1_FULL_PATH));
    // }
    //
    // @Test
    // public void testCheckoutWithSwitch2() throws RemoteException {
    // ALICE
    // .superBot()
    // .views()
    // .packageExplorerView()
    // .tree()
    // .team()
    // .switchResource(Constants.SVN_CLS1_FULL_PATH,
    // Constants.SVN_CLS1_SWITCHED_URL, Constants.SVN_CLS1_REV3);
    // assertEquals(Constants.SVN_CLS1_SWITCHED_URL,
    // ALICE.superBot().views().packageExplorerView()
    // .getURLOfRemoteResource(Constants.SVN_CLS1_FULL_PATH));
    // assertEquals(Constants.SVN_CLS1_REV3, ALICE.superBot().views()
    // .packageExplorerView().getRevision(Constants.SVN_CLS1_FULL_PATH));
    // // buildSessionSequentially(SVN_PROJECT,
    // // TypeOfCreateProject.NEW_PROJECT,
    // // ALICE, BOB);
    // ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
    // .workTogetherOn().project(Constants.SVN_PROJECT);
    // BOB.superBot().confirmShellSessionInvitationAndShellAddProject(
    // Constants.SVN_PROJECT, TypeOfCreateProject.NEW_PROJECT);
    //
    // ALICE.superBot().views().sarosView()
    // .waitUntilIsInviteeInSession(BOB.superBot());
    // BOB.superBot().views().sarosView().waitUntilIsInSession();
    //
    // assertTrue(BOB.superBot().views().packageExplorerView()
    // .isProjectManagedBySVN(Constants.SVN_PROJECT));
    // assertEquals(Constants.SVN_CLS1_SWITCHED_URL,
    // BOB.superBot().views().packageExplorerView()
    // .getURLOfRemoteResource(Constants.SVN_CLS1_FULL_PATH));
    // assertEquals(Constants.SVN_CLS1_REV3, BOB.superBot().views()
    // .packageExplorerView().getRevision(Constants.SVN_CLS1_FULL_PATH));
    // }
    //
    // @Test
    // public void testCheckoutWithModification() throws RemoteException {
    // assertTrue(ALICE.superBot().views().packageExplorerView()
    // .selectPkg(Constants.SVN_PROJECT, Constants.SVN_PKG)
    // .existsWithRegex(Constants.SVN_CLS1 + SUFFIX_JAVA));
    // ALICE
    // .superBot()
    // .views()
    // .packageExplorerView()
    // .selectClass(Constants.SVN_PROJECT, Constants.SVN_PKG,
    // Constants.SVN_CLS1).open();
    // String cls1_content_before = ALICE.remoteBot()
    // .editor(Constants.SVN_CLS1_SUFFIX).getText();
    // ALICE.remoteBot().editor(Constants.SVN_CLS1 + SUFFIX_JAVA)
    // .setTextFromFile(Constants.CP1);
    // String cls1_content_after = ALICE.remoteBot()
    // .editor(Constants.SVN_CLS1_SUFFIX).getText();
    // assertFalse(cls1_content_after.equals(cls1_content_before));
    //
    // // buildSessionSequentially(SVN_PROJECT,
    // // TypeOfCreateProject.NEW_PROJECT,
    // // ALICE, BOB);
    // ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
    // .workTogetherOn().project(Constants.SVN_PROJECT);
    // BOB.superBot().confirmShellSessionInvitationAndShellAddProject(
    // Constants.SVN_PROJECT, TypeOfCreateProject.NEW_PROJECT);
    //
    // ALICE.superBot().views().sarosView()
    // .waitUntilIsInviteeInSession(BOB.superBot());
    // BOB.superBot().views().sarosView().waitUntilIsInSession();
    //
    // assertTrue(BOB.superBot().views().packageExplorerView()
    // .isProjectManagedBySVN(Constants.SVN_PROJECT));
    // BOB.superBot()
    // .views()
    // .packageExplorerView()
    // .selectClass(Constants.SVN_PROJECT, Constants.SVN_PKG,
    // Constants.SVN_CLS1).open();
    // assertEquals(cls1_content_after,
    // BOB.remoteBot().editor(Constants.SVN_CLS1_SUFFIX).getText());
    // }
}
