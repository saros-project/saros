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
package saros.session;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import saros.activities.IActivity;
import saros.activities.IResourceActivity;
import saros.concurrent.management.ConcurrentDocumentClient;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.net.xmpp.JID;
import saros.session.IActivityConsumer.Priority;
import saros.session.User.Permission;
import saros.synchronize.StopManager;

/**
 * A Saros session consists of one or more shared reference point(s), which are the central concept
 * of the Saros plugin. They are associated with local directory trees and make them available for
 * synchronous/real-time collaboration.
 */
public interface ISarosSession {

  /**
   * @JTourBusStop 3, Architecture Overview, Session Management:
   *
   * <p>This Interface is the main entrance Point for the "Session Management"-Component. The
   * Session Management is responsible for managing a Session and keeping the shared reference
   * points in a consistent state across the local copies of all participants. It functions as the
   * core component in a running session and directs communication between all other components. In
   * general this component takes input from the User Interface, processes it, and afterwards passes
   * the result to the Network Layer.
   */

  /** Connection identifier to use for sending data. */
  String SESSION_CONNECTION_ID = "saros-main-session";

  /** @return a list of all users of this session */
  List<User> getUsers();

  /** @return a list of all remote users of this session */
  List<User> getRemoteUsers();

  /**
   * Changes the {@link Permission permission} of a user.
   *
   * <p><b>Restriction: </b> This method may only called by the host.<br>
   * <b>Restriction: </b> This method has to be invoked from a background thread.
   *
   * @param user The user whose {@link Permission} has to be changed
   * @param permission The new {@link Permission} of the user
   * @throws CancellationException
   * @throws InterruptedException
   */
  void changePermission(User user, Permission permission)
      throws CancellationException, InterruptedException;

  /**
   * @return <code>true</code> if the local user has {@link Permission#WRITE_ACCESS write access},
   *     <code>false</code> otherwise
   */
  boolean hasWriteAccess();

  /**
   * Returns the host of this session.
   *
   * @immutable This method will always return the same value for this session
   */
  User getHost();

  /**
   * @return <code>true</code> if the local user is the host of this session, <code>false</code>
   *     otherwise.
   */
  boolean isHost();

  /**
   * Adds the user to this session. If the session currently serves as host all other session users
   * will be noticed about the new user.
   *
   * @param user the user that is to be added
   */
  void addUser(User user);

  /**
   * Informs all listeners that a user now has reference points and can process {@link
   * IResourceActivity}s.
   *
   * @host This method may only called by the host.
   * @param user
   */
  void userStartedQueuing(final User user);

  /**
   * Informs all participants and listeners that a user now has finished the resource negotiation.
   *
   * @param user
   */
  void userFinishedResourceNegotiation(final User user);

  /**
   * Removes a user from this session.
   *
   * @param user the user that is to be removed
   */
  void removeUser(User user);

  /**
   * Kicks and removes the user out of the session.
   *
   * @param user the user that should be kicked from the session
   * @throws IllegalStateException if the local user is not the host of the session
   * @throws IllegalArgumentException if the user to kick is the local user
   */
  void kickUser(User user);

  /**
   * Adds the given session listener. This call is ignored if the listener is already a listener of
   * this session.
   *
   * @param listener the listener to add
   */
  void addListener(ISessionListener listener);

  /**
   * Removes the given session listener. This call is ignored if the listener does not belong to the
   * current listeners of this session.
   *
   * @param listener the listener to remove
   */
  void removeListener(ISessionListener listener);

  /**
   * Enables or disables the execution of received activities for the given reference point. If the
   * execution is disabled, activities for resources of the given reference point will be dropped
   * without being applied.
   *
   * <p>This method can be used to disable the execution of received activities for reference points
   * that are no longer available (i.e. are no longer part of the session or are no longer
   * present/accessible locally).
   *
   * @param referencePoint the shared reference point to enable or disable the activity execution
   *     for
   * @param enabled <code>true</code> to enable or <code>false</code> to disable the activity
   *     execution
   */
  void setActivityExecution(IReferencePoint referencePoint, boolean enabled);

  /**
   * @return the shared reference points associated with this session, never <code>null</code> but
   *     may be empty
   */
  Set<IReferencePoint> getReferencePoints();

  /**
   * FOR INTERNAL USE ONLY !
   *
   * @deprecated only the session manager should be able to call this
   */
  @Deprecated
  void start();

  /**
   * Given a resource qualified JID, this method will return the user which has the identical ID
   * including resource.
   *
   * <p>Use getResourceQualifiedJID(JID) in the case if you do not know the RQ-JID.
   *
   * @return the user with the given fully qualified JID or <code>null</code> if not user with such
   *     a JID exists in the session
   */
  User getUser(JID jid);

  /**
   * Given a JID (resource qualified or not), will return the resource qualified JID associated with
   * this user or <code>null</code> if no user for the given JID exists in the session.
   *
   * <pre>
   * E.g:
   * <code>
   * JID rqJID = session.getResourceQualifiedJID(new JID("alice@foo.com");
   * System.out.println(rqJID);
   * </code>
   * </pre>
   *
   * <p>Will print out something like alice@foo.com/Saros*****
   *
   * @param jid the JID to retrieve the resource qualified JID for
   * @return the resource qualified JID or <code>null</code> if no user is found with this JID
   * @deprecated Do not use this method in new code, ensure you can obtain a resource qualified JID
   *     and use {@link #getUser(JID)} instead.
   */
  @Deprecated
  JID getResourceQualifiedJID(JID jid);

  /**
   * Returns the local user of this session.
   *
   * @immutable This method will always return the same value for this session
   */
  User getLocalUser();

  /**
   * the concurrent document manager is responsible for all jupiter controlled documents
   *
   * @return the concurrent document manager
   */
  ConcurrentDocumentClient getConcurrentDocumentClient();

  /**
   * Returns a snapshot of the currently unavailable (in use) color ids.
   *
   * @return
   */
  Set<Integer> getUnavailableColors();

  /** FOR INTERNAL USE ONLY ! */
  void exec(List<IActivity> activities);

  /**
   * Adds an {@link IActivityProducer} so the production of its activities will be noticed.
   *
   * @param producer The session will register an {@link IActivityListener} on this producer. It is
   *     expected that the producer will inform that listener about new activities via {@link
   *     IActivityListener#created(IActivity) created()}.
   * @see #removeActivityProducer(IActivityProducer)
   */
  void addActivityProducer(IActivityProducer producer);

  /**
   * Removes an {@link IActivityProducer} from the session.
   *
   * @param producer The session will unregister its {@link IActivityListener} from this producer
   *     and it is expected that the producer no longer calls {@link
   *     IActivityListener#created(IActivity) created()}.
   * @see #addActivityProducer(IActivityProducer)
   */
  void removeActivityProducer(IActivityProducer producer);

  /**
   * Adds an {@link IActivityConsumer} so it will be called when an activity is to be executed
   * locally.
   *
   * @param consumer The {@link IActivityConsumer#exec(IActivity) exec()} method of this consumer
   *     will be called. "Consume" is not meant in a destructive way: all consumers will be called
   *     for every activity.
   * @param priority Indicates whether this consumer performs actions that have visible consequences
   *     or just records some state (see {@link IActivityConsumer.Priority}). The latter type will
   *     be notified first.<br>
   *     Adding the same consumer multiple times with different priorities will assume the last one
   *     is correct. Use individual consumers if you want to get notified multiple times.
   * @see #removeActivityConsumer(IActivityConsumer)
   */
  void addActivityConsumer(IActivityConsumer consumer, Priority priority);

  /**
   * Removes an {@link IActivityConsumer} from the session
   *
   * @param consumer This consumer will no longer be called when an activity is to be executed
   *     locally.
   * @see #addActivityConsumer(IActivityConsumer, Priority)
   */
  void removeActivityConsumer(IActivityConsumer consumer);

  /**
   * Checks if the user is ready to process {@link IResourceActivity}s for a given reference point
   */
  boolean userHasReferencePoint(User user, IReferencePoint referencePoint);

  /**
   * @return <code>true</code> if the given {@link IResource resource} is currently shared in this
   *     session, <code>false</code> otherwise
   */
  boolean isShared(IResource resource);

  /**
   * Returns the global ID of the reference point.
   *
   * @return the global ID of the reference point or <code>null</code> if this reference point is
   *     not shared
   */
  String getReferencePointId(IReferencePoint referencePoint);

  /**
   * Returns the reference point with the given ID.
   *
   * @return the reference point with the given ID or <code>null</code> if no reference point with
   *     this ID is shared
   */
  IReferencePoint getReferencePoint(String referencePointID);

  /**
   * Adds the specified reference point to this session.
   *
   * @param referencePoint The reference point to share
   * @param referencePointId The global reference point ID
   */
  void addSharedReferencePoint(IReferencePoint referencePoint, String referencePointId);

  /**
   * Stores a bidirectional mapping between <code>reference point</code> and <code>
   * reference point ID</code>.
   *
   * <p>This information is necessary for receiving (deserializing) resource-related activities.
   *
   * @param referencePointId Session-wide ID of the reference point
   * @param referencePoint the local representation of the reference point
   * @see #removeReferencePointMapping(String, IReferencePoint)
   */
  void addReferencePointMapping(String referencePointId, IReferencePoint referencePoint);

  /**
   * Removes the bidirectional mapping <code>reference point</code> and <code>reference point ID
   * </code> that was created by {@link #addReferencePointMapping(String, IReferencePoint)} .
   *
   * <p>TODO Why is the reference point parameter needed here? This forces callers to store the
   * mapping themselves (or retrieve it just before calling this method).
   *
   * @param referencePointId Session-wide ID of the reference point
   * @param referencePoint the local representation of the reference point
   */
  void removeReferencePointMapping(String referencePointId, IReferencePoint referencePoint);

  /**
   * Return the stop manager of this session.
   *
   * @return
   */
  StopManager getStopManager();

  /**
   * Changes the color for the current session. The color change is performed on the session host
   * and may therefore result in a different color id.
   *
   * @param colorID the new color id that should be used during the session
   */
  void changeColor(int colorID);

  /**
   * FOR INTERNAL USE ONLY !
   *
   * <p>Starts queuing of incoming {@linkplain IResourceActivity reference-point-related
   * activities}, since they cannot be applied before their corresponding reference point is
   * received and extracted.
   *
   * <p>That queuing relies on an existing reference point-to-reference-point-ID mapping (see {@link
   * #addReferencePointMapping(String, IReferencePoint)}), otherwise incoming activities cannot be
   * queued and will be lost.
   *
   * @param referencePoint the reference point for which reference-point-related activities should
   *     be queued
   * @see #disableQueuing
   */
  void enableQueuing(IReferencePoint referencePoint);

  /**
   * FOR INTERNAL USE ONLY !
   *
   * <p>Disables queuing for the given reference point and flushes all queued activities.
   */
  void disableQueuing(IReferencePoint referencePoint);

  /**
   * Returns the id of the current session.
   *
   * @return the id of the current session
   */
  String getID();

  /**
   * Returns the session runtime component with the given key.
   *
   * <p><b>Attention:</b> This method should be used with great care. It is up to to the caller to
   * ensure that the returned reference can be garbage collected when the session has stopped, i.e
   * by setting the reference to <code>null</code>
   *
   * @param key the key of the component
   * @return the runtime component or <code>null</code> if the component is either not available or
   *     does not exist
   */
  <T> T getComponent(Class<T> key);
}
