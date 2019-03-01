package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import saros.session.User;

@XStreamAlias("nopActivity")
public final class NOPActivity extends AbstractActivity implements ITargetedActivity {

  private final User target;
  private final int id;

  public NOPActivity(User source, User target, int id) {
    super(source);

    if (target == null) throw new IllegalArgumentException("target must not be null");

    this.target = target;
    this.id = id;
  }

  @Override
  public boolean isValid() {
    return super.isValid() && (target != null);
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  public int getID() {
    return id;
  }

  @Override
  public User getTarget() {
    return target;
  }
}
