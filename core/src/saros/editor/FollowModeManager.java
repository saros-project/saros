package saros.editor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import saros.activities.EditorActivity;
import saros.activities.IActivity;
import saros.activities.SPath;
import saros.activities.StartFollowingActivity;
import saros.activities.StopFollowingActivity;
import saros.activities.TextSelectionActivity;
import saros.activities.ViewportActivity;
import saros.context.IContextFactory;
import saros.editor.IFollowModeListener.Reason;
import saros.editor.remote.EditorState;
import saros.editor.remote.UserEditorStateManager;
import saros.editor.text.LineRange;
import saros.editor.text.TextSelection;
import saros.filesystem.IFile;
import saros.repackaged.picocontainer.Startable;
import saros.session.AbstractActivityConsumer;
import saros.session.IActivityConsumer;
import saros.session.IActivityConsumer.Priority;
import saros.session.ISarosSession;
import saros.session.ISarosSessionContextFactory;
import saros.session.ISessionListener;
import saros.session.User;

/**
 * Allows to follow other session participants. To be in the Follow Mode means to locally mirror
 * remote editor changes (activation and closing editors) and viewport changes (upon scrolling or
 * changed selections). The Follow Mode is stopped when the local user takes on actions on his/her
 * own, such as opening a different editor. Typing and scrolling, however, does not stop the Follow
 * Mode.
 *
 * <p>TODO Move this class to the UI package as this feature does not make any sense without a
 * graphical user interface. (Precondition: The {@link ISarosSessionContextFactory}s need to switch
 * from a inheritance-based to a composition-based style, just like the {@link IContextFactory}s.)
 */
public class FollowModeManager implements Startable {

  private static final Logger log = Logger.getLogger(FollowModeManager.class);

  private final ISarosSession session;

  private final IEditorManager editorManager;
  private final UserEditorStateManager userEditorStateManager;

  private User localUser;
  private User currentlyFollowedUser;

  private final Map<User, User> followModeStates = Collections.synchronizedMap(new HashMap<>());

  private final CopyOnWriteArrayList<IFollowModeListener> listeners = new CopyOnWriteArrayList<>();

  /** If the user left which I am following, then stop following him/her */
  private ISessionListener stopFollowingWhenUserLeaves =
      new ISessionListener() {
        @Override
        public void userLeft(User user) {

          followModeStates.remove(user);

          if (!isFollowing()) return;

          if (user.equals(currentlyFollowedUser)) {

            dropFollowModeState();
            notifyStopped(Reason.FOLLOWEE_LEFT_SESSION);
          }
        }
      };

  /** If the local user activates another editor (or closes the followed one), stop following */
  private ISharedEditorListener stopFollowingOnOwnActions =
      new ISharedEditorListener() {
        @Override
        public void editorActivated(User user, SPath filePath) {
          if (!user.equals(localUser) || !isFollowing()) return;

          Reason reason = Reason.FOLLOWER_CLOSED_OR_SWITCHED_EDITOR;

          EditorState remoteActiveEditor = followeeEditor();

          if (remoteActiveEditor != null && !remoteActiveEditor.getPath().equals(filePath)) {

            dropFollowModeState();
            notifyStopped(reason);
          }
        }

        @Override
        public void editorClosed(User user, SPath filePath) {
          if (!user.equals(localUser) || !isFollowing()) return;

          Reason reason = Reason.FOLLOWER_CLOSED_EDITOR;

          EditorState remoteActiveEditor = followeeEditor();

          if (remoteActiveEditor != null && remoteActiveEditor.getPath().equals(filePath)) {

            dropFollowModeState();
            notifyStopped(reason);
          }
        }
      };

  private IActivityConsumer remoteFollowStates =
      new AbstractActivityConsumer() {
        @Override
        public void receive(StartFollowingActivity activity) {
          final User follower = activity.getSource();
          final User followee = activity.getFollowedUser();

          followModeStates.put(follower, followee);
          notifyRemoteStarted(follower, followee);
        }

        @Override
        public void receive(StopFollowingActivity activity) {
          final User follower = activity.getSource();

          followModeStates.remove(follower);
          notifyRemoteStopped(follower);
        }
      };

  /**
   * If the followed user activates or closes an editor, activate or close it too. If the followed
   * user scrolls or changes his/her selection, adjust the local viewport.
   */
  private IActivityConsumer mirrorRemoteEditor =
      new AbstractActivityConsumer() {
        @Override
        public void exec(IActivity activity) {
          if (!isFollowing(activity.getSource())) return;

          super.exec(activity);
        }

        @Override
        public void receive(EditorActivity activity) {
          IFile file = activity.getResource();

          switch (activity.getType()) {
            case ACTIVATED:
              // path == null means there is no open editor left
              if (file != null) {
                // open editor, but don't change focus
                editorManager.openEditor(new SPath(file), false);
              } else {
                // the follow mode is "paused"
              }
              break;
            case CLOSED:
              break;
            case SAVED:
              break;
          }
        }

        @Override
        public void receive(ViewportActivity activity) {
          LineRange range = new LineRange(activity.getStartLine(), activity.getNumberOfLines());

          editorManager.adjustViewport(
              new SPath(activity.getResource()), range, followeeSelection());
        }

        @Override
        public void receive(TextSelectionActivity activity) {
          TextSelection selection = activity.getSelection();

          editorManager.adjustViewport(
              new SPath(activity.getResource()), followeeRange(), selection);
        }
      };

  public FollowModeManager(
      ISarosSession session,
      IEditorManager editorManager,
      UserEditorStateManager userEditorStateManager) {

    this.editorManager = editorManager;
    this.session = session;
    this.userEditorStateManager = userEditorStateManager;
  }

  @Override
  public void start() {
    localUser = session.getLocalUser();

    session.addListener(stopFollowingWhenUserLeaves);
    session.addActivityConsumer(mirrorRemoteEditor, Priority.ACTIVE);
    session.addActivityConsumer(remoteFollowStates, Priority.ACTIVE);
    editorManager.addSharedEditorListener(stopFollowingOnOwnActions);
  }

  @Override
  public void stop() {
    localUser = null;
    currentlyFollowedUser = null;

    session.removeActivityConsumer(remoteFollowStates);
    session.removeActivityConsumer(mirrorRemoteEditor);
    session.removeListener(stopFollowingWhenUserLeaves);

    editorManager.removeSharedEditorListener(stopFollowingOnOwnActions);

    followModeStates.clear();
  }

  /* Public methods */

  /**
   * Sets the {@link User} to follow.
   *
   * <p>Registered {@link IFollowModeListener listeners} will be notified about the effective
   * changes (meaning if the method is called multiple times with the same parameter, no new events
   * will be fired). There are three cases:
   *
   * <ol>
   *   <li>User enters the follow mode: {@link IFollowModeListener#startedFollowing(User)
   *       startedFollowing()} is called.
   *   <li>User stops the follow mode: {@link IFollowModeListener#stoppedFollowing(Reason)
   *       stoppedFollowing()} is called with {@link Reason#FOLLOWER_STOPPED}.
   *   <li>User switches the followee: {@link IFollowModeListener#stoppedFollowing(Reason)
   *       stoppedFollowing()} is called with {@link Reason#FOLLOWER_SWITCHES_FOLLOWEE}, immediately
   *       followed by a call of {@link IFollowModeListener#startedFollowing(User)
   *       startedFollowing()}
   * </ol>
   *
   * @param newFollowedUser The user to follow from now on. Set to <code>null</code> if no user
   *     should be followed.
   */
  public void follow(User newFollowedUser) {

    final User localUser = session.getLocalUser();

    assert newFollowedUser == null || !newFollowedUser.equals(localUser)
        : "local user cannot follow himself!";

    final User previouslyFollowedUser = currentlyFollowedUser;

    currentlyFollowedUser = newFollowedUser;

    if (newFollowedUser != null && !newFollowedUser.equals(previouslyFollowedUser)) {
      editorManager.jumpToUser(newFollowedUser);

      if (previouslyFollowedUser != null) {
        followModeStates.remove(localUser);
        notifyStopped(Reason.FOLLOWER_SWITCHES_FOLLOWEE);
      }

      followModeStates.put(localUser, newFollowedUser);
      notifyStarted(newFollowedUser);

    } else if (newFollowedUser == null && previouslyFollowedUser != null) {
      followModeStates.remove(localUser);
      notifyStopped(Reason.FOLLOWER_STOPPED);
    }
  }

  /**
   * Returns <code>true</code> if there is currently a {@link User} followed, otherwise <code>false
   * </code>.
   */
  public boolean isFollowing() {
    return currentlyFollowedUser != null;
  }

  /**
   * Check whether a specific user is being followed right now.
   *
   * @param user the user to check
   * @return <code>true</code> if the given user is followed by the local user, <code>false</code>
   *     otherwise (following another user, or none at all).
   */
  public boolean isFollowing(User user) {
    if (user == null) return false;

    return user.equals(currentlyFollowedUser);
  }

  /**
   * Get the currently followed user
   *
   * @return The followed user if there is one, <code>null</code> otherwise.
   */
  public User getFollowedUser() {
    return currentlyFollowedUser;
  }

  /**
   * Returns the current follow mode states as a snapshot.
   *
   * @return the current follow mode states, never <code>null</code>
   */
  public FollowModeStates getFollowModeStates() {
    synchronized (followModeStates) {
      return new FollowModeStates(followModeStates);
    }
  }

  /* Listener related */

  /**
   * Registers a listener interested in changes of the local follow mode.
   *
   * <p>{@link IFollowModeListener#startedFollowing(User) startedFollowing()} and {@link
   * IFollowModeListener#stoppedFollowing(Reason) stoppedFollowing()} will be called until {@link
   * #removeListener(IFollowModeListener) removeListener()} is called.
   */
  public void addListener(IFollowModeListener listener) {
    listeners.addIfAbsent(listener);
  }

  public void removeListener(IFollowModeListener listener) {
    listeners.remove(listener);
  }

  private void notifyStopped(Reason reason) {
    for (IFollowModeListener listener : listeners) {
      try {
        listener.stoppedFollowing(reason);
      } catch (RuntimeException e) {
        log.error("error while calling listener " + listener, e);
      }
    }
  }

  private void notifyStarted(User followee) {
    for (IFollowModeListener listener : listeners) {
      try {
        listener.startedFollowing(followee);
      } catch (RuntimeException e) {
        log.error("error while calling listener " + listener, e);
      }
    }
  }

  private void notifyRemoteStarted(final User follower, final User followee) {
    for (IFollowModeListener listener : listeners) {
      try {
        listener.startedFollowing(follower, followee);
      } catch (RuntimeException e) {
        log.error("error while calling listener " + listener, e);
      }
    }
  }

  private void notifyRemoteStopped(final User follower) {
    for (IFollowModeListener listener : listeners) {
      try {
        listener.stoppedFollowing(follower);
      } catch (RuntimeException e) {
        log.error("error while calling listener " + listener, e);
      }
    }
  }
  /**
   * Drops the currently held state of the follow mode, thereby ending the current follow mode.
   *
   * <p>This method does not notify the listener about the end of the follow mode. Notifying the
   * listener using the correct reason is the responsibility of the caller.
   */
  private void dropFollowModeState() {
    currentlyFollowedUser = null;
  }

  /* Helper */

  private EditorState followeeEditor() {
    return userEditorStateManager.getState(currentlyFollowedUser).getActiveEditorState();
  }

  private TextSelection followeeSelection() {
    EditorState editor = followeeEditor();
    return (editor != null) ? editor.getSelection() : null;
  }

  private LineRange followeeRange() {
    EditorState editor = followeeEditor();
    return (editor != null) ? editor.getViewport() : null;
  }
}
