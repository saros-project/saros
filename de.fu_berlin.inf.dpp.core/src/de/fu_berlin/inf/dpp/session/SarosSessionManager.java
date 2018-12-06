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
package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.context.IContainerContext;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.AbstractIncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.negotiation.AbstractOutgoingProjectNegotiation;
import de.fu_berlin.inf.dpp.negotiation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.negotiation.NegotiationFactory;
import de.fu_berlin.inf.dpp.negotiation.NegotiationListener;
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelOption;
import de.fu_berlin.inf.dpp.negotiation.OutgoingSessionNegotiation;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiationCollector;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiationData;
import de.fu_berlin.inf.dpp.negotiation.SessionNegotiation;
import de.fu_berlin.inf.dpp.negotiation.TransferType;
import de.fu_berlin.inf.dpp.negotiation.hooks.ISessionNegotiationHook;
import de.fu_berlin.inf.dpp.negotiation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.preferences.PreferenceStore;
import de.fu_berlin.inf.dpp.session.internal.SarosSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;

/**
 * The SessionManager is responsible for initiating new Saros sessions and for reacting to
 * invitations. The user can be only part of one session at most.
 *
 * @author rdjemili
 */
@Component(module = "core")
public class SarosSessionManager implements ISarosSessionManager {

  /**
   * @JTourBusStop 6, Architecture Overview, Invitation Management:
   *
   * <p>While Activities are used to keep a running session consistent, we use MESSAGES whenever the
   * Session itself is modified. This means adding users or projects to the session.
   *
   * <p>The Invitation Process is managed by the "Invitation Management"-Component. This class is
   * the main entrance point of this Component. During the invitation Process, the Network Layer is
   * used to send MESSAGES between the host and the invitees and the Session Management is informed
   * about joined users and added projects.
   *
   * <p>For more information about the Invitation Process see the "Invitation Process"-Tour.
   */
  private static final Logger log = Logger.getLogger(SarosSessionManager.class.getName());

  private static final int SESSION_STATE_STOPPED = 0;

  private static final int SESSION_STATE_STARTING = 1;

  private static final int SESSION_STATE_RUNNING = 2;

  private static final int SESSION_STATE_STOPPING = 3;

  private static final Random ID_GENERATOR = new Random();

  private static final long NEGOTIATION_TIMEOUT = 10000L;

  private volatile SarosSession session;

  private final IContainerContext context;

  private final NegotiationFactory negotiationFactory;

  private final NegotiationPacketListener negotiationPacketLister;

  private final SessionNegotiationHookManager hookManager;

  private final SessionNegotiationObservable currentSessionNegotiations;

  private final ProjectNegotiationObservable currentProjectNegotiations;

  private final ProjectNegotiationCollector nextProjectNegotiation =
      new ProjectNegotiationCollector();

  private XMPPConnectionService connectionService;

  private final List<ISessionLifecycleListener> sessionLifecycleListeners =
      new CopyOnWriteArrayList<ISessionLifecycleListener>();

  private volatile INegotiationHandler negotiationHandler;

  private final ExecutorService worker = Executors.newSingleThreadExecutor();

  private final Lock sessionStateLock = new ReentrantLock();

  private int sessionState = SESSION_STATE_STOPPED;

  private final NegotiationListener negotiationListener =
      new NegotiationListener() {
        @Override
        public void negotiationTerminated(final SessionNegotiation negotiation) {
          currentSessionNegotiations.remove(negotiation);
        }

        @Override
        public void negotiationTerminated(final ProjectNegotiation negotiation) {
          currentProjectNegotiations.remove(negotiation);

          triggerResourceNegotiation();
        }
      };

  private final IConnectionListener connectionListener =
      new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection, ConnectionState state) {

          if (state == ConnectionState.DISCONNECTING) {
            // TODO run async and not block SMACK
            stopSession(SessionEndReason.CONNECTION_LOST);
          }
        }
      };

  public SarosSessionManager(
      IContainerContext context,
      NegotiationFactory negotiationFactory,
      SessionNegotiationHookManager hookManager,
      XMPPConnectionService connectionService,
      ITransmitter transmitter,
      IReceiver receiver) {

    this.context = context;
    this.connectionService = connectionService;
    this.currentSessionNegotiations = new SessionNegotiationObservable();
    this.currentProjectNegotiations = new ProjectNegotiationObservable();
    this.connectionService.addListener(connectionListener);

    this.negotiationFactory = negotiationFactory;
    this.hookManager = hookManager;

    this.negotiationPacketLister =
        new NegotiationPacketListener(
            this, currentSessionNegotiations, currentProjectNegotiations, transmitter, receiver);
  }

  @Override
  public void setNegotiationHandler(INegotiationHandler handler) {
    negotiationHandler = handler;
  }

  /**
   * @JTourBusStop 3, Invitation Process:
   *
   * <p>This class manages the current Saros session.
   *
   * <p>Saros makes a distinction between a session and a shared project. A session is an on-line
   * collaboration between users which allows users to carry out activities. The main activity is to
   * share projects. Hence, before you share a project, a session has to be started and all users
   * added to it.
   *
   * <p>(At the moment, this separation is invisible to the user. He/she must share a project in
   * order to start a session.)
   */
  @Override
  public void startSession(final Map<IProject, List<IResource>> projectResourcesMapping) {

    final Future<?> future;

    sessionStateLock.lock();
    try {
      if (sessionState != SESSION_STATE_STOPPED) return;

      sessionState = SESSION_STATE_STARTING;

      future = worker.submit(() -> internalStartSession(projectResourcesMapping));

    } finally {
      sessionStateLock.unlock();
    }

    try {
      future.get();
    } catch (InterruptedException e) {
      log.warn("interrupted while waiting for session start", e);
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      log.error("unable to start a session", e);
    }
  }

  private void internalStartSession(final Map<IProject, List<IResource>> resources) {

    assert sessionState == SESSION_STATE_STARTING;

    final String sessionID = String.valueOf(ID_GENERATOR.nextInt(Integer.MAX_VALUE));

    negotiationPacketLister.setRejectSessionNegotiationRequests(true);

    IPreferenceStore hostProperties = new PreferenceStore();

    if (hookManager != null) {
      for (ISessionNegotiationHook hook : hookManager.getHooks()) {
        hook.setInitialHostPreferences(hostProperties);
      }
    }

    session = new SarosSession(sessionID, hostProperties, context);

    sessionStarting(session);
    session.start();

    sessionStarted(session);

    for (Entry<IProject, List<IResource>> entry : resources.entrySet()) {

      final IProject project = entry.getKey();
      final List<IResource> resourcesList = entry.getValue();

      String projectID = String.valueOf(ID_GENERATOR.nextInt(Integer.MAX_VALUE));

      session.addSharedResources(project, projectID, resourcesList);
    }

    log.info("session started");

    sessionStateLock.lock();
    try {
      sessionState = SESSION_STATE_RUNNING;
    } finally {
      sessionStateLock.unlock();
    }
  }

  // FIXME offer a startSession method for the client and host !
  @Override
  public ISarosSession joinSession(
      String id, JID host, IPreferenceStore hostProperties, IPreferenceStore localProperties) {

    assert session == null;

    session = new SarosSession(id, host, localProperties, hostProperties, context);

    log.info("joined uninitialized Saros session");

    // FIXME this is not true !
    sessionState = SESSION_STATE_RUNNING;

    return session;
  }

  @Override
  public void stopSession(final SessionEndReason reason) {

    final Future<?> future;

    sessionStateLock.lock();
    try {

      if (sessionState != SESSION_STATE_RUNNING) {

        // see IncomingSessionNegotiation , we have to execute this
        negotiationPacketLister.setRejectSessionNegotiationRequests(false);
        return;
      }

      sessionState = SESSION_STATE_STOPPING;
      future = worker.submit(() -> internalStopSession(reason));

    } finally {
      sessionStateLock.unlock();
    }

    try {
      future.get();
    } catch (InterruptedException e) {
      log.warn("interrupted while waiting for session stop", e);
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      log.error("unable to stop the current session", e);
    }
  }

  private void internalStopSession(final SessionEndReason reason) {

    assert sessionState == SESSION_STATE_STOPPING;

    log.debug("terminating all running negotiations");

    if (!terminateNegotiations()) log.warn("there are still running negotiations");

    sessionEnding(session);

    try {
      session.stop(reason);
      log.info("stopped session: " + session.getID());
    } catch (RuntimeException e) {
      log.error("failed to stop the session", e);
    }

    /*
     * FIXME check the behavior if getSession should already return null at
     * this point
     */

    final ISarosSession currentSession = session;
    session = null;

    sessionEnded(currentSession, reason);

    negotiationPacketLister.setRejectSessionNegotiationRequests(false);

    sessionStateLock.lock();
    try {
      sessionState = SESSION_STATE_STOPPED;
    } finally {
      sessionStateLock.unlock();
    }
  }

  /**
   * This method and the sarosSessionObservable are dangerous to use. The session might be in the
   * process of being destroyed while you call this method. The caller needs to save the returned
   * value to a local variable and do a null check. For new code you should consider being scoped by
   * the SarosSession and get the SarosSession in the constructor.
   *
   * @deprecated Error prone method, which produces NPE if not handled correctly. Will soon get
   *     removed.
   */
  @Override
  @Deprecated
  public ISarosSession getSession() {
    return session;
  }

  void sessionNegotiationRequestReceived(
      final JID remoteAddress,
      final String sessionID,
      final String negotiationID,
      final String version,
      final String description) {

    sessionStateLock.lock();
    try {

      if (sessionState != SESSION_STATE_STOPPED) return;

      if (negotiationPacketLister.isRejectingSessionNegotiationsRequests()) {
        log.error("could not accept invitation because there is already a pending invitation");
        return;
      }

      negotiationPacketLister.setRejectSessionNegotiationRequests(true);

      worker.submit(
          () ->
              acceptSessionNegotiationRequest(
                  remoteAddress, sessionID, negotiationID, version, description));

    } finally {
      sessionStateLock.unlock();
    }
  }

  private void acceptSessionNegotiationRequest(
      final JID remoteAddress,
      final String sessionID,
      final String negotiationID,
      final String version,
      final String description) {

    final INegotiationHandler handler = negotiationHandler;

    if (handler == null) {
      log.warn("could not accept invitation because no handler is installed");
      return;
    }

    final IncomingSessionNegotiation negotiation;

    negotiation =
        negotiationFactory.newIncomingSessionNegotiation(
            remoteAddress, negotiationID, sessionID, version, this, description);

    negotiation.setNegotiationListener(negotiationListener);
    currentSessionNegotiations.add(negotiation);

    handler.handleIncomingSessionNegotiation(negotiation);
  }

  void projectNegotiationRequestReceived(
      final JID remoteAddress,
      final TransferType transferType,
      final List<ProjectNegotiationData> projectNegotiationData,
      final String negotiationID) {

    sessionStateLock.lock();
    try {

      if (sessionState != SESSION_STATE_RUNNING) return;

      worker.submit(
          () ->
              acceptProjectNegotiationRequest(
                  remoteAddress, transferType, projectNegotiationData, negotiationID));

    } finally {
      sessionStateLock.unlock();
    }
  }

  private void acceptProjectNegotiationRequest(
      final JID remoteAddress,
      final TransferType transferType,
      final List<ProjectNegotiationData> projectNegotiationData,
      final String negotiationID) {

    final INegotiationHandler handler = negotiationHandler;

    if (handler == null) {
      log.warn("could not accept project negotiation because no handler is installed");
      return;
    }

    final AbstractIncomingProjectNegotiation negotiation;

    negotiation =
        negotiationFactory.newIncomingProjectNegotiation(
            remoteAddress, transferType, negotiationID, projectNegotiationData, this, session);

    negotiation.setNegotiationListener(negotiationListener);
    currentProjectNegotiations.add(negotiation);

    handler.handleIncomingProjectNegotiation(negotiation);
  }

  @Override
  public void invite(final JID toInvite, final String description) {

    sessionStateLock.lock();
    try {

      if (sessionState != SESSION_STATE_RUNNING) return;

      worker.submit(() -> inviteInternal(toInvite, description));

    } finally {
      sessionStateLock.unlock();
    }
  }

  private void inviteInternal(final JID user, final String description) {

    final INegotiationHandler handler = negotiationHandler;
    final ISarosSession currentSession = session;

    assert currentSession != null;

    if (handler == null) {
      log.warn("could not start an invitation because no handler is installed");
      return;
    }

    final OutgoingSessionNegotiation negotiation;

    /*
     * this assumes that a user is added to the session before the
     * negotiation terminates !
     */
    if (session.getResourceQualifiedJID(user) != null) return;

    if (currentSessionNegotiations.exists(user)) return;

    negotiation =
        negotiationFactory.newOutgoingSessionNegotiation(user, this, session, description);

    negotiation.setNegotiationListener(negotiationListener);
    currentSessionNegotiations.add(negotiation);

    handler.handleOutgoingSessionNegotiation(negotiation);
  }

  @Override
  public void invite(Collection<JID> jidsToInvite, String description) {
    for (JID jid : jidsToInvite) invite(jid, description);
  }

  /**
   * Adds project resources to an existing session.
   *
   * @param projectResourcesMapping
   */
  @Override
  public void addResourcesToSession(Map<IProject, List<IResource>> projectResourcesMapping) {

    if (projectResourcesMapping == null) return;

    /*
     * To prevent multiple concurrent negotiations per user. 1. Collect all
     * new mappings, 2. If active negotiations are running wait till they
     * finish (collect new mappings in the meantime), 3. Create one
     * negotiation with all collected resources.
     */

    nextProjectNegotiation.add(projectResourcesMapping);
    triggerResourceNegotiation();
  }

  /**
   * This method handles new project negotiations for already invited users (not the first in the
   * process of inviting to the session).
   */
  private void startCollectedResourcesNegotiation() {

    final Map<IProject, List<IResource>> resourceMapping = nextProjectNegotiation.get();

    final ISarosSession currentSession = session;

    assert currentSession != null;

    if (resourceMapping.isEmpty()) return;

    /*
     * TODO: there are race conditions, USER A restricts USER B to read-only
     * while this code is executed
     */

    // FIXME non host sharing is disabled, this logic makes no sense
    if (!currentSession.hasWriteAccess()) {
      log.error(
          "current local user has not enough privileges to add resources to the current session");
      return;
    }

    final List<IProject> projectsToShare = new ArrayList<IProject>();

    for (Entry<IProject, List<IResource>> entry : resourceMapping.entrySet()) {

      final IProject project = entry.getKey();
      final List<IResource> resources = entry.getValue();

      // side effect: non shared projects are always partial -.-
      if (currentSession.isCompletelyShared(project)) continue;

      String projectID = currentSession.getProjectID(project);

      if (projectID == null) {
        projectID = String.valueOf(ID_GENERATOR.nextInt(Integer.MAX_VALUE));
      }

      currentSession.addSharedResources(project, projectID, resources);

      projectsToShare.add(project);
    }

    if (projectsToShare.isEmpty()) {
      log.warn(
          "skipping project negotiation because no new projects were added to the current session");
      return;
    }

    for (User user : currentSession.getRemoteUsers())
      startResourceNegotiation(user.getJID(), projectsToShare);
  }

  @Override
  public void startSharingProjects(final JID user) {

    sessionStateLock.lock();
    try {

      if (sessionState != SESSION_STATE_RUNNING) return;

      worker.submit(() -> startResourceNegotiation(user));

    } finally {
      sessionStateLock.unlock();
    }
  }

  private void startResourceNegotiation(final JID user) {

    ISarosSession currentSession = session;

    assert currentSession != null;

    final List<IProject> currentSharedProjects =
        new ArrayList<IProject>(currentSession.getProjects());

    if (currentSharedProjects.isEmpty()) return;

    startResourceNegotiation(user, currentSharedProjects);
  }

  private void startResourceNegotiation(final JID user, final List<IProject> projects) {

    ISarosSession currentSession = session;

    assert currentSession != null;

    final INegotiationHandler handler = negotiationHandler;

    if (handler == null) {
      log.warn("could not start a resource negotiation because no" + " handler is installed");
      return;
    }

    final AbstractOutgoingProjectNegotiation negotiation;

    final User remoteUser = currentSession.getUser(user);

    if (remoteUser == null) {
      log.warn(
          "could not start a resource negotiation because"
              + " the remote user "
              + user
              + " is not part of the current session");
      return;
    }

    final TransferType type =
        TransferType.valueOf(
            currentSession
                .getUserProperties(remoteUser)
                .getString(ProjectNegotiationTypeHook.KEY_TYPE));

    negotiation =
        negotiationFactory.newOutgoingProjectNegotiation(
            user, type, projects, this, currentSession);

    negotiation.setNegotiationListener(negotiationListener);
    currentProjectNegotiations.add(negotiation);

    handler.handleOutgoingProjectNegotiation(negotiation);
  }

  private void triggerResourceNegotiation() {

    sessionStateLock.lock();
    try {

      if (sessionState != SESSION_STATE_RUNNING) return;

      worker.submit(
          () -> {
            if (currentProjectNegotiations.isEmpty()) startCollectedResourcesNegotiation();
          });

    } finally {
      sessionStateLock.unlock();
    }
  }

  @Override
  public void addSessionLifecycleListener(ISessionLifecycleListener listener) {
    sessionLifecycleListeners.add(listener);
  }

  @Override
  public void removeSessionLifecycleListener(ISessionLifecycleListener listener) {
    sessionLifecycleListeners.remove(listener);
  }

  @Override
  public void postOutgoingInvitationCompleted(IProgressMonitor monitor, User user) {
    try {
      for (ISessionLifecycleListener listener : sessionLifecycleListeners) {
        listener.postOutgoingInvitationCompleted(session, user, monitor);
      }
    } catch (RuntimeException e) {
      log.error("Internal error in notifying listener" + " of an outgoing invitation: ", e);
    }
  }

  @Override
  public void sessionStarting(ISarosSession sarosSession) {
    try {
      for (ISessionLifecycleListener listener : sessionLifecycleListeners) {
        listener.sessionStarting(sarosSession);
      }
    } catch (RuntimeException e) {
      log.error("error in notifying listener of session starting: ", e);
    }
  }

  @Override
  public void sessionStarted(ISarosSession sarosSession) {
    for (ISessionLifecycleListener listener : sessionLifecycleListeners) {
      try {
        listener.sessionStarted(sarosSession);
      } catch (RuntimeException e) {
        log.error("error in notifying listener of session start: ", e);
      }
    }
  }

  private void sessionEnding(ISarosSession sarosSession) {
    for (ISessionLifecycleListener listener : sessionLifecycleListeners) {
      try {
        listener.sessionEnding(sarosSession);
      } catch (RuntimeException e) {
        log.error("error in notifying listener of session ending: ", e);
      }
    }
  }

  private void sessionEnded(ISarosSession sarosSession, SessionEndReason reason) {
    for (ISessionLifecycleListener listener : sessionLifecycleListeners) {
      try {
        listener.sessionEnded(sarosSession, reason);
      } catch (RuntimeException e) {
        log.error("error in notifying listener of session end: ", e);
      }
    }
  }

  private boolean terminateNegotiations() {

    for (SessionNegotiation negotiation : currentSessionNegotiations.list()) {
      negotiation.localCancel(null, CancelOption.NOTIFY_PEER);
    }

    for (ProjectNegotiation negotiation : currentProjectNegotiations.list())
      negotiation.localCancel(null, CancelOption.NOTIFY_PEER);

    log.trace("waiting for all session and project negotiations to terminate");

    long startTime = System.currentTimeMillis();

    boolean terminated = false;

    while (System.currentTimeMillis() - startTime < NEGOTIATION_TIMEOUT) {
      if (currentSessionNegotiations.list().isEmpty() && currentProjectNegotiations.isEmpty()) {
        terminated = true;
        break;
      }

      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }

    return terminated;
  }
}
