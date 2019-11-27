package saros.editor;

import java.util.HashMap;
import java.util.Map;
import saros.session.User;

/** Represents a <b>snapshot</b> of the current follow mode states for all users of a session. */
public final class FollowModeStates {

  private final Map<User, User> followerToFollowee;
  private final Map<User, User> followeeToFollower;

  FollowModeStates(final Map<User, User> states) {
    followerToFollowee = new HashMap<>();
    followeeToFollower = new HashMap<>();

    states.forEach(this::fill);
  }

  /**
   * Returns the followee for the given user.
   *
   * @param user the follower
   * @return the followee or <code>null</code> if the user is not following another user
   */
  public User getFollowee(final User user) {
    return followerToFollowee.get(user);
  }

  /**
   * Returns the follower for the given user.
   *
   * @param user the followee
   * @return the follower or <code>null</code> if the user is not followed by another user
   */
  public User getFollower(final User user) {
    return followeeToFollower.get(user);
  }

  private void fill(final User follower, final User followee) {
    followerToFollowee.put(follower, followee);
    followeeToFollower.put(followee, follower);
  }
}
