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
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.net.IConnectionManager;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.preferences.IPreferenceStore;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.repackaged.picocontainer.PicoContainer;
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

  /* Application Context Dependencies Start*/

  private final UISynchronizer synchronizer;

  private final ITransmitter transmitter;

  private final IConnectionManager connectionManager;

  /* Application Context Dependencies End*/

  private final IContainerContext containerContext;

  private final ConcurrentDocumentClient concurrentDocumentClient;

  private final ActivityHandler activityHandler;

  private final CopyOnWriteArrayList<IActivityProducer> activityProducers =
      new CopyOnWriteArrayList<IActivityProducer>();

  private final Set<IReferencePoint> filteredReferencePoints = new CopyOnWriteArraySet<>();

  private final List<IActivityConsumer> activeActivityConsumers =
      new CopyOnWriteArrayList<IActivityConsumer>();

  private final List<IActivityConsumer> passiveActivityConsumers =
      new CopyOnWriteArrayList<IActivityConsumer>();

  /* Instance fields */
  private final User localUser;

  private final ConcurrentHashMap<JID, User> participants = new ConcurrentHashMap<JID, User>();

  private final SessionListenerDispatch listenerDispatch = new SessionListenerDispatch();

  private final User hostUser;

  private final SharedReferencePointMapper referencePointMapper;

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
          // Filters out resource activities for reference points whose activity execution is
          // disabled
          if (activity instanceof IResourceActivity) {
            IResource resource = ((IResourceActivity<? extends IResource>) activity).getResource();

            if (resource != null
                && filteredReferencePoints.contains(resource.getReferencePoint())) {
              log.debug("Dropped activity for resource of filtered reference point: " + activity);

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
      final String id,
      JID localUserJID,
      IPreferenceStore properties,
      IContainerContext containerContext) {
    this(id, containerContext, properties, localUserJID, /* unused */ null, /* unused */ null);
  }

  /** Constructor for client. */
  public SarosSession(
      final String id,
      JID localUserJID,
      JID hostJID,
      IPreferenceStore localProperties,
      IPreferenceStore hostProperties,
      IContainerContext containerContext) {
    this(id, containerContext, localProperties, localUserJID, hostJID, hostProperties);
  }

  @Override
  public void addSharedReferencePoint(IReferencePoint referencePoint, String referencePointId) {
    if (!referencePointMapper.isShared(referencePoint)) {
      referencePointMapper.addReferencePoint(referencePointId, referencePoint);

      listenerDispatch.referencePointAdded(referencePoint);
    }

    listenerDispatch.resourcesAdded(referencePoint);
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
  public boolean userHasReferencePoint(User user, IReferencePoint referencePoint) {
    return referencePointMapper.userHasReferencePoint(user, referencePoint);
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

    log.info(
        "user " + user + " started queuing reference points and can receive IResourceActivities");

    if (isHost()) {
      /*
       * Notify the system that the user's client now knows about all
       * currently shared reference points and can handle (process or queue)
       * activities related to them.
       *
       * Only the host needs this information because non-hosts don't have
       * to decide whom to send activities to - they just send them to the
       * host, who decides for them.
       */
      referencePointMapper.addMissingReferencePointsToUser(user);
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
  public void userFinishedResourceNegotiation(final User user) {

    log.info("user " + user + " now has reference points and can process IResourceActivities");

    synchronizer.syncExec(
        ThreadUtils.wrapSafe(
            log,
            new Runnable() {
              @Override
              public void run() {
                listenerDispatch.userFinishedResourceNegotiation(user);
              }
            }));

    if (isHost()) {

      JID jid = user.getJID();
      /**
       * This informs all participants, that a user is now able to process IResourceActivities.
       * After receiving this message the participants will send their awareness information.
       */
      userListHandler.sendUserFinishedResourceNegotiation(getRemoteUsers(), jid);
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

    referencePointMapper.userLeft(user);

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
  public void setActivityExecution(IReferencePoint referencePoint, boolean enabled) {
    if (referencePoint != null) {
      if (!enabled) {
        filteredReferencePoints.add(referencePoint);
      } else {
        filteredReferencePoints.remove(referencePoint);
      }
    }
  }

  @Override
  public Set<IReferencePoint> getReferencePoints() {
    return referencePointMapper.getReferencePoints();
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

    // If we don't have any shared reference points don't send ResourceActivities
    if (referencePointMapper.size() == 0 && (activity instanceof IResourceActivity)) {
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
    return referencePointMapper.isShared(resource);
  }

  @Override
  public String getReferencePointId(IReferencePoint referencePoint) {
    return referencePointMapper.getID(referencePoint);
  }

  @Override
  public IReferencePoint getReferencePoint(String referencePointID) {
    return referencePointMapper.getReferencePoint(referencePointID);
  }

  @Override
  public void addReferencePointMapping(String referencePointId, IReferencePoint referencePoint) {
    if (referencePointMapper.getReferencePoint(referencePointId) == null) {
      referencePointMapper.addReferencePoint(referencePointId, referencePoint);
      listenerDispatch.referencePointAdded(referencePoint);
    }
  }

  @Override
  public void removeReferencePointMapping(String referencePointId, IReferencePoint referencePoint) {
    if (referencePointMapper.getReferencePoint(referencePointId) != null) {
      referencePointMapper.removeReferencePoint(referencePointId);
      listenerDispatch.referencePointRemoved(referencePoint);
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
  public void enableQueuing(IReferencePoint referencePoint) {
    activityQueuer.enableQueuing(referencePoint);
  }

  @Override
  public void disableQueuing(IReferencePoint referencePoint) {
    activityQueuer.disableQueuing(referencePoint);
    // send us a dummy activity to ensure the queues get flushed
    sendActivity(Collections.singletonList(localUser), new NOPActivity(localUser, localUser, 0));
  }

  private SarosSession(
      final String id,
      IContainerContext context,
      IPreferenceStore localProperties,
      JID localUserJID,
      JID host,
      IPreferenceStore hostProperties) {

    this.sessionID = id;
    this.referencePointMapper = new SharedReferencePointMapper();
    this.activityQueuer = new ActivityQueuer();
    this.containerContext = context;

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

    if (factory == null) {
      throw new IllegalStateException(
          "component of class type "
              + ISarosSessionContextFactory.class.getName()
              + " could not be found in the current global application context but is required for"
              + " operation");
    }

    factory.createComponents(this, sessionContainer);

    // Force the creation of the components added to the session container.
    sessionContainer.getComponents();

    // Obtained from Application context START
    synchronizer = getComponent(sessionContainer, UISynchronizer.class);
    transmitter = getComponent(sessionContainer, ITransmitter.class);
    connectionManager = getComponent(sessionContainer, IConnectionManager.class);
    // Obtained from Application context END

    // Obtained from Session context START
    concurrentDocumentClient = getComponent(sessionContainer, ConcurrentDocumentClient.class);
    activityHandler = getComponent(sessionContainer, ActivityHandler.class);
    stopManager = getComponent(sessionContainer, StopManager.class);
    changeColorManager = getComponent(sessionContainer, ChangeColorManager.class);
    permissionManager = getComponent(sessionContainer, PermissionManager.class);
    activitySequencer = getComponent(sessionContainer, ActivitySequencer.class);
    userListHandler = getComponent(sessionContainer, UserInformationHandler.class);
    // Obtained from Session context END

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

  private static <T> T getComponent(final PicoContainer container, final Class<T> componentType) {
    final T result = container.getComponent(componentType);

    if (result == null)
      throw new IllegalStateException(
          "component of class type "
              + componentType.getName()
              + " could not be found in the current session context or application context but is"
              + " required for operation");

    return result;
  }
}
