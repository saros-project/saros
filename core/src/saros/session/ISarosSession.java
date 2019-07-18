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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import saros.activities.IActivity;
import saros.activities.IResourceActivity;
import saros.concurrent.management.ConcurrentDocumentClient;
import saros.concurrent.management.ConcurrentDocumentServer;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.net.xmpp.JID;
import saros.session.IActivityConsumer.Priority;
import saros.session.User.Permission;
import saros.synchronize.StopManager;

/**
 * A Saros session consists of one or more shared projects, which are the central concept of the
 * Saros plugin. They are associated with projects and make them available for synchronous/real-time
 * collaboration.
 *
 * @author rdjemili
 */
public interface ISarosSession {

  /**
   * @JTourBusStop 3, Architecture Overview, Session Management:
   *
   * <p>This Interface is the main entrance Point for the "Session Management"-Component. The
   * Session Management is responsible for managing a Session and keeping the shared projects in a
   * consistent state across the local copies of all participants. It functions as the core
   * component in a running session and directs communication between all other components. In
   * general this component takes input from the User Interface, processes it, and afterwards passes
   * the result to the Network Layer.
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
   */
  public void addUser(User user);

  /**
   * Informs all listeners that a user now has Projects and can process {@link IResourceActivity}s.
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
   * @return the shared projects associated with this session, never <code>null</code> but may be
   *     empty
   */
  public Set<IProject> getProjects();

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

  /** Checks if the user is ready to process {@link IResourceActivity}s for a given project */
  public boolean userHasProject(User user, IProject project);

  /**
   * @return <code>true</code> if the given {@link IResource resource} is currently shared in this
   *     session, <code>false</code> otherwise
   */
  public boolean isShared(IResource resource);

  /**
   * Checks if selected project is a complete shared one or partial shared.
   *
   * @param project
   * @return <code>true</code> if complete, <code>false</code> if partial
   */
  public boolean isCompletelyShared(IProject project);

  /**
   * Returns the global ID of the project.
   *
   * @return the global ID of the project or <code>null</code> if this project is not shared
   */
  public String getProjectID(IProject project);

  /**
   * Returns the project with the given ID.
   *
   * @return the project with the given ID or <code>null</code> if no project with this ID is shared
   */
  public IProject getProject(String projectID);

  /**
   * Adds the specified project and/or resources to this session.
   *
   * @param project The project to share.
   * @param projectID The global project ID.
   * @param dependentResources The project dependent resources.
   */
  public void addSharedResources(
      IProject project, String projectID, List<IResource> dependentResources);

  /**
   * Returns all shared resources in this session.
   *
   * @return a list of all shared resources (excluding projects) from this session.
   */
  public List<IResource> getSharedResources();

  /**
   * Returns a map with the mapping of shared resources to their project.
   *
   * @return project-->resource mapping
   */
  public Map<IProject, List<IResource>> getProjectResourcesMapping();

  /**
   * Returns the shared resources of the project in this session.
   *
   * @param project
   * @return the shared resources or <code>null</code> if this project is not or fully shared.
   */
  public List<IResource> getSharedResources(IProject project);

  /**
   * Stores a bidirectional mapping between <code>project</code> and <code>projectID</code>.
   *
   * <p>This information is necessary for receiving (unserializing) resource-related activities.
   *
   * @param projectID Session-wide ID of the project
   * @param project the local representation of the project
   * @see #removeProjectMapping(String, IProject)
   */
  public void addProjectMapping(String projectID, IProject project);

  /**
   * Removes the bidirectional mapping <code>project</code> and <code>projectId</code> that was
   * created by {@link #addProjectMapping(String, IProject) addProjectMapping()} .
   *
   * <p>TODO Why is the project parameter needed here? This forces callers to store the mapping
   * themselves (or retrieve it just before calling this method).
   *
   * @param projectID Session-wide ID of the project
   * @param project the local representation of the project
   */
  public void removeProjectMapping(String projectID, IProject project);

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
   * <p>Starts queuing of incoming {@linkplain IResourceActivity project-related activities}, since
   * they cannot be applied before their corresponding project is received and extracted.
   *
   * <p>That queuing relies on an existing project-to-projectID mapping (see {@link
   * #addProjectMapping(String, IProject)}), otherwise incoming activities cannot be queued and will
   * be lost.
   *
   * @param project the project for which project-related activities should be queued
   * @see #disableQueuing
   */
  public void enableQueuing(IProject project);

  /**
   * FOR INTERNAL USE ONLY !
   *
   * <p>Disables queuing for the given project and flushes all queued activities.
   */
  public void disableQueuing(IProject project);

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
