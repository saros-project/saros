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

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IResourceActivity;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.session.IActivityConsumer.Priority;
import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;

/**
 * A Saros session consists of one or more shared reference points, which are the central concept of
 * the Saros plugin. They are associated with reference points and make them available for
 * synchronous/real-time collaboration.
 *
 * @author rdjemili
 */
public interface ISarosSession {

  /**
   * @JTourBusStop 3, Architecture Overview, Session Management:
   *
   * <p>This Interface is the main entrance Point for the "Session Management"-Component. The
   * Session Management is responsible for managing a Session and keeping the shared reference point
   * in a consistent state across the local * copies of all participants. It functions as the core *
   * component in a running session and directs communication * between all other components. In
   * general this component * takes input from the User Interface, processes it, and * afterwards
   * passes the result to the Network Layer.
   */

  /** Connection identifier to use for sending data. */
  public static final String SESSION_CONNECTION_ID = "saros-main-session";

  /** @return a list of all users of this session */
  public List<User> getUsers();

  /** @return a list of all remote users of this session */
  public List<User> getRemoteUsers();

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
  public void changePermission(User user, Permission permission)
      throws CancellationException, InterruptedException;

  /**
   * @return <code>true</code> if the local user has {@link Permission#WRITE_ACCESS write access},
   *     <code>false</code> otherwise
   */
  public boolean hasWriteAccess();

  /**
   * Returns the host of this session.
   *
   * @immutable This method will always return the same value for this session
   */
  public User getHost();

  /**
   * @return <code>true</code> if the local user is the host of this session, <code>false</code>
   *     otherwise.
   */
  public boolean isHost();

  /**
   * Adds the user to this session. If the session currently serves as host all other session users
   * will be noticed about the new user.
   *
   * @param user the user that is to be added
   * @param preferences the initial properties of the new user
   */
  public void addUser(User user, IPreferenceStore preferences);

  /**
   * Informs all listeners that a user now has reference points and can * process {@link
   * IResourceActivity}s.
   *
   * @host This method may only called by the host.
   * @param user
   */
  public void userStartedQueuing(final User user);

  /**
   * Informs all participants and listeners that a user now has finished the Project Negotiation.
   *
   * @param user
   */
  public void userFinishedProjectNegotiation(final User user);

  /**
   * Removes a user from this session.
   *
   * @param user the user that is to be removed
   */
  public void removeUser(User user);

  /**
   * Kicks and removes the user out of the session.
   *
   * @param user the user that should be kicked from the session
   * @throws IllegalStateException if the local user is not the host of the session
   * @throws IllegalArgumentException if the user to kick is the local user
   */
  public void kickUser(User user);

  /**
   * Adds the given session listener. This call is ignored if the listener is already a listener of
   * this session.
   *
   * @param listener the listener to add
   */
  public void addListener(ISessionListener listener);

  /**
   * Removes the given session listener. This call is ignored if the listener does not belong to the
   * current listeners of this session.
   *
   * @param listener the listener to remove
   */
  public void removeListener(ISessionListener listener);

  /**
   * @return the shared reference points associated with this session, never <code>null</code> but
   *     may be empty
   */
  public Set<IReferencePoint> getReferencePoints();

  /**
   * FOR INTERNAL USE ONLY !
   *
   * @deprecated only the session manager should be able to call this
   */
  @Deprecated
  public void start();

  /**
   * Given a resource qualified JID, this method will return the user which has the identical ID
   * including resource.
   *
   * <p>Use getResourceQualifiedJID(JID) in the case if you do not know the RQ-JID.
   *
   * @return the user with the given fully qualified JID or <code>null</code> if not user with such
   *     a JID exists in the session
   */
  public User getUser(JID jid);

  /**
   * Given a user, this method will return this users session properties.
   *
   * @param user the user to get the preferences for
   * @return Properties of the given user or <code>null</code> if the user is not known to the
   *     session
   */
  public IPreferenceStore getUserProperties(User user);

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
  public JID getResourceQualifiedJID(JID jid);

  /**
   * Returns the local user of this session.
   *
   * @immutable This method will always return the same value for this session
   */
  public User getLocalUser();

  /**
   * the concurrent document manager is responsible for all jupiter controlled documents
   *
   * @return the concurrent document manager
   */
  public ConcurrentDocumentServer getConcurrentDocumentServer();

  /**
   * the concurrent document manager is responsible for all jupiter controlled documents
   *
   * @return the concurrent document manager
   */
  public ConcurrentDocumentClient getConcurrentDocumentClient();

  /**
   * Returns a snapshot of the currently unavailable (in use) color ids.
   *
   * @return
   */
  public Set<Integer> getUnavailableColors();

  /** FOR INTERNAL USE ONLY ! */
  public void exec(List<IActivity> activities);

  /**
   * Adds an {@link IActivityProducer} so the production of its activities will be noticed.
   *
   * @param producer The session will register an {@link IActivityListener} on this producer. It is
   *     expected that the producer will inform that listener about new activities via {@link
   *     IActivityListener#created(IActivity) created()}.
   * @see #removeActivityProducer(IActivityProducer)
   */
  public void addActivityProducer(IActivityProducer producer);

  /**
   * Removes an {@link IActivityProducer} from the session.
   *
   * @param producer The session will unregister its {@link IActivityListener} from this producer
   *     and it is expected that the producer no longer calls {@link
   *     IActivityListener#created(IActivity) created()}.
   * @see #addActivityProducer(IActivityProducer)
   */
  public void removeActivityProducer(IActivityProducer producer);

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
  public void addActivityConsumer(IActivityConsumer consumer, Priority priority);

  /**
   * Removes an {@link IActivityConsumer} from the session
   *
   * @param consumer This consumer will no longer be called when an activity is to be executed
   *     locally.
   * @see #addActivityConsumer(IActivityConsumer, Priority)
   */
  public void removeActivityConsumer(IActivityConsumer consumer);

  /**
   * Checks if the user is ready to process {@link IResourceActivity}s for a given reference point
   */
  public boolean userHasReferencePoint(User user, IReferencePoint referencePoint);

  /**
   * @return <code>true</code> if the given {@link IResource resource} is currently shared in this
   *     session, <code>false</code> otherwise
   */
  @Deprecated
  public boolean isShared(IResource resource);

  /**
   * @return <code>true</code> if the given {@link IResource resource} and its {@link
   *     IReferencePoint reference point} is currently shared in this session, <code>false</code>
   *     otherwise
   */
  public boolean isShared(IReferencePoint referencePoint, IResource resource);

  /**
   * Checks if selected reference point is a complete shared one or partial shared.
   *
   * @param referencePoint
   * @return <code>true</code> if complete, <code>false</code> if partial
   */
  public boolean isCompletelyShared(IReferencePoint referencePoint);

  /**
   * Returns the global ID of the reference point.
   *
   * @return the global ID of the reference point or <code>null</code> if this reference point is
   *     not shared
   * @param referencePoint
   */
  public String getReferencePointID(IReferencePoint referencePoint);

  /**
   * Returns the reference point with the given ID.
   *
   * @return the referencePoint with the given ID or <code>null</code> if no reference point with
   *     this ID is shared
   * @param referencePointID
   */
  public IReferencePoint getReferencePoint(String referencePointID);

  /**
   * Adds the specified reference point and/or resources to this session.
   *
   * @param referencePoint The reference point to share.
   * @param referencePointID The global reference point ID.
   * @param dependentResources
   */
  public void addSharedResources(
      IReferencePoint referencePoint, String referencePointID, List<IResource> dependentResources);

  /**
   * Returns all shared resources in this session.
   *
   * @return a list of all shared resources (excluding reference points) from this session.
   */
  public List<IResource> getSharedResources();

  /**
   * Returns a map with the mapping of shared resources to their reference point.
   *
   * @return reference point-->resource mapping
   */
  public Map<IReferencePoint, List<IResource>> getReferencePointResourcesMapping();

  /**
   * Returns the shared resources of the reference point in this session.
   *
   * @param referencePoint
   * @return the shared resources or <code>null</code> if this reference point is not or fully
   *     shared.
   */
  public List<IResource> getSharedResources(IReferencePoint referencePoint);

  /**
   * Stores a bidirectional mapping between <code>referencePoint</code> and <code>referencePointID
   * </code>.
   *
   * <p>This information is necessary for receiving (unserializing) resource-related activities.
   *
   * @param referencePointID Session-wide ID of the reference point
   * @param referencePoint the local representation of the reference point
   * @see #removeProjectMapping(String, IReferencePoint)
   */
  public void addReferencePointMapping(String referencePointID, IReferencePoint referencePoint);

  /**
   * Removes the bidirectional mapping <code>referencePoint</code> and <code>referencePointId</code>
   * that was created by {@link #addReferencePointMapping(String, IReferencePoint)
   * addReferencePointMapping()} .
   *
   * <p>TODO Why is the project parameter needed here? This forces callers to store the mapping
   * themselves (or retrieve it just before calling this method).
   *
   * @param referencePointID Session-wide ID of the project
   * @param referencePoint the local representation of the project
   */
  public void removeReferencePointMapping(String referencePointID, IReferencePoint referencePoint);

  /**
   * Return the stop manager of this session.
   *
   * @return
   */
  public StopManager getStopManager();

  /**
   * Changes the color for the current session. The color change is performed on the session host
   * and may therefore result in a different color id.
   *
   * @param colorID the new color id that should be used during the session
   */
  public void changeColor(int colorID);

  /**
   * FOR INTERNAL USE ONLY !
   *
   * <p>Starts queuing of incoming {@linkplain IResourceActivity reference point-related
   * activities}, since they cannot be applied before their corresponding reference point is
   * received and extracted.
   *
   * <p>That queuing relies on an existing reference point-to-reference point mapping (see {@link
   * #addReferencePointMapping(String, IReferencePoint)}), otherwise incoming activities cannot be
   * queued and will be lost.
   *
   * @param referencePoint the reference point for which reference point-related activities should
   *     be queued
   * @see #disableQueuing
   */
  public void enableQueuing(IReferencePoint referencePoint);

  /**
   * FOR INTERNAL USE ONLY !
   *
   * <p>Disables queuing for the given reference point and flushes all queued activities.
   *
   * @param referencePoint
   */
  public void disableQueuing(IReferencePoint referencePoint);

  /**
   * Returns the id of the current session.
   *
   * @return the id of the current session
   */
  public String getID();

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
  public <T> T getComponent(Class<T> key);
}
