package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import saros.session.User;

/**
 * This activity notifies the recipient that the local user is following someone in the running
 * session
 *
 * <p>TODO Consider treating {@link StartFollowingActivity} and {@link StopFollowingActivity} as
 * different types of the same class (since this class here has no logic of its own).
 */
@XStreamAlias("stopFollowingActivity")
public class StopFollowingActivity extends AbstractActivity {
  public StopFollowingActivity(User source) {
    super(source);
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  @Override
  public String toString() {
    return "StopFollowingActivity(" + getSource() + ")";
  }
}
