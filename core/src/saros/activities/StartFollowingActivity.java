package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import saros.session.User;

/**
 * This activity notifies the recipient that the local user is following someone in the running
 * session
 *
 * @author Alexander Waldmann (contact@net-corps.de)
 */
@XStreamAlias("startFollowingActivity")
public class StartFollowingActivity extends AbstractActivity {

  @XStreamAsAttribute protected final User followedUser;

  public StartFollowingActivity(User source, User followedUser) {
    super(source);

    if (followedUser == null) throw new IllegalArgumentException("followedUser must not be null");

    this.followedUser = followedUser;
  }

  @Override
  public boolean isValid() {
    return super.isValid() && (followedUser != null);
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  @Override
  public String toString() {
    return "StartFollowingActivity(" + getSource() + " > " + followedUser + ")";
  }

  public User getFollowedUser() {
    return followedUser;
  }
}
