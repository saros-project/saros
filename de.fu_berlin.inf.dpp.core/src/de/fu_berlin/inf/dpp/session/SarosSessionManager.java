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
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
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
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
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

  private static final Random SESSION_ID_GENERATOR = new Random();

  private static final long LOCK_TIMEOUT = 10000L;

  private static final long NEGOTIATION_TIMEOUT = 10000L;
  private final IContainerContext context;
  private final NegotiationFactory negotiationFactory;
  private final NegotiationPacketListener negotiationPacketLister;
  private final SessionNegotiationHookManager hookManager;
  private final SessionNegotiationObservable currentSessionNegotiations;
  private final ProjectNegotiationObservable currentProjectNegotiations;
  private final ProjectNegotiationCollector nextProjectNegotiation =
      new ProjectNegotiationCollector();
  private final List<ISessionLifecycleListener> sessionLifecycleListeners =
      new CopyOnWriteArrayList<ISessionLifecycleListener>();
  private final Lock startStopSessionLock = new ReentrantLock();
  private final NegotiationListener negotiationListener =
      new NegotiationListener() {
        @Override
        public void negotiationTerminated(final SessionNegotiation negotiation) {
          currentSessionNegotiations.remove(negotiation);
        }

        @Override
        public void negotiationTerminated(final ProjectNegotiation negotiation) {
          currentProjectNegotiations.remove(negotiation);

          if (currentProjectNegotiations.isEmpty()) {
            synchronized (nextProjectNegotiation) {
              nextProjectNegotiation.notifyAll();
            }
          }
        }
      };
  private Thread nextProjectNegotiationWorker;
  private volatile SarosSession session;
  private XMPPConnectionService connectionService;
  private volatile boolean sessionStartup = false;
  private volatile boolean sessionShutdown = false;
  private final IConnectionListener connectionListener =
      new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection, ConnectionState state) {

          if (state == ConnectionState.DISCONNECTING) {
            stopSession(SessionEndReason.CONNECTION_LOST);
          }
        }
      };
  private volatile INegotiationHandler negotiationHandler;
  private IReferencePointManager referencePointManager;

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
  public void startSession(
      Map<IReferencePoint, List<IResource>> referencePointResources,
      IReferencePointManager referencePointManager) {
    /*
     * FIXME split the logic, start a session without anything and then add
     * resources !
     */
    try {
      if (!startStopSessionLock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
        log.warn(
            "could not start a new session because another operation still tries to start or stop a session");
        return;
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return;
    }

    try {

      if (sessionShutdown)
        throw new IllegalStateException(
            "cannot start the session from the same thread context that is currently about to stop the session: "
                + Thread.currentThread().getName());

      if (sessionStartup) {
        log.warn("recursive execution detected, ignoring session start request", new StackTrace());
        return;
      }

      if (session != null) {
        log.warn("could not start a new session because a session has already been started");
        return;
      }

      if (negotiationPacketLister.isRejectingSessionNegotiationsRequests()) {
        log.warn("cannot start a session while a session invitation is pending");
        return;
      }

      sessionStartup = true;

      final String sessionID = String.valueOf(SESSION_ID_GENERATOR.nextInt(Integer.MAX_VALUE));

      negotiationPacketLister.setRejectSessionNegotiationRequests(true);

      IPreferenceStore hostProperties = new PreferenceStore();
      if (hookManager != null) {
        for (ISessionNegotiationHook hook : hookManager.getHooks()) {
          hook.setInitialHostPreferences(hostProperties);
        }
      }

      session = new SarosSession(sessionID, hostProperties, context, referencePointManager);

      sessionStarting(session);
      session.start();
      sessionStarted(session);

      this.referencePointManager = session.getComponent(IReferencePointManager.class);

      for (Entry<IReferencePoint, List<IResource>> mapEntry : referencePointResources.entrySet()) {

        final IReferencePoint referencePoint = mapEntry.getKey();
        final List<IResource> resourcesList = mapEntry.getValue();

        String referencePointID = String.valueOf(SESSION_ID_GENERATOR.nextInt(Integer.MAX_VALUE));

        session.addSharedResources(referencePoint, referencePointID, resourcesList);
      }

      log.info("session started");
    } finally {
      sessionStartup = false;
      startStopSessionLock.unlock();
    }
  }

  // FIXME offer a startSession method for the client and host !
  @Override
  public ISarosSession joinSession(
      String id, JID host, IPreferenceStore hostProperties, IPreferenceStore localProperties) {

    assert session == null;

    session = new SarosSession(id, host, localProperties, hostProperties, context);

    log.info("joined uninitialized Saros session");

    return session;
  }

  /** @nonSWT */
  @Override
  public void stopSession(SessionEndReason reason) {

    try {
      if (!startStopSessionLock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
        log.warn(
            "could not stop the current session because another operation still tries to start or stop a session");
        return;
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return;
    }

    try {

      if (sessionStartup)
        throw new IllegalStateException(
            "cannot stop the session from the same thread context that is currently about to start the session: "
                + Thread.currentThread().getName());

      if (sessionShutdown) {
        log.warn("recursive execution detected, ignoring session stop request", new StackTrace());
        return;
      }

      if (session == null) return;

      sessionShutdown = true;

      log.debug("terminating all running negotiations");

      if (!terminateNegotiations()) log.warn("there are still running negotiations");

      sessionEnding(session);

      try {
        session.stop(reason);
        log.info("session stopped");
      } catch (RuntimeException e) {
        log.error("failed to stop the session", e);
      }

      /*
       * FIXME check the behavior if getSession should already return null
       * at this point
       */

      ISarosSession currentSession = session;
      session = null;

      sessionEnded(currentSession, reason);

    } finally {
      sessionShutdown = false;
      negotiationPacketLister.setRejectSessionNegotiationRequests(false);
      startStopSessionLock.unlock();
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
      JID remoteAddress,
      String sessionID,
      String negotiationID,
      String version,
      String description) {

    INegotiationHandler handler = negotiationHandler;

    if (handler == null) {
      log.warn("could not accept invitation because no handler is installed");
      return;
    }

    IncomingSessionNegotiation negotiation;

    synchronized (this) {
      if (!startStopSessionLock.tryLock()) {
        log.warn("could not accept invitation because the current session is about to stop");
        return;
      }

      try {

        // should not happen
        if (negotiationPacketLister.isRejectingSessionNegotiationsRequests()) {
          log.error("could not accept invitation because there is already a pending invitation");
          return;
        }

        negotiationPacketLister.setRejectSessionNegotiationRequests(true);

        negotiation =
            negotiationFactory.newIncomingSessionNegotiation(
                remoteAddress, negotiationID, sessionID, version, this, description);

        negotiation.setNegotiationListener(negotiationListener);
        currentSessionNegotiations.add(negotiation);

      } finally {
        startStopSessionLock.unlock();
      }
    }
    handler.handleIncomingSessionNegotiation(negotiation);
  }

  void projectNegotiationRequestReceived(
      JID remoteAddress,
      TransferType transferType,
      List<ProjectNegotiationData> projectNegotiationData,
      String negotiationID) {

    INegotiationHandler handler = negotiationHandler;

    if (handler == null) {
      log.warn("could not accept project negotiation because no handler is installed");
      return;
    }

    AbstractIncomingProjectNegotiation negotiation;

    synchronized (this) {
      if (!startStopSessionLock.tryLock()) {
        log.warn(
            "could not accept project negotiation because the current session is about to stop");
        return;
      }

      try {
        negotiation =
            negotiationFactory.newIncomingProjectNegotiation(
                remoteAddress, transferType, negotiationID, projectNegotiationData, this, session);

        negotiation.setNegotiationListener(negotiationListener);
        currentProjectNegotiations.add(negotiation);

      } finally {
        startStopSessionLock.unlock();
      }
    }
    handler.handleIncomingProjectNegotiation(negotiation);
  }

  @Override
  public void invite(JID toInvite, String description) {

    INegotiationHandler handler = negotiationHandler;

    if (handler == null) {
      log.warn("could not start an invitation because no handler is installed");
      return;
    }

    OutgoingSessionNegotiation negotiation;

    synchronized (this) {
      if (!startStopSessionLock.tryLock()) {
        log.warn(
            "could not start an invitation because the current session is about to start or stop");
        return;
      }

      try {

        if (session == null) return;

        /*
         * this assumes that a user is added to the session before the
         * negotiation terminates !
         */
        if (session.getResourceQualifiedJID(toInvite) != null) return;

        if (currentSessionNegotiations.exists(toInvite)) return;

        negotiation =
            negotiationFactory.newOutgoingSessionNegotiation(toInvite, this, session, description);

        negotiation.setNegotiationListener(negotiationListener);
        currentSessionNegotiations.add(negotiation);

      } finally {
        startStopSessionLock.unlock();
      }
    }
    handler.handleOutgoingSessionNegotiation(negotiation);
  }

  @Override
  public void invite(Collection<JID> jidsToInvite, String description) {
    for (JID jid : jidsToInvite) invite(jid, description);
  }

  /**
   * Adds reference point resources to an existing session.
   *
   * @param referencePointResourcesMapping
   */
  @Override
  public synchronized void addResourcesToSession(
      Map<IReferencePoint, List<IResource>> referencePointResourcesMapping) {
    if (referencePointResourcesMapping == null) {
      return;
    }

    /*
     * To prevent multiple concurrent negotiations per user. 1. Collect all
     * new mappings, 2. If active negotiations are running wait till they
     * finish (collect new mappings in the meantime), 3. Create one
     * negotiation with all collected resources.
     */

    nextProjectNegotiation.add(referencePointResourcesMapping);

    if (nextProjectNegotiationWorker != null && nextProjectNegotiationWorker.isAlive()) {
      return;
    } else if (currentProjectNegotiations.isEmpty()) {
      /* shortcut to direct handling */
      startNextProjectNegotiation();
      return;
    }

    /* else create a worker thread */
    Runnable worker =
        new Runnable() {
          @Override
          public void run() {
            synchronized (nextProjectNegotiation) {
              while (!currentProjectNegotiations.isEmpty()) {
                try {
                  nextProjectNegotiation.wait();
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  return;
                }
              }
            }

            startNextProjectNegotiation();
          }
        };
    nextProjectNegotiationWorker = ThreadUtils.runSafeAsync(log, worker);
  }

  /**
   * This method handles new project negotiations for already invited user (not the first in the
   * process of inviting to the session).
   */
  private synchronized void startNextProjectNegotiation() {
    ISarosSession currentSession = session;

    if (currentSession == null) {
      log.warn("could not add resources because there is no active session");
      return;
    }

    /*
     * TODO: there are race conditions, USER A restricts USER B to read-only
     * while this code is executed
     */

    if (!currentSession.hasWriteAccess()) {
      log.error(
          "current local user has not enough privileges to add resources to the current session");
      return;
    }

    List<IProject> projectsToShare = new ArrayList<IProject>();
    Map<IReferencePoint, List<IResource>> mapping = nextProjectNegotiation.get();

    for (Entry<IReferencePoint, List<IResource>> mapEntry : mapping.entrySet()) {
      final IReferencePoint referencePoint = mapEntry.getKey();
      final IProject project = referencePointManager.get(referencePoint);
      final List<IResource> resourcesList = mapEntry.getValue();

      // side effect: non shared projects are always partial -.-
      if (!currentSession.isCompletelyShared(project.getReferencePoint())) {
        String projectID = currentSession.getReferencePointID(project.getReferencePoint());

        if (projectID == null) {
          projectID = String.valueOf(SESSION_ID_GENERATOR.nextInt(Integer.MAX_VALUE));
        }
        currentSession.addSharedResources(project.getReferencePoint(), projectID, resourcesList);

        projectsToShare.add(project);
      }
    }

    if (projectsToShare.isEmpty()) {
      log.warn(
          "skipping project negotiation because no new projects were added to the current session");
      return;
    }

    INegotiationHandler handler = negotiationHandler;
    if (handler == null) {
      log.warn("could not start a project negotiation because no handler is installed");
      return;
    }

    List<AbstractOutgoingProjectNegotiation> negotiations =
        new ArrayList<AbstractOutgoingProjectNegotiation>();

    if (!startStopSessionLock.tryLock()) {
      log.warn(
          "could not start a project negotiation because the current session is about to stop");
      return;
    }

    try {
      for (User user : currentSession.getRemoteUsers()) {

        TransferType type =
            TransferType.valueOf(
                currentSession
                    .getUserProperties(user)
                    .getString(ProjectNegotiationTypeHook.KEY_TYPE));
        AbstractOutgoingProjectNegotiation negotiation =
            negotiationFactory.newOutgoingProjectNegotiation(
                user.getJID(),
                type,
                projectsToShare
                    .stream()
                    .map(project -> project.getReferencePoint())
                    .collect(Collectors.toList()),
                this,
                currentSession);

        negotiation.setNegotiationListener(negotiationListener);
        currentProjectNegotiations.add(negotiation);
        negotiations.add(negotiation);
      }
    } finally {
      startStopSessionLock.unlock();
    }

    for (AbstractOutgoingProjectNegotiation negotiation : negotiations)
      handler.handleOutgoingProjectNegotiation(negotiation);
  }

  @Override
  public void startSharingProjects(JID user) {

    ISarosSession currentSession = session;

    if (currentSession == null) {
      /*
       * as this currently only called by the OutgoingSessionNegotiation
       * job just silently return
       */
      log.error("cannot share projects when no session is running");
      return;
    }

    List<IProject> currentSharedProjects =
        new ArrayList<IProject>(
            referencePointManager.getProjects(currentSession.getReferencePoints()));

    if (currentSharedProjects.isEmpty()) return;

    INegotiationHandler handler = negotiationHandler;

    if (handler == null) {
      log.warn("could not start a project negotiation because no" + " handler is installed");
      return;
    }

    AbstractOutgoingProjectNegotiation negotiation;

    synchronized (this) {
      if (!startStopSessionLock.tryLock()) {
        log.warn(
            "could not start a project negotiation because the"
                + " current session is about to stop");
        return;
      }

      try {
        User remoteUser = currentSession.getUser(user);
        if (remoteUser == null) {
          log.warn(
              "could not start a project negotiation because"
                  + " the remote user is not part of the current session");
          return;
        }

        TransferType type =
            TransferType.valueOf(
                currentSession
                    .getUserProperties(remoteUser)
                    .getString(ProjectNegotiationTypeHook.KEY_TYPE));
        negotiation =
            negotiationFactory.newOutgoingProjectNegotiation(
                user,
                type,
                currentSharedProjects
                    .stream()
                    .map(project -> project.getReferencePoint())
                    .collect(Collectors.toList()),
                this,
                currentSession);

        negotiation.setNegotiationListener(negotiationListener);
        currentProjectNegotiations.add(negotiation);

      } finally {
        startStopSessionLock.unlock();
      }
    }
    handler.handleOutgoingProjectNegotiation(negotiation);
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
