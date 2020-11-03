package saros.stf.client.util;

import static saros.stf.shared.Constants.ACCEPT;
import static saros.stf.shared.Constants.NODE_SAROS;
import static saros.stf.shared.Constants.SHELL_SESSION_INVITATION;
import static saros.stf.shared.Constants.VIEW_SAROS;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import saros.net.xmpp.JID;
import saros.stf.client.tester.AbstractTester;
import saros.stf.shared.Constants.SessionInvitationModality;
import saros.stf.shared.Constants.TypeOfCreateProject;

public class Util {
  /**
   * Closes the following views:
   *
   * <ol>
   *   <li>Problems
   *   <li>Javadoc
   *   <li>Declaration
   *   <li>Task List
   *   <li>Outline
   * </ol>
   *
   * @param tester the remote tester e.g ALICE
   */
  public static void closeUnnecessaryViews(AbstractTester tester) throws RemoteException {
    if (tester.remoteBot().isViewOpen("Problems")) tester.remoteBot().view("Problems").close();

    if (tester.remoteBot().isViewOpen("Javadoc")) tester.remoteBot().view("Javadoc").close();

    if (tester.remoteBot().isViewOpen("Declaration"))
      tester.remoteBot().view("Declaration").close();

    if (tester.remoteBot().isViewOpen("Task List")) tester.remoteBot().view("Task List").close();

    if (tester.remoteBot().isViewOpen("Outline")) tester.remoteBot().view("Outline").close();
  }

  /**
   * Opens the <b>Saros</b> view
   *
   * @param tester the remote tester e.g. ALICE
   */
  public static void openSarosView(AbstractTester tester) throws RemoteException {
    if (!tester.remoteBot().isViewOpen(VIEW_SAROS)) {
      tester.superBot().menuBar().window().showViewWithName(NODE_SAROS, VIEW_SAROS);
    }
  }

  /**
   * A convenient function to quickly build a session with a project. The project is created by this
   * method so it <b>must not</b> exist before. The invitees are invited using the given modality.
   *
   * <p><b>Note:</b> This method does not enforce that the project nature on the inviter's side is
   * correctly applied on the invitee's side. As a result, it is only supported for cases where the
   * project nature is not of interest. In cases where the same project nature is required on all
   * sides (e.g. if Java language support is needed), please use {@link
   * #setUpSessionWithJavaProject(String, SessionInvitationModality, AbstractTester,
   * AbstractTester...)} instead.
   *
   * <p><b>Note:</b> There is no guarantee that the project and its files are already shared after
   * this method returns.
   *
   * @param projectName the name of the project
   * @param sessionInvitationModality the session invitation modality to use
   * @param inviter the inviting test user, e.g. ALICE
   * @param invitees the invited test user(s), e.g. BOB, CARL
   * @throws IllegalStateException if the inviter or one of the invitee is not connected or is
   *     already in a session
   * @throws Exception for any other (internal) failure
   */
  public static void setUpSessionWithProject(
      String projectName,
      SessionInvitationModality sessionInvitationModality,
      AbstractTester inviter,
      AbstractTester... invitees)
      throws Exception {

    assertStates(true, false, inviter, invitees);

    inviter.superBot().internal().createProject(projectName);

    for (AbstractTester invitee : invitees) {
      invitee.superBot().internal().createProject(projectName);
    }

    switch (sessionInvitationModality) {
      case CONCURRENTLY:
        buildSessionConcurrently(projectName, TypeOfCreateProject.EXIST_PROJECT, inviter, invitees);
        break;

      case SEQUENTIALLY:
        buildSessionSequentially(projectName, TypeOfCreateProject.EXIST_PROJECT, inviter, invitees);
        break;

      default:
        throw new IllegalArgumentException(
            "Encountered unhandled session invitation modality " + sessionInvitationModality);
    }
  }

  /**
   * A convenient function to quickly build a session with a project and a file. The project is
   * created by this method so it <b>must not</b> exist before. The invitees are invited using the
   * given modality.
   *
   * <p><b>Note:</b> This method does not enforce that the project nature on the inviter's side is
   * correctly applied on the invitee's side. As a result, it is only supported for cases where the
   * project nature is not of interest. In cases where the same project nature is required on all
   * sides (e.g. if Java language support is needed), please use {@link
   * #setUpSessionWithJavaProjectAndClass(String, String, String, AbstractTester,
   * AbstractTester...)} instead.
   *
   * <p><b>Note:</b> There is no guarantee that the project and its files are already shared after
   * this method returns.
   *
   * @param projectName the name of the project
   * @param path the path of the file, e.g. foo/bar/readme.txt
   * @param content the content of the file
   * @param sessionInvitationModality the session invitation modality to use
   * @param inviter the inviting test user, e.g. ALICE
   * @param invitees the invited test user(s), e.g. BOB, CARL
   * @throws IllegalStateException if the inviter or one of the invitee is not connected or is
   *     already in a session
   * @throws Exception for any other (internal) failure
   */
  public static void setUpSessionWithProjectAndFile(
      String projectName,
      String path,
      String content,
      SessionInvitationModality sessionInvitationModality,
      AbstractTester inviter,
      AbstractTester... invitees)
      throws Exception {

    assertStates(true, false, inviter, invitees);

    inviter.superBot().internal().createProject(projectName);
    inviter.superBot().internal().createFile(projectName, path, content);

    for (AbstractTester invitee : invitees) {
      invitee.superBot().internal().createProject(projectName);
    }

    switch (sessionInvitationModality) {
      case CONCURRENTLY:
        buildSessionConcurrently(projectName, TypeOfCreateProject.EXIST_PROJECT, inviter, invitees);
        break;

      case SEQUENTIALLY:
        buildSessionSequentially(projectName, TypeOfCreateProject.EXIST_PROJECT, inviter, invitees);
        break;

      default:
        throw new IllegalArgumentException(
            "Encountered unhandled session invitation modality " + sessionInvitationModality);
    }
  }

  /**
   * A convenient function to quickly build a session with a java project. The project is created by
   * this method so it <b>must not</b> exist before. The invitees are invited using the given
   * modality.
   *
   * <p><b>Note:</b> There is no guarantee that the project and its files are already shared after
   * this method returns.
   *
   * @param projectName the name of the project
   * @param sessionInvitationModality the session invitation modality to use
   * @param inviter the inviting test user, e.g. ALICE
   * @param invitees the invited test user(s), e.g. BOB, CARL
   * @throws IllegalStateException if the inviter or one of the invitees is not connected or is
   *     already in a session
   * @throws Exception for any other (internal) failure
   */
  public static void setUpSessionWithJavaProject(
      String projectName,
      SessionInvitationModality sessionInvitationModality,
      AbstractTester inviter,
      AbstractTester... invitees)
      throws Exception {

    assertStates(true, false, inviter, invitees);

    for (AbstractTester invitee : invitees) {
      invitee.superBot().internal().createJavaProject(projectName);
    }

    switch (sessionInvitationModality) {
      case CONCURRENTLY:
        buildSessionConcurrently(projectName, TypeOfCreateProject.EXIST_PROJECT, inviter, invitees);
        break;

      case SEQUENTIALLY:
        buildSessionSequentially(projectName, TypeOfCreateProject.EXIST_PROJECT, inviter, invitees);
        break;

      default:
        throw new IllegalArgumentException(
            "Encountered unhandled session invitation modality " + sessionInvitationModality);
    }
  }

  /**
   * A convenient function to quickly build a session with a java project and a class. The project
   * is created by this method so it <b>must not</b> exist before. The invitees are invited
   * concurrently.
   *
   * <p><b>Note:</b> There is no guarantee that the project and its files are already shared after
   * this method returns.
   *
   * @param projectName the name of the project
   * @param packageName the name of the package
   * @param className the name of the class without .java or .class suffix
   * @param inviter the inviting test user, e.g. ALICE
   * @param invitees the invited test user(s), e.g. BOB, CARL
   * @throws IllegalStateException if the inviter or one of the invitee is not connected or is
   *     already in a session
   * @throws Exception for any other (internal) failure
   */
  public static void setUpSessionWithJavaProjectAndClass(
      String projectName,
      String packageName,
      String className,
      AbstractTester inviter,
      AbstractTester... invitees)
      throws Exception {

    assertStates(true, false, inviter, invitees);

    inviter.superBot().internal().createJavaProject(projectName);
    inviter.superBot().internal().createJavaClass(projectName, packageName, className);

    for (AbstractTester invitee : invitees) {
      invitee.superBot().internal().createJavaProject(projectName);
    }

    buildSessionConcurrently(projectName, TypeOfCreateProject.EXIST_PROJECT, inviter, invitees);
  }

  /**
   * Creates a project with an empty file for every tester in his workspace. The project and the
   * file <b>must not</b> exist.
   *
   * @param projectName the name of the project
   * @param path the path of the file e.g. foo/bar/readme.txt
   * @param testers e.g. ALICE, CARL
   * @throws Exception if a (internal) failure occurs
   */
  public static void createProjectWithEmptyFile(
      String projectName, String path, AbstractTester... testers) throws Exception {

    for (AbstractTester tester : testers) {
      tester.superBot().internal().createProject(projectName);
      tester.superBot().internal().createFile(projectName, path, "");
    }
  }

  /**
   * Adds a project to the current session. This is done sequentially, so the project is send to the
   * invitees one after another.
   *
   * <p><b>Note:</b> The creation type {@link TypeOfCreateProject#NEW_PROJECT} does not enforce that
   * the project nature on the inviter's side is correctly applied on the invitee's side. As a
   * result, it is only supported for cases where the project nature is not of interest. In cases
   * where the same project nature is necessary on all sides (e.g. if Java language support is
   * needed), please use {@link TypeOfCreateProject#EXIST_PROJECT}. For Java projects, the utility
   * method {@link #addJavaProjectToSessionSequentially(String, AbstractTester, AbstractTester...)}
   * can be used for this purpose.
   *
   * <p><b>Note:</b> Adding a project that is already shared or does not exist results in unexpected
   * behavior.
   *
   * <p><b>Note:</b> There is no guarantee that the project and its files are already shared after
   * this method returns.
   *
   * @param projectName the name of the project
   * @param projectType the type of project that should be used on the invitee side e.g new, use
   *     existing ...
   * @param inviter the inviting test user, e.g. ALICE
   * @param invitees the invited test user(s), e.g. BOB, CARL
   * @throws IllegalStateException if the inviter or one of the invitee is not connected or is not
   *     in a session
   * @throws Exception for any other (internal) failure
   */
  public static void addProjectToSessionSequentially(
      String projectName,
      TypeOfCreateProject projectType,
      AbstractTester inviter,
      AbstractTester... invitees)
      throws Exception {

    assertStates(true, true, inviter, invitees);

    inviter.superBot().menuBar().saros().addProjects(projectName);

    for (AbstractTester invitee : invitees) {
      invitee.superBot().confirmShellAddProjectUsingWhichProject(projectName, projectType);
    }
  }

  /**
   * Adds a project to the current session. This is done sequentially, so the project is send to the
   * invitees one after another.
   *
   * <p>The projects to add is created on the invitee's side as part of this process. It <b>must
   * not</b> already exist on the invitee's side. Adding a project that already exists on the
   * invitee's side or is already shared results in unexpected behavior.
   *
   * <p><b>Note:</b> There is no guarantee that the project and its files are already shared after
   * this method returns.
   *
   * @param projectName the name of the project
   * @param inviter the inviting test user, e.g. ALICE
   * @param invitees the invited test user(s), e.g. BOB, CARL
   * @throws IllegalStateException if the inviter or one of the invitee is not connected or is not
   *     in a session
   * @throws Exception for any other (internal) failure
   */
  public static void addJavaProjectToSessionSequentially(
      String projectName, AbstractTester inviter, AbstractTester... invitees) throws Exception {

    assertStates(true, true, inviter, invitees);

    for (AbstractTester invitee : invitees) {
      invitee.superBot().internal().createJavaProject(projectName);
    }

    inviter.superBot().menuBar().saros().addProjects(projectName);

    for (AbstractTester invitee : invitees) {
      invitee
          .superBot()
          .confirmShellAddProjectUsingWhichProject(projectName, TypeOfCreateProject.EXIST_PROJECT);
    }
  }

  /**
   * Establish a Saros session with the given invitees. Every invitee is invited one bye one.
   *
   * <p><b>Note:</b> The creation type {@link TypeOfCreateProject#NEW_PROJECT} does not enforce that
   * the project nature on the inviter's side is correctly applied on the invitee's side. As a
   * result, it is only supported for cases where the project nature is not of interest. In cases
   * where the same project nature is necessary on all sides (e.g. if Java language support is
   * needed), please use {@link TypeOfCreateProject#EXIST_PROJECT}. For Java projects, the utility
   * method {@link #setUpSessionWithJavaProject(String, SessionInvitationModality, AbstractTester,
   * AbstractTester...)} can be used for this purpose.
   *
   * <p><b>Note:</b> Establishing session with a project that is already shared or does not exist
   * results in unexpected behavior.
   *
   * <p><b>Note:</b> There is no guarantee that the project and its files are already shared after
   * this method returns.
   *
   * @param projectName the name of the project to share
   * @param projectType the type of project that should be used on the invitee side e.g new, use
   *     existing ...
   * @param inviter the inviting test user, e.g. ALICE
   * @param invitees the invited test user(s), e.g. BOB, CARL
   * @throws IllegalStateException if the inviter or one of the invitee is not connected or is
   *     already in a session
   * @throws Exception for any other (internal) failure
   */
  public static void buildSessionSequentially(
      String projectName,
      TypeOfCreateProject projectType,
      AbstractTester inviter,
      AbstractTester... invitees)
      throws Exception {

    assertStates(true, false, inviter, invitees);

    JID[] inviteesJID = getJID(invitees);

    inviter.superBot().menuBar().saros().shareProjects(projectName, inviteesJID);

    for (AbstractTester invitee : invitees) {
      invitee.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);
      invitee.superBot().confirmShellAddProjectUsingWhichProject(projectName, projectType);
    }
  }

  /**
   * Establish a Saros session with the given invitees. All invitees are invited simultaneously.
   *
   * <p><b>Note:</b> The creation type {@link TypeOfCreateProject#NEW_PROJECT} does not enforce that
   * the project nature on the inviter's side is correctly applied on the invitee's side. As a
   * result, it is only supported for cases where the project nature is not of interest. In cases
   * where the same project nature is necessary on all sides (e.g. if Java language support is
   * needed), please use {@link TypeOfCreateProject#EXIST_PROJECT}. For Java projects, the utility
   * method {@link #setUpSessionWithJavaProject(String, SessionInvitationModality, AbstractTester,
   * AbstractTester...)} can be used for this purpose.
   *
   * <p><b>Note:</b> Establishing session with a project that is already shared or does not exist
   * results in unexpected behavior.
   *
   * <p><b>Note:</b> There is no guarantee that the project and its files are already shared after
   * this method returns.
   *
   * @param projectName the name of the project to share
   * @param projectType the type of project that should be used on the invitee side e.g new, use
   *     existing ...
   * @param inviter the inviting test user, e.g. ALICE
   * @param invitees the invited test user(s), e.g. BOB, CARL
   * @throws IllegalStateException if the inviter or one of the invitee is not connected or is
   *     already in a session
   * @throws Exception for any other (internal) failure
   */
  public static void buildSessionConcurrently(
      final String projectName,
      final TypeOfCreateProject projectType,
      AbstractTester inviter,
      AbstractTester... invitees)
      throws Exception {

    assertStates(true, false, inviter, invitees);

    inviter.superBot().menuBar().saros().shareProjects(projectName, Util.getJID(invitees));

    List<Callable<Void>> joinSessionTasks = new ArrayList<Callable<Void>>();
    for (final AbstractTester invitee : invitees) {
      joinSessionTasks.add(
          new Callable<Void>() {
            @Override
            public Void call() throws Exception {
              invitee
                  .superBot()
                  .confirmShellSessionInvitationAndShellAddProject(projectName, projectType);
              return null;
            }
          });
    }

    workAll(joinSessionTasks);
  }

  /**
   * Activates the Follow mode feature. Activating Follow mode when the followed participant has no
   * open editors results in failure of this method.
   *
   * @param followedParticipant the participant to follow e.g ALICE
   * @param participants the list of participants who want to activate Follow mode e.g BOB, CARL
   * @throws TimeoutException if the follow participant has no editor open or the editor activity
   *     was not received yet
   * @throws IllegalStateException if the followed participant or one of the participants is not
   *     connected or not in a session
   * @throws Exception for any other (internal) failure
   */
  public static void activateFollowMode(
      final AbstractTester followedParticipant, AbstractTester... participants) throws Exception {

    assertStates(true, true, followedParticipant, participants);

    for (AbstractTester tester : participants)
      tester
          .superBot()
          .views()
          .sarosView()
          .selectUser(followedParticipant.getJID())
          .followParticipant();
  }

  /**
   * Adds testers to the contact list of the tester. All testers will have the tester added to their
   * contact list as well.
   *
   * @param tester the tester who wants to add testers to his contact list e.g ALICE
   * @param testers the testers to add, e.g BOB, CARL
   * @throws IllegalStateException if the tester or one of the testers is not connected
   * @throws Exception for any other (internal) failure
   */
  public static void addTestersToContactList(AbstractTester tester, AbstractTester... testers)
      throws Exception {

    assertStates(true, null, tester, testers);

    // TODO Remove loop, nobody ever calls this with #testers > 1
    for (AbstractTester contact : testers) {
      if (!tester.superBot().views().sarosView().isInContactList(contact.getJID())) {
        tester.superBot().views().sarosView().addContact(contact.getJID());
        contact.superBot().confirmShellRequestOfSubscriptionReceived();
        tester.superBot().confirmShellRequestOfSubscriptionReceived();
      }
    }
  }

  /**
   * Removes the given testers from the contact list of the tester. All testers will have the tester
   * removed from their contact list as well.
   *
   * @param tester the tester who wants to remove testers from his contact list e.g ALICE
   * @param testers the testers to remove, e.g BOB, CARL
   * @throws IllegalStateException if the tester or one of the testers is not connected
   * @throws Exception for any other (internal) failure
   */
  public static void removeTestersFromContactList(AbstractTester tester, AbstractTester... testers)
      throws Exception {

    assertStates(true, null, tester, testers);

    // TODO Remove loop, nobody ever calls this with #testers > 1
    for (AbstractTester contact : testers) {

      if (!tester.superBot().views().sarosView().isInContactList(contact.getJID())) continue;

      boolean isInRemoteContactList =
          contact.superBot().views().sarosView().isInContactList(tester.getJID());

      tester.superBot().views().sarosView().selectContact(contact.getJID()).delete();

      if (!isInRemoteContactList) continue;

      // Dirty Hack, retry twice

      for (int i = 0; i < 2; i++) {
        try {
          /*
           * this may throw a widget is disposed exception or some
           * other stuff depending on how the content provider for the
           * session tree viewer updates the contents when the new
           * subscription state is received.
           */

          contact.superBot().views().sarosView().selectContact(tester.getJID()).delete();

          break;
        } catch (Exception e) {
          if (i >= 1) throw e;
        }
      }
    }
  }

  /**
   * Adds testers to the current session.
   *
   * <p><b>Note:</b> The creation type {@link TypeOfCreateProject#NEW_PROJECT} does not enforce that
   * the project nature on the inviter's side is correctly applied on the invitee's side. As a
   * result, it is only supported for cases where the project nature is not of interest. In cases
   * where the same project nature is necessary on all sides (e.g. if Java language support is
   * needed), please use {@link TypeOfCreateProject#EXIST_PROJECT}.
   *
   * <p><b>Note:</b> There is no guarantee that the project and its files are already shared after
   * this method returns.
   *
   * @param projectName the name of the project which <b>must</b> be shared in the current session
   * @param projectType the type of project that should be used on the invitee side e.g new, use
   *     existing ...
   * @param inviter the test who must be host of the current session
   * @param invitees the testers you want to invite to your session
   * @throws IllegalStateException if the inviter or one of the invitees is not connected, one of
   *     the invitee is already in a session or the inviter is not host
   * @throws Exception for any other (internal) failure
   */
  public static void addTestersToSession(
      final String projectName,
      final TypeOfCreateProject projectType,
      AbstractTester inviter,
      AbstractTester... invitees)
      throws Exception {

    assertStates(true, null, inviter, invitees);

    if (!inviter.superBot().views().sarosView().isInSession())
      throw new IllegalStateException(inviter + " is not in a session");

    if (!inviter.superBot().views().sarosView().isHost())
      throw new IllegalStateException(inviter + " is not host of the current session");

    for (AbstractTester invitee : invitees) {
      if (invitee.superBot().views().sarosView().isInSession())
        throw new IllegalStateException(invitee + " is already in a session");
    }

    inviter.superBot().menuBar().saros().addContactsToSession(Util.getBaseJID(invitees));

    List<Callable<Void>> joinSessionTasks = new ArrayList<Callable<Void>>();

    for (final AbstractTester tester : invitees) {
      joinSessionTasks.add(
          new Callable<Void>() {
            @Override
            public Void call() throws Exception {
              tester.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);
              tester.superBot().confirmShellAddProjectUsingWhichProject(projectName, projectType);
              return null;
            }
          });
    }

    Util.workAll(joinSessionTasks);
  }

  /**
   * Returns the base part of the JID from the tester.
   *
   * @param tester a list of tester
   * @return the base part JIDs of the tester in the same order as in the tester list
   */
  public static String[] getBaseJID(AbstractTester... tester) {
    String[] peerBaseJIDs = new String[tester.length];
    for (int i = 0; i < tester.length; i++) {
      peerBaseJIDs[i] = tester[i].getBaseJid();
    }
    return peerBaseJIDs;
  }

  /**
   * Returns the JID from the tester.
   *
   * @param tester a list of tester
   * @return the JIDs of the tester in the same order as in the tester list
   */
  public static JID[] getJID(AbstractTester... tester) {
    JID[] peerBaseJIDs = new JID[tester.length];
    for (int i = 0; i < tester.length; i++) {
      peerBaseJIDs[i] = tester[i].getJID();
    }
    return peerBaseJIDs;
  }

  /**
   * Grants write access to the given testers.
   *
   * @param host the host of the current session, e.g ALICE
   * @param testers testers that are in the current session and should gain write access, e.g BOB,
   *     CARL
   * @throws IllegalStateException if the host or one of the participants is not connected or is not
   *     in a session or the host is not host of the current session host
   * @throws Exception for any other (internal) failure
   */
  public static void grantWriteAccess(AbstractTester host, AbstractTester... testers)
      throws Exception {

    assertStates(true, true, host, testers);

    if (!host.superBot().views().sarosView().isHost())
      throw new IllegalStateException(host + " is not host of the current session");

    for (AbstractTester tester : testers) {
      if (tester.superBot().views().sarosView().isInSession()
          && host.superBot().views().sarosView().selectUser(tester.getJID()).hasReadOnlyAccess()) {
        host.superBot().views().sarosView().selectUser(tester.getJID()).grantWriteAccess();
      }
    }
  }

  /**
   * Stops the follow mode feature for all given testers.
   *
   * @param testers testers that are currently in the a session where follow mode should been
   *     stopped
   * @throws IllegalStateException if one of the testers is not connected or not in a session
   * @throws Exception for any other (internal) failure
   */
  public static void stopFollowModeSequentially(AbstractTester... testers) throws Exception {

    assertStates(true, true, testers);

    for (AbstractTester tester : testers) {
      if (tester.superBot().views().sarosView().isInSession()
          && tester.superBot().views().sarosView().isFollowing()) {
        JID followedTesterJID = tester.superBot().views().sarosView().getFollowedUser();
        tester.superBot().views().sarosView().selectUser(followedTesterJID).stopFollowing();
      }
    }
  }

  /**
   * Stops the follow mode feature. This is done concurrently for all given testers.
   *
   * @param testers testers that are currently in the a session where follow mode should been
   *     stopped
   * @throws IllegalStateException if one of the testers is not connected or not in a session
   * @throws Exception for any other (internal) failure
   */
  public static void stopFollowModeConcurrently(AbstractTester... testers) throws Exception {

    assertStates(true, true, testers);

    List<Callable<Void>> stopFollowTasks = new ArrayList<Callable<Void>>();
    for (int i = 0; i < testers.length; i++) {
      final AbstractTester tester = testers[i];
      stopFollowTasks.add(
          new Callable<Void>() {
            @Override
            public Void call() throws Exception {
              JID followedTesterJID = tester.superBot().views().sarosView().getFollowedUser();
              tester.superBot().views().sarosView().selectUser(followedTesterJID).stopFollowing();
              return null;
            }
          });
    }
    workAll(stopFollowTasks);
  }

  /**
   * Rebuilds a session if necessary with the given testers. If the project does not exists on the
   * inviter side an empty non Java Project will be created. Participants that are already (in a
   * different) session will not be invited.
   *
   * <p><b>Note:</b> there is no guarantee that the project and its files are already shared after
   * this method returns
   *
   * <p><b>Note:</b> calling this method with always different project names during a session will
   * result in unexpected behavior
   *
   * @param projectName the name of the project
   * @param inviter e.g. ALICE
   * @param invitees e.g. BOB, CARL
   * @throws IllegalStateException if the host or one of the participants is not connected
   * @throws Exception for any other (internal) failure
   */
  public static void reBuildSession(
      String projectName, AbstractTester inviter, AbstractTester... invitees) throws Exception {

    assertStates(true, null, inviter, invitees);

    if (!inviter.superBot().internal().existsResource(projectName))
      inviter.superBot().internal().createProject(projectName);

    for (AbstractTester tester : invitees)
      if (!tester.superBot().views().sarosView().isInSession())
        tester
            .superBot()
            .views()
            .sarosView()
            .connectWith(tester.getJID(), tester.getPassword(), false);

    if (!inviter.superBot().views().sarosView().isInSession()) {

      for (AbstractTester tester : invitees) {
        buildSessionSequentially(projectName, TypeOfCreateProject.EXIST_PROJECT, inviter, tester);
      }
    }
  }

  /**
   * Converts a class path of and Eclipse JDT project to its real path.<br>
   * E.g project name = foo, pkg = my.foo, className = HelloWorld will be converted to
   * foo/src/my/foo/HelloWorld.java
   *
   * @param projectName the name of an Eclipse project
   * @param pkg a java package name
   * @param className a class name
   * @return the path to that class
   */
  public static String classPathToFilePath(String projectName, String pkg, String className) {
    return projectName + "/src/" + pkg.replace('.', '/') + "/" + className + ".java";
  }

  /**
   * Invokes all callable tasks in the list and returns when all tasks completed.
   *
   * @param <T>
   * @param tasks a list containing callable tasks
   * @return a list with the results of all tasks
   */
  public static <T> List<T> workAll(List<Callable<T>> tasks) {
    if (System.getProperty("os.name").matches("Mac OS X.*"))
      // the menubar is only active on Mac OS on the Window that has the
      // current focus
      return workAll(tasks, 1);
    else return workAll(tasks, tasks.size());
  }

  /**
   * Invokes all callable tasks in the list and returns when all tasks completed.
   *
   * @param <T>
   * @param tasks a list containing callable tasks
   * @param numberOfThreads the number of threads to use to execute the tasks
   * @return a list with the results of all tasks
   */
  public static <T> List<T> workAll(List<Callable<T>> tasks, int numberOfThreads) {

    ExecutorService pool = Executors.newFixedThreadPool(numberOfThreads);

    try {

      List<T> result = new ArrayList<T>();

      for (Future<T> future : pool.invokeAll(tasks)) result.add(future.get());

      return result;

    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } finally {
      pool.shutdown();
    }
  }

  /**
   * Waits until all threads terminate.
   *
   * @param timeout how long to wait in milliseconds until timeout
   * @param threads the threads to observe
   * @return <code>true</code> if all threads terminated, <code>false</code> if the timeout is
   *     exceeded
   */
  public static boolean joinAll(long timeout, Thread... threads) {

    long currentTime;
    long lastTime = System.currentTimeMillis();

    for (Thread thread : threads) {
      try {
        thread.join(timeout);

        if (thread.isAlive()) return false;

        currentTime = System.currentTimeMillis();
        timeout -= (System.currentTimeMillis() - lastTime);
        lastTime = currentTime;

        if (timeout <= 0) timeout = 1;

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return false;
      }
    }

    return true;
  }

  private static void assertStates(
      Boolean isConnected, Boolean isInSession, AbstractTester tester, AbstractTester... testers)
      throws Exception {
    AbstractTester[] t = new AbstractTester[testers.length + 1];
    System.arraycopy(testers, 0, t, 0, testers.length);
    t[t.length - 1] = tester;

    assertStates(isConnected, isInSession, t);
  }

  private static void assertStates(
      Boolean isConnected, Boolean isInSession, AbstractTester... testers) throws Exception {
    for (AbstractTester tester : testers) {
      if (isConnected != null && isConnected != tester.superBot().views().sarosView().isConnected())
        throw new IllegalStateException(
            tester + " is " + (isConnected ? "not connected" : "connected"));

      if (isInSession != null && isInSession != tester.superBot().views().sarosView().isInSession())
        throw new IllegalStateException(
            tester + " is " + (isInSession ? "not" : "already") + " in a session");
    }
  }
}
