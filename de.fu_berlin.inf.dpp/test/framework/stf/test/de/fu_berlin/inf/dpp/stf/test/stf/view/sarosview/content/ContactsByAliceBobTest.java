package de.fu_berlin.inf.dpp.stf.test.stf.view.sarosview.content;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;
import java.rmi.RemoteException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class ContactsByAliceBobTest extends StfTestCase {

  /**
   * Preconditions:
   *
   * <ol>
   *   <li>Alice (Host, Write Access)
   *   <li>Bob (Read-Only Access)
   *   <li>Carl (Read-Only Access)
   *   <li>Alice shares a java project with BOB
   * </ol>
   *
   * @throws RemoteException
   */
  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @After
  public void afterEveryTest() throws Exception {
    clearWorkspaces();
    resetContacts();
    resetNicknames();
  }

  @Test
  public void testAddExistingContact() throws RemoteException {
    assertTrue(ALICE.superBot().views().sarosView().isInContactList(BOB.getJID()));
    ALICE.superBot().views().sarosView().selectContacts().addContact(BOB.getJID());
    assertTrue(ALICE.superBot().views().sarosView().isInContactList(BOB.getJID()));
  }

  /**
   * Steps:
   *
   * <ol>
   *   <li>ALICE renames BOB to "BOB_stf".
   *   <li>ALICE renames BOB to "new BOB".
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>ALICE has contact with BOB and BOB's name is changed.
   *   <li>ALICE has contact with BOB and BOB's name is changed.
   * </ol>
   *
   * @throws RemoteException
   *     <p>TODO: This test isn't stable, sometime it is successful, sometime not. I think, there
   *     are some little bugs in this test case.
   */
  @Test
  public void renameContact() throws RemoteException {
    assertTrue(ALICE.superBot().views().sarosView().isInContactList(BOB.getJID()));
    ALICE.superBot().views().sarosView().selectContact(BOB.getJID()).rename(BOB.getName());

    assertTrue(ALICE.superBot().views().sarosView().isInContactList(BOB.getJID()));
    assertTrue(
        ALICE.superBot().views().sarosView().getNickname(BOB.getJID()).equals(BOB.getName()));

    ALICE.superBot().views().sarosView().selectContact(BOB.getJID()).rename("new name");
    assertTrue(ALICE.superBot().views().sarosView().isInContactList(BOB.getJID()));
    assertTrue(ALICE.superBot().views().sarosView().getNickname(BOB.getJID()).equals("new name"));
  }

  @Test
  public void addContact() throws Exception {
    Util.removeTestersFromContactList(ALICE, BOB);
    assertFalse(ALICE.superBot().views().sarosView().isInContactList(BOB.getJID()));
    assertFalse(BOB.superBot().views().sarosView().isInContactList(ALICE.getJID()));
    Util.addTestersToContactList(ALICE, BOB);
    assertTrue(ALICE.superBot().views().sarosView().isInContactList(BOB.getJID()));
    assertTrue(BOB.superBot().views().sarosView().isInContactList(ALICE.getJID()));
  }

  /**
   * Steps:
   *
   * <ol>
   *   <li>ALICE deletes BOB
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>ALICE and BOB don't contact each other
   * </ol>
   *
   * @throws RemoteException
   */
  @Test
  public void deleteContact() throws Exception {
    assertTrue(ALICE.superBot().views().sarosView().isInContactList(BOB.getJID()));
    Util.removeTestersFromContactList(ALICE, BOB);
    assertFalse(ALICE.superBot().views().sarosView().isInContactList(BOB.getJID()));
    assertFalse(BOB.superBot().views().sarosView().isInContactList(ALICE.getJID()));
    Thread.sleep(5000);
  }

  @Test
  public void workTogetherOnProject() throws RemoteException {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
    ALICE
        .superBot()
        .views()
        .sarosView()
        .selectContact(BOB.getJID())
        .workTogetherOn()
        .project(Constants.PROJECT1);
    BOB.superBot()
        .confirmShellSessionInvitationAndShellAddProject(
            Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    ALICE.superBot().views().sarosView().leaveSession();
    ALICE.superBot().views().sarosView().waitUntilIsNotInSession();
    BOB.superBot().views().sarosView().waitUntilIsNotInSession();
  }

  @Test
  public void workTogetherOnMultiProject() throws RemoteException {

    ALICE.superBot().internal().createProject("Foo");
    ALICE.superBot().internal().createProject("Bar");

    ALICE
        .superBot()
        .views()
        .sarosView()
        .selectContact(BOB.getJID())
        .workTogetherOn()
        .multipleProjects("Foo", BOB.getJID());

    BOB.superBot()
        .confirmShellSessionInvitationAndShellAddProject(
            Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT);

    ALICE.superBot().views().sarosView().leaveSession();
    ALICE.superBot().views().sarosView().waitUntilIsNotInSession();
    BOB.superBot().views().sarosView().waitUntilIsNotInSession();
  }
}
