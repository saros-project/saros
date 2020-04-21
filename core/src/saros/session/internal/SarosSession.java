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
package saros.session.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import saros.activities.IActivity;
import saros.activities.IResourceActivity;
import saros.activities.NOPActivity;
import saros.communication.extensions.KickUserExtension;
import saros.communication.extensions.LeaveSessionExtension;
import saros.concurrent.management.ConcurrentDocumentClient;
import saros.context.IContainerContext;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.net.IConnectionManager;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.preferences.IPreferenceStore;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.IActivityConsumer;
import saros.session.IActivityConsumer.Priority;
import saros.session.IActivityHandlerCallback;
import saros.session.IActivityListener;
import saros.session.IActivityProducer;
import saros.session.ISarosSession;
import saros.session.ISarosSessionContextFactory;
import saros.session.ISessionListener;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.session.User.Permission;
import saros.synchronize.StopManager;
import saros.synchronize.UISynchronizer;
import saros.util.StackTrace;
import saros.util.ThreadUtils;

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

  private final ActivityHandler activityHandler;

  private final CopyOnWriteArrayList<IActivityProducer> activityProducers =
      new CopyOnWriteArrayList<IActivityProducer>();

  private final Set<IProject> filteredProjects = new CopyOnWriteArraySet<>();

  private final List<IActivityConsumer> activeActivityConsumers =
      new CopyOnWriteArrayList<IActivityConsumer>();
  private final List<IActivityConsumer> passiveActivityConsumers =
      new CopyOnWriteArrayList<IActivityConsumer>();

  /* Instance fields */
  private final User localUser;

  private final ConcurrentHashMap<JID, User> participants = new ConcurrentHashMap<JID, User>();

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
          // Filters out resource activities for projects whose activity execution is disabled
          if (activity instanceof IResourceActivity) {
            IResource resource = ((IResourceActivity) activity).getResource();

            if (resource != null && filteredProjects.contains(resource.getProject())) {
              log.debug("Dropped activity for resource of filtered project: " + activity);

              return;
            }
          }

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
  public void addSharedProject(IProject project, String projectID) {
    if (!projectMapper.isShared(project)) {
      projectMapper.addProject(projectID, project);

      listenerDispatch.projectAdded(project);
    }

    listenerDispatch.resourcesAdded(project);
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
  public void addUser(final User user) {

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
  public void setActivityExecution(IProject project, boolean enabled) {
    if (project != null) {
      if (!enabled) {
        filteredProjects.add(project);
      } else {
        filteredProjects.remove(project);
      }
    }
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

    try {
      activitySequencer.sendActivity(recipients, activity);
    } catch (IllegalArgumentException e) {
      log.warn("could not serialize activity: " + activity, e);
    }
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
  public String getProjectID(IProject project) {
    return projectMapper.getID(project);
  }

  @Override
  public IProject getProject(String projectID) {
    return projectMapper.getProject(projectID);
  }

  @Override
  public void addProjectMapping(String projectID, IProject project) {
    if (projectMapper.getProject(projectID) == null) {
      projectMapper.addProject(projectID, project);
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

    localUser = new User(localUserJID, host == null, true, localProperties);
    localUser.setInSession(true);

    if (host == null) {
      hostUser = localUser;
      participants.put(hostUser.getJID(), hostUser);
    } else {
      hostUser = new User(host, true, false, hostProperties);
      hostUser.setInSession(true);
      participants.put(hostUser.getJID(), hostUser);
      participants.put(localUser.getJID(), localUser);
    }

    sessionContainer = context.createChildContainer();
    sessionContainer.addComponent(ISarosSession.class, this);
    sessionContainer.addComponent(IActivityHandlerCallback.class, activityCallback);

    ISarosSessionContextFactory factory = context.getComponent(ISarosSessionContextFactory.class);
    factory.createComponents(this, sessionContainer);

    // Force the creation of the components added to the session container.
    sessionContainer.getComponents();

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
