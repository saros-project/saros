package saros.editor.remote;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.picocontainer.Startable;
import saros.activities.IActivity;
import saros.activities.SPath;
import saros.annotations.Component;
import saros.session.IActivityConsumer;
import saros.session.ISarosSession;
import saros.session.ISessionListener;
import saros.session.User;

/**
 * This class contains the state of the editors, viewports and selections of all remote users as we
 * believe it to be by listening to the Activities we receive. This class is a {@linkplain
 * saros.session.IActivityConsumer.Priority#PASSIVE passive consumer}.
 */
@Component
public class UserEditorStateManager implements IActivityConsumer, Startable {

  /**
   * Most sessions have two participants, some have three. So there are one, maybe two remote users
   * (<code>initialCapacity: 2</code>). Furthermore, mostly expect access from one thread, maybe two
   * ( <code>concurrencyLevel: 2</code>).
   */
  private final ConcurrentHashMap<User, UserEditorState> userEditorStates =
      new ConcurrentHashMap<User, UserEditorState>(2, 0.75f, 2);

  private final ISarosSession sarosSession;

  private final ISessionListener setupAndTeardownUserStates =
      new ISessionListener() {
        @Override
        public void userJoined(User user) {
          /*
           * We cannot be sure that userJoined() will be called before the
           * request on getState(), therefore we don't simply "put" an empty
           * state in the map.
           */
          userEditorStates.putIfAbsent(user, new UserEditorState());
        }

        @Override
        public void userLeft(User user) {
          userEditorStates.remove(user);
        }
      };

  /**
   * Created by PicoContainer
   *
   * @param sarosSession the session to listen on for activities and users leaving and joining
   */
  public UserEditorStateManager(ISarosSession sarosSession) {
    this.sarosSession = sarosSession;
  }

  @Override
  public void start() {
    sarosSession.addActivityConsumer(this, Priority.PASSIVE);
    sarosSession.addListener(setupAndTeardownUserStates);
  }

  @Override
  public void stop() {
    sarosSession.removeActivityConsumer(this);
    sarosSession.removeListener(setupAndTeardownUserStates);
  }

  @Override
  public void exec(IActivity activity) {
    /*
     * This class manages the user state objects, which are created lazily
     * upon the first received activity. Therefore, this class needs to be
     * an activity consumer that is registered to the session. Hence, there
     * is no need for the user state consumers to be registered to the
     * session -- instead, this method implements a real activity second
     * dispatch.
     */
    UserEditorState state = getState(activity.getSource());
    state.consumer.exec(activity);
  }

  /* Public methods */

  /**
   * Retrieve the status of the shared editors of the given remote user as inferred by the
   * activities received from him/her.
   *
   * @param user A user in the current session
   * @return The remote user's current status of shared editors. In case we didn't receive any
   *     activities from this user yet, the status will be empty (see {@link UserEditorState}).
   */
  public UserEditorState getState(User user) {
    /*
     * Lazily adding a new state allows to record activities, in case
     * someone is interested in the user state before the first activity
     * arrived.
     */
    UserEditorState initState = new UserEditorState();
    UserEditorState oldValue = userEditorStates.putIfAbsent(user, initState);

    return oldValue != null ? oldValue : initState;
  }

  /**
   * Returns all paths representing the editors which are currently opened by the remote users of
   * the running session (i.e. not our own).
   *
   * @return A set containing the paths corresponding to all open editors. If no editors are opened
   *     an empty set is being returned.
   */
  public Set<SPath> getOpenEditors() {
    Set<SPath> result = new HashSet<SPath>();
    for (UserEditorState state : userEditorStates.values()) {
      result.addAll(state.getOpenEditors());
    }
    return result;
  }
}
