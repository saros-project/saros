package saros.session;

public class UserPrivilege {

  public enum Keys {
    SESSION_ADMINISTER,
    SESSION_DELETE_DATA,
    SESSION_GRANT_PERMISSION,
    SESSION_INVITE_USER,
    SESSION_JOIN,
    SESSION_READONLY_ACCESS,
    SESSION_SHARE_DOCUMENT,
    SESSION_START_SERVER,
    SESSION_STOP_SERVER,
    SESSION_WRITE_ACCESS,
    CONFIGURE_SERVER
  }

  protected UserPrivilege.Keys key;
  protected Boolean value = false; // defaults to false, no?

  public UserPrivilege(UserPrivilege.Keys key, Boolean value) {
    this.key = key;
    this.value = value;
  }

  public UserPrivilege() {}

  public String toString() {
    return this.key.toString() + " : " + getValue();
  }

  public void setKey(UserPrivilege.Keys key) {
    this.key = key;
  }

  public UserPrivilege.Keys getKey() {
    return this.key;
  }

  // null becomes false
  public void setValue(Boolean value) {
    if (value.compareTo(true) == 0) {
      this.value = value;
    } else if (value == null) {
      this.value = new Boolean(false);
    }
  }

  public Boolean getValue() {
    return this.value;
  }
}
