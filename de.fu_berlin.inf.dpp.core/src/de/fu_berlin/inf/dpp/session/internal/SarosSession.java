/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.session.internal;

import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderCreatedActivity;
import de.fu_berlin.inf.dpp.activities.FolderDeletedActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IFileSystemModificationActivity;
import de.fu_berlin.inf.dpp.activities.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.NOPActivity;
import de.fu_berlin.inf.dpp.communication.extensions.KickUserExtension;
import de.fu_berlin.inf.dpp.communication.extensions.LeaveSessionExtension;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.context.IContainerContext;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.net.IConnectionManager;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.session.ColorNegotiationHook;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer.Priority;
import de.fu_berlin.inf.dpp.session.IActivityHandlerCallback;
import de.fu_berlin.inf.dpp.session.IActivityListener;
import de.fu_berlin.inf.dpp.session.IActivityProducer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionContextFactory;
import de.fu_berlin.inf.dpp.session.ISessionListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.annotations.Inject;

/**
 * TODO Review if SarosSession, ConcurrentDocumentManager, ActivitySequencer all honor start() and
 * stop() semantics.
 */
public final class SarosSession implements ISarosSession {

  private static final Logger log = Logger.getLogger(SarosSession.class);

  @Inject private UISynchronizer synchronizer;

  /* Dependencies */

  @Inject private ITransmitter transmitter;

  @Inject private XMPPConnectionService connectionService;

  @Inject private IConnectionManager connectionManager;

  private final IContainerContext containerContext;

  private final ConcurrentDocumentClient concurrentDocumentClient;

  private final ConcurrentDocumentServer concurrentDocumentServer;

  private final ActivityHandler activityHandler;

  private final CopyOnWriteArrayList<IActivityProducer> activityProducers =
      new CopyOnWriteArrayList<IActivityProducer>();

  private final List<IActivityConsumer> activeActivityConsumers =
      new CopyOnWriteArrayList<IActivityConsumer>();
  private final List<IActivityConsumer> passiveActivityConsumers =
      new CopyOnWriteArrayList<IActivityConsumer>();

  /* Instance fields */
  private final User localUser;

  private final ConcurrentHashMap<JID, User> participants = new ConcurrentHashMap<JID, User>();

  private final ConcurrentHashMap<User, IPreferenceStore> userProperties =
      new ConcurrentHashMap<User, IPreferenceStore>();

  private final SessionListenerDispatch listenerDispatch = new SessionListenerDispatch();

  private final User hostUser;

  private final SharedProjectMapper projectMapper;

  private final MutablePicoContainer sessionContainer;

  private final StopManager stopManager;

  private final ChangeColorManager changeColorManager;

  private final PermissionManager permissionManager;

  private final ActivitySequencer activitySequencer;

  private final UserInformationHandler userListHandler;

  private final String sessionID;

  private boolean started = false;
  private boolean stopped = false;

  private final ActivityQueuer activityQueuer;
  private boolean starting = false;
  private boolean stopping = false;

  private final Object componentAccessLock = new Object();

  /**
   * @JTourBusStop 5, Activity sending, Forwarding the IActivity:
   *
   * <p>This is where the SarosSession will receive the activity. This listener it is not part of
   * the ISarosSession interface to avoid misuse.
   */
  private final IActivityListener activityListener =
      new IActivityListener() {
        @Override
        public void created(final IActivity activity) {
          if (activity == null) throw new NullPointerException("activity is null");

          activityHandler.handleOutgoingActivities(Collections.singletonList(activity));
        }
      };

  private final IActivityHandlerCallback activityCallback =
      new IActivityHandlerCallback() {

        @Override
        public void send(List<User> recipients, IActivity activity) {
          sendActivity(recipients, activity);
        }

        @Override
        public void execute(IActivity activity) {
          /**
           * @JTourBusStop 10, Activity sending, Local Execution, first dispatch:
           *
           * <p>Afterwards, every registered ActivityConsumer is informed about the remote activity
           * that should be executed locally. This is the first dispatch: Each activity is
           * dispatched to an array of consumers.
           */
          for (IActivityConsumer consumer : passiveActivityConsumers) {
            try {
              consumer.exec(activity);
            } catch (RuntimeException e) {
              log.error(
                  "error while invoking passive activity consumer: "
                      + consumer
                      + ", activity: "
                      + activity,
                  e);
            }
          }

          for (IActivityConsumer consumer : activeActivityConsumers) {
            try {
              consumer.exec(activity);
            } catch (RuntimeException e) {
              log.error(
                  "error while invoking active activity consumer: "
                      + consumer
                      + ", activity: "
                      + activity,
                  e);
            }
          }

          /*
           * TODO depending if we call this before or after the consumer
           * dispatch a consumer may see the resource as shared or not. This
           * is weird as there is NO guideline currently on how
           * IFileSystemModificationActivity should be treated when it comes
           * to resource query of the current session, e.g isShared()
           */

          if (activity instanceof IFileSystemModificationActivity)
            updatePartialSharedResources((IFileSystemModificationActivity) activity);
        }
      };

  // FIXME those parameter passing feels strange, find a better way
  /** Constructor for host. */
  public SarosSession(
      final String id, IPreferenceStore properties, IContainerContext containerContext) {
    this(id, containerContext, properties, /* unused */ null, /* unused */ null);
  }

  /** Constructor for client. */
  public SarosSession(
      final String id,
      JID hostJID,
      IPreferenceStore localProperties,
      IPreferenceStore hostProperties,
      IContainerContext containerContext) {
    this(id, containerContext, localProperties, hostJID, hostProperties);
  }

  @Override
  public void addSharedResources(IProject project, String id, List<IResource> resources) {

    Set<IResource> allResources = null;

    if (resources != null) {
      allResources = new HashSet<IResource>();
      for (IResource resource : resources) allResources.addAll(getAllNonSharedChildren(resource));
    }

    if (!projectMapper.isShared(project)) {
      // new project
      if (allResources == null) {
        // new fully shared project
        projectMapper.addProject(id, project, false);
      } else {
        // new partially shared project
        projectMapper.addProject(id, project, true);
        projectMapper.addResources(project, allResources);
      }

      listenerDispatch.projectAdded(project);
    } else {
      // existing project
      if (allResources == null) {
        // upgrade partially shared to fully shared project
        projectMapper.addProject(id, project, false);
      } else {
        // increase scope of partially shared project
        projectMapper.addResources(project, allResources);
      }
    }

    listenerDispatch.resourcesAdded(project);
  }

  /**
   * Recursively get non-shared resources
   *
   * @param resource of type {@link IResource#FOLDER} or {@link IResource#FILE}
   */
  private List<IResource> getAllNonSharedChildren(IResource resource) {
    List<IResource> list = new ArrayList<IResource>();

    if (isShared(resource)) return list;

    list.add(resource);

    if (resource.getType() == IResource.FOLDER) {
      try {
        IResource[] members = resource.getAdapter(IFolder.class).members();

        for (int i = 0; i < members.length; i++) list.addAll(getAllNonSharedChildren(members[i]));
      } catch (IOException e) {
        log.error("Can't get children of folder " + resource, e);
      }
    }

    return list;
  }

  @Override
  public List<User> getUsers() {
    return new ArrayList<User>(participants.values());
  }

  @Override
  public List<User> getRemoteUsers() {
    List<User> result = new ArrayList<User>();
    for (User user : getUsers()) {
      if (user.isRemote()) result.add(user);
    }
    return result;
  }

  @Override
  public boolean userHasProject(User user, IProject project) {
    return projectMapper.userHasProject(user, project);
  }

  @Override
  public void changePermission(final User user, final Permission newPermission)
      throws CancellationException, InterruptedException {

    permissionManager.changePermission(user, newPermission);
  }

  public void setPermission(final User user, final Permission permission) {

    if (user == null || permission == null) throw new IllegalArgumentException();

    synchronizer.syncExec(
        ThreadUtils.wrapSafe(
            log,
            new Runnable() {
              @Override
              public void run() {
                user.setPermission(permission);
                listenerDispatch.permissionChanged(user);
              }
            }));

    log.info("user " + user + " permission changed: " + permission);
  }

  @Override
  public String getID() {
    return sessionID;
  }

  @Override
  public User getHost() {
    return hostUser;
  }

  @Override
  public boolean isHost() {
    return localUser.isHost();
  }

  @Override
  public boolean hasWriteAccess() {
    return localUser.hasWriteAccess();
  }

  /*
   * FIXME only accept a JID or create a method session.createUser to ensure
   * proper initialization etc. of User objects !
   */
  @Override
  public void addUser(final User user, IPreferenceStore properties) {

    // TODO synchronize this method !

    JID jid = user.getJID();

    if (!jid.isResourceQualifiedJID())
      throw new IllegalArgumentException(
          "network id of user " + user + " is not unique, resource part of JID is missing");

    user.setInSession(true);

    if (participants.putIfAbsent(jid, user) != null) {
      log.error("user " + user + " added twice to SarosSession", new StackTrace());
      throw new IllegalArgumentException();
    }
    if (this.userProperties.putIfAbsent(user, properties) != null) {
      log.warn("user " + user + " already has properties");
    }

    /*
     *
     * as long as we do not know when something is send to someone this will
     * always produce errors ... swapping synchronizeUserList and userJoined
     * can produce different results
     */

    if (isHost()) {

      activitySequencer.registerUser(user);

      List<User> timedOutUsers =
          userListHandler.synchronizeUserList(getUsers(), null, getRemoteUsers());

      if (!timedOutUsers.isEmpty()) {
        activitySequencer.unregisterUser(user);
        participants.remove(jid);
        // FIXME do not throw a runtime exception here
        throw new RuntimeException(
            "could not synchronize user list, following users did not respond: "
                + StringUtils.join(timedOutUsers, ", "));
      }
    }

    synchronizer.syncExec(
        ThreadUtils.wrapSafe(
            log,
            new Runnable() {
              @Override
              public void run() {
                listenerDispatch.userJoined(user);
              }
            }));

    log.info("user " + user + " joined session");
  }

  @Override
  public void userStartedQueuing(final User user) {

    log.info("user " + user + " started queuing projects and can receive IResourceActivities");

    if (isHost()) {
      /*
       * Notify the system that the user's client now knows about all
       * currently shared projects and can handle (process or queue)
       * activities related to them.
       *
       * Only the host needs this information because non-hosts don't have
       * to decide whom to send activities to - they just send them to the
       * host, who decides for them.
       */
      projectMapper.addMissingProjectsToUser(user);
    }

    synchronizer.syncExec(
        ThreadUtils.wrapSafe(
            log,
            new Runnable() {
              @Override
              public void run() {
                listenerDispatch.userStartedQueuing(user);
              }
            }));
  }

  @Override
  public void userFinishedProjectNegotiation(final User user) {

    log.info("user " + user + " now has Projects and can process IResourceActivities");

    synchronizer.syncExec(
        ThreadUtils.wrapSafe(
            log,
            new Runnable() {
              @Override
              public void run() {
                listenerDispatch.userFinishedProjectNegotiation(user);
              }
            }));

    if (isHost()) {

      JID jid = user.getJID();
      /**
       * This informs all participants, that a user is now able to process IResourceActivities.
       * After receiving this message the participants will send their awareness information.
       */
      userListHandler.sendUserFinishedProjectNegotiation(getRemoteUsers(), jid);
    }
  }

  public void userColorChanged(User user) {
    listenerDispatch.userColorChanged(user);
  }

  @Override
  public void removeUser(final User user) {
    synchronized (this) {
      if (!user.isInSession()) {
        log.warn("user " + user + " is already or is currently removed from the session");
        return;
      }

      user.setInSession(false);
    }

    JID jid = user.getJID();
    if (participants.remove(jid) == null) {
      log.error("tried to remove user " + user + " who was never added to the session");
      return;
    }
    if (userProperties.remove(user) == null) {
      log.error("tried to remove properties of user " + user + " that were never initialized");
    }

    activitySequencer.unregisterUser(user);

    projectMapper.userLeft(user);

    List<User> currentRemoteUsers = getRemoteUsers();

    if (isHost() && !currentRemoteUsers.isEmpty()) {

      List<User> timedOutUsers =
          userListHandler.synchronizeUserList(
              null, Collections.singletonList(user), currentRemoteUsers);

      if (!timedOutUsers.isEmpty()) {
        log.error(
            "could not synchronize user list properly, following users did not respond: "
                + StringUtils.join(timedOutUsers, ", "));
      }
    }

    synchronizer.syncExec(
        ThreadUtils.wrapSafe(
            log,
            new Runnable() {
              @Override
              public void run() {
                listenerDispatch.userLeft(user);
              }
            }));

    // TODO what is to do here if no user with write access exists anymore?

    // Disconnect bytestream connection when user leaves session to
    // prevent idling connection when not needed anymore.
    connectionManager.closeConnection(ISarosSession.SESSION_CONNECTION_ID, jid);

    log.info("user " + user + " left session");
  }

  @Override
  public void kickUser(final User user) {

    if (!isHost())
      throw new IllegalStateException("only the host can kick users from the current session");

    if (user.equals(getLocalUser()))
      throw new IllegalArgumentException("the local user cannot kick itself out of the session");

    try {
      transmitter.send(
          SESSION_CONNECTION_ID,
          user.getJID(),
          KickUserExtension.PROVIDER.create(new KickUserExtension(getID())));
    } catch (IOException e) {
      log.warn(
          "could not kick user "
              + user
              + " from the session because the connection to the user is already lost");
    }

    removeUser(user);
  }

  @Override
  public void addListener(ISessionListener listener) {
    listenerDispatch.add(listener);
  }

  @Override
  public void removeListener(ISessionListener listener) {
    listenerDispatch.remove(listener);
  }

  @Override
  public Set<IProject> getProjects() {
    return projectMapper.getProjects();
  }

  // FIXME synchronization
  @Override
  public void start() {
    if (started || stopped) {
      throw new IllegalStateException();
    }

    synchronized (componentAccessLock) {
      starting = true;
    }

    sessionContainer.start();

    for (User user : getRemoteUsers()) activitySequencer.registerUser(user);

    synchronized (componentAccessLock) {
      starting = false;
      started = true;
    }
  }

  /**
   * Stops this session and performing cleanup as necessary. All remote users will also be notified
   * about the local session stop.
   *
   * @throws IllegalStateException if the session is already stopped or was not started at all
   */
  // FIXME synchronization
  public void stop(SessionEndReason reason) {
    if (!started || stopped) {
      throw new IllegalStateException();
    }

    synchronized (componentAccessLock) {
      stopping = true;
    }

    containerContext.removeChildContainer(sessionContainer);
    sessionContainer.stop();
    sessionContainer.dispose();

    if (reason == SessionEndReason.LOCAL_USER_LEFT) {
      notifyParticipants();
    }

    for (User user : getRemoteUsers())
      connectionManager.closeConnection(ISarosSession.SESSION_CONNECTION_ID, user.getJID());

    synchronized (componentAccessLock) {
      stopping = false;
      stopped = true;
    }
  }

  /**
   * Notifies other participants that the local session has ended. If the local user is the host,
   * all other participants are notified. Otherwise, only the host is notified.
   */
  private void notifyParticipants() {
    List<User> usersToNotify;

    if (isHost()) usersToNotify = getRemoteUsers();
    else usersToNotify = Collections.singletonList(getHost());

    for (User user : usersToNotify) {
      try {
        transmitter.send(
            SESSION_CONNECTION_ID,
            user.getJID(),
            LeaveSessionExtension.PROVIDER.create(new LeaveSessionExtension(getID())));
      } catch (IOException e) {
        log.warn("failed to notify user " + user + " about local session stop", e);
      }
    }
  }

  @Override
  public User getUser(JID jid) {

    if (jid == null) throw new IllegalArgumentException("jid is null");

    if (jid.isBareJID())
      throw new IllegalArgumentException("JID is not resource qualified: " + jid);

    User user = participants.get(jid);

    if (user == null || !user.getJID().strictlyEquals(jid)) return null;

    return user;
  }

  @Override
  public IPreferenceStore getUserProperties(User user) {
    if (user == null) throw new IllegalArgumentException("user is null");

    return userProperties.get(user);
  }

  @Override
  public JID getResourceQualifiedJID(JID jid) {

    if (jid == null) throw new IllegalArgumentException("jid is null");

    User user = participants.get(jid);

    if (user == null) return null;

    return user.getJID();
  }

  @Override
  public User getLocalUser() {
    return localUser;
  }

  @Override
  public ConcurrentDocumentClient getConcurrentDocumentClient() {
    return concurrentDocumentClient;
  }

  @Override
  public ConcurrentDocumentServer getConcurrentDocumentServer() {
    if (!isHost()) throw new IllegalStateException("the session is running in client mode");

    return concurrentDocumentServer;
  }

  @Override
  public void exec(List<IActivity> activities) {
    /**
     * @JTourBusStop 7, Activity sending, Incoming activities:
     *
     * <p>Incoming activities will arrive here. The ActivitySequencer calls this method for
     * activities received over the Network Layer.
     */
    final List<IActivity> valid = new ArrayList<IActivity>();

    // Check every incoming activity for validity
    for (IActivity activity : activities) {
      if (activity.isValid()) valid.add(activity);
      else log.error("could not handle incoming activity: " + activity);
    }

    List<IActivity> processed = activityQueuer.process(valid);
    activityHandler.handleIncomingActivities(processed);
  }

  /*
   * FIXME most (if not all checks) to send or not activities should be
   * handled by the activity handler and not here !
   */
  private void sendActivity(final List<User> recipients, final IActivity activity) {

    if (recipients == null) throw new IllegalArgumentException();

    if (activity == null) throw new IllegalArgumentException();

    // If we don't have any shared projects don't send ResourceActivities
    if (projectMapper.size() == 0 && (activity instanceof IResourceActivity)) {
      return;
    }

    boolean send = true;

    /*
     * To explain some magic: When the host generates an activity this
     * method will be called multiple times. It is first called with the
     * host as it is only recipient and then it is called again for the
     * other recipients.
     */
    // handle IFileSystemModificationActivities to update ProjectMapper
    if (activity instanceof IFileSystemModificationActivity
        && (!isHost() || (isHost() && recipients.contains(getLocalUser())))) {

      send = updatePartialSharedResources((IFileSystemModificationActivity) activity);
    }

    if (!send) return;

    try {
      activitySequencer.sendActivity(recipients, activity);
    } catch (IllegalArgumentException e) {
      log.warn("could not serialize activity: " + activity, e);
    }
  }

  /**
   * Must be called to update the project mapper when changes on shared files or shared folders
   * happened.
   *
   * @param activity {@link IFileSystemModificationActivity} to handle
   * @return <code>true</code> if the activity should be send to the user, <code>false</code>
   *     otherwise
   */
  /*
   * TODO This method needs more information, e.g we need to add resources to
   * the mapper on the receiving side even if there was an I/= error. The
   * current logic will abort as soon as possible, which might result in an
   * inconsistent state that cannot be resolved.
   */
  /*
   * TODO move to ProjectMapper
   */
  /*
   * FIXME While it is nice to have this logic here, it should be done in the
   * component that handles these activities. Drawback would be that we have
   * to call addSharedResources which would result in broadcasts.
   */

  private boolean updatePartialSharedResources(final IFileSystemModificationActivity activity) {

    final IProject project = activity.getPath().getProject();

    /*
     * The follow 'if check' assumes that move operations where at least one
     * project is not part of the sharing is announced as create and delete
     * activities.
     */

    if (!projectMapper.isPartiallyShared(project)) return true;

    if (activity instanceof FileActivity) {
      FileActivity fileActivity = ((FileActivity) activity);
      IFile file = fileActivity.getPath().getFile();

      switch (fileActivity.getType()) {
        case CREATED:
          if (!isShared(file.getParent())) {
            log.error(
                "PSFIC -"
                    + " unable to update partial sharing state"
                    + ", parent is not shared for file: "
                    + file);

            return false;
          }

          if (!file.exists()) {
            log.error(
                "PSFIC -"
                    + " unable to update partial sharing state"
                    + ", file does not exist: "
                    + file);
            return false;
          }

          projectMapper.addResources(project, Collections.singletonList(file));

          break;

        case REMOVED:
          if (!isShared(file)) {
            log.error("PSFIR -" + " file removal detected for a non shared file: " + file);
            return false;
          }

          if (file.exists()) {
            log.error(
                "PSFIR -"
                    + " unable to update partial sharing state"
                    + ", file still exists: "
                    + file);
            return false;
          }

          projectMapper.removeResources(project, Collections.singletonList(file));

          break;

        case MOVED:
          IFile oldFile = fileActivity.getOldPath().getFile();

          if (!isShared(oldFile)) {
            log.error(
                "PSFIM -"
                    + " file move detected for a non shared file, source file is not shared, src: "
                    + oldFile
                    + " , dest: "
                    + file);
            return false;
          }

          if (oldFile.exists()) {
            log.error(
                "PSFIM -"
                    + " unable to update partial sharing state"
                    + ", source file still exist: "
                    + oldFile);
            return false;
          }

          if (isShared(file)) {
            log.error(
                "PSFIM -"
                    + " file move detected for shared file, destination file already shared, src: "
                    + oldFile
                    + " , dest: "
                    + file);
            return false;
          }

          if (!file.exists()) {
            log.error(
                "PSFIM -"
                    + " unable to update partial sharing state"
                    + ", destination file does not exist: "
                    + file);
            return false;
          }

          projectMapper.removeAndAddResources(
              project, Collections.singletonList(oldFile), Collections.singletonList(file));

          break;
      }
    } else if (activity instanceof FolderCreatedActivity) {
      IFolder folder = activity.getPath().getFolder();

      if (!isShared(folder.getParent())) {
        log.error("PSFOC -" + " folder creation detected for a non shared parent: " + folder);
        return false;
      }

      if (!folder.exists()) {
        log.error(
            "PSFOC - unable to update partial sharing state"
                + ", folder does not exist: "
                + folder);
        return false;
      }

      projectMapper.addResources(project, Collections.singletonList(folder));

    } else if (activity instanceof FolderDeletedActivity) {
      IFolder folder = activity.getPath().getFolder();

      if (!isShared(folder)) {
        log.error("PSFOR -" + " folder removal detected for a non shared folder: " + folder);
        return false;
      }

      if (folder.exists()) {
        log.error(
            "PSFOR -"
                + " unable to update partial sharing state"
                + ", folder still exists: "
                + folder);
        return false;
      }

      projectMapper.removeResources(project, Collections.singletonList(folder));
    }

    return true;
  }

  @Override
  public void addActivityProducer(IActivityProducer producer) {
    if (activityProducers.addIfAbsent(producer)) producer.addActivityListener(activityListener);
  }

  @Override
  public void removeActivityProducer(IActivityProducer producer) {
    if (activityProducers.remove(producer)) producer.removeActivityListener(activityListener);
  }

  @Override
  public void addActivityConsumer(IActivityConsumer consumer, Priority priority) {

    removeActivityConsumer(consumer);

    switch (priority) {
      case ACTIVE:
        activeActivityConsumers.add(consumer);
        break;
      case PASSIVE:
        passiveActivityConsumers.add(consumer);
        break;
    }
  }

  @Override
  public void removeActivityConsumer(IActivityConsumer consumer) {
    activeActivityConsumers.remove(consumer);
    passiveActivityConsumers.remove(consumer);
  }

  @Override
  public boolean isShared(IResource resource) {
    return projectMapper.isShared(resource);
  }

  @Override
  public List<IResource> getSharedResources() {
    return projectMapper.getPartiallySharedResources();
  }

  @Override
  public String getProjectID(IProject project) {
    return projectMapper.getID(project);
  }

  @Override
  public IProject getProject(String projectID) {
    return projectMapper.getProject(projectID);
  }

  @Override
  public Map<IProject, List<IResource>> getProjectResourcesMapping() {
    return projectMapper.getProjectResourceMapping();
  }

  @Override
  public List<IResource> getSharedResources(IProject project) {
    return projectMapper.getProjectResourceMapping().get(project);
  }

  @Override
  public boolean isCompletelyShared(IProject project) {
    return projectMapper.isCompletelyShared(project);
  }

  @Override
  public void addProjectMapping(String projectID, IProject project) {
    if (projectMapper.getProject(projectID) == null) {
      projectMapper.addProject(projectID, project, true);
      listenerDispatch.projectAdded(project);
    }
  }

  @Override
  public void removeProjectMapping(String projectID, IProject project) {
    if (projectMapper.getProject(projectID) != null) {
      projectMapper.removeProject(projectID);
      listenerDispatch.projectRemoved(project);
    }
  }

  @Override
  public <T> T getComponent(Class<T> key) {
    /*
     * Ensure that we return null when the session is about to start or stop
     * because the MutablePicoContainer#start/stop/dispose method is
     * synchronized and may cause a deadlock if the method is called from
     * the UI thread while a component may call #syncExec inside the start
     * or stop methods.
     */
    synchronized (componentAccessLock) {
      if (starting || stopping || stopped) return null;

      return sessionContainer.getComponent(key);
    }
  }

  @Override
  public StopManager getStopManager() {
    return stopManager;
  }

  @Override
  public void changeColor(int colorID) {
    changeColorManager.changeColorID(colorID);
  }

  @Override
  public Set<Integer> getUnavailableColors() {
    return changeColorManager.getUsedColorIDs();
  }

  @Override
  public void enableQueuing(IProject project) {
    activityQueuer.enableQueuing(project);
  }

  @Override
  public void disableQueuing(IProject project) {
    activityQueuer.disableQueuing(project);
    // send us a dummy activity to ensure the queues get flushed
    sendActivity(Collections.singletonList(localUser), new NOPActivity(localUser, localUser, 0));
  }

  private SarosSession(
      final String id,
      IContainerContext context,
      IPreferenceStore localProperties,
      JID host,
      IPreferenceStore hostProperties) {

    context.initComponent(this);

    this.sessionID = id;
    this.projectMapper = new SharedProjectMapper();
    this.activityQueuer = new ActivityQueuer();
    this.containerContext = context;

    // FIXME that should be passed in !
    JID localUserJID = connectionService.getJID();

    assert localUserJID != null;

    int localColorID = localProperties.getInt(ColorNegotiationHook.KEY_INITIAL_COLOR);
    int localFavoriteColorID = localProperties.getInt(ColorNegotiationHook.KEY_FAV_COLOR);
    localUser = new User(localUserJID, host == null, true, localColorID, localFavoriteColorID);

    localUser.setInSession(true);

    if (host == null) {
      hostUser = localUser;
      participants.put(hostUser.getJID(), hostUser);
      userProperties.put(hostUser, localProperties);
    } else {
      int hostColorID = hostProperties.getInt(ColorNegotiationHook.KEY_INITIAL_COLOR);
      int hostFavoriteColorID = hostProperties.getInt(ColorNegotiationHook.KEY_FAV_COLOR);
      hostUser = new User(host, true, false, hostColorID, hostFavoriteColorID);
      hostUser.setInSession(true);
      participants.put(hostUser.getJID(), hostUser);
      participants.put(localUser.getJID(), localUser);
      userProperties.put(hostUser, hostProperties);
      userProperties.put(localUser, localProperties);
    }

    sessionContainer = context.createChildContainer();
    sessionContainer.addComponent(ISarosSession.class, this);
    sessionContainer.addComponent(IActivityHandlerCallback.class, activityCallback);

    ISarosSessionContextFactory factory = context.getComponent(ISarosSessionContextFactory.class);
    factory.createComponents(this, sessionContainer);

    // Force the creation of the components added to the session container.
    sessionContainer.getComponents();

    concurrentDocumentServer = sessionContainer.getComponent(ConcurrentDocumentServer.class);

    concurrentDocumentClient = sessionContainer.getComponent(ConcurrentDocumentClient.class);

    activityHandler = sessionContainer.getComponent(ActivityHandler.class);

    stopManager = sessionContainer.getComponent(StopManager.class);

    changeColorManager = sessionContainer.getComponent(ChangeColorManager.class);

    permissionManager = sessionContainer.getComponent(PermissionManager.class);

    activitySequencer = sessionContainer.getComponent(ActivitySequencer.class);

    userListHandler = sessionContainer.getComponent(UserInformationHandler.class);

    // ensure that the container uses caching
    assert sessionContainer.getComponent(ActivityHandler.class)
            == sessionContainer.getComponent(ActivityHandler.class)
        : "container is wrongly configured - no cache support";
  }

  /**
   * This method is only meant to be used by unit tests to verify the cleanup of activity producers
   * and consumers.
   *
   * @return the size of the internal activity producer collection
   */
  boolean hasActivityProducers() {
    return !activityProducers.isEmpty();
  }

  /**
   * This method is only meant to be used by unit tests to verify the cleanup of activity producers
   * and consumers.
   *
   * @return the size of the internal activity consumer collection
   */
  boolean hasActivityConsumers() {
    return !activeActivityConsumers.isEmpty() || !passiveActivityConsumers.isEmpty();
  }
}
