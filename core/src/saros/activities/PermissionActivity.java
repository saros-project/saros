package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import saros.session.User;
import saros.session.User.Permission;

/** A PermissionActivity indicates that a {@link User} has a new {@link Permission}. */
@XStreamAlias("permissionActivity")
public class PermissionActivity extends AbstractActivity {

  @XStreamAsAttribute private final Permission permission;

  @XStreamAsAttribute private final User affectedUser;

  /**
   * Creates a new {@link PermissionActivity} which indicates that the given user should change into
   * the given {@link Permission}.
   */
  public PermissionActivity(User source, User affectedUser, Permission permission) {

    super(source);

    if (affectedUser == null) throw new IllegalArgumentException("affectedUser must not be null");

    this.affectedUser = affectedUser;
    this.permission = permission;
  }

  @Override
  public boolean isValid() {
    return super.isValid() && (affectedUser != null);
  }

  public User getAffectedUser() {
    return affectedUser;
  }

  public Permission getPermission() {
    return permission;
  }

  @Override
  public String toString() {
    return "PermissionActivity(user: "
        + getAffectedUser()
        + ", permission: "
        + getPermission()
        + ")";
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }
}
