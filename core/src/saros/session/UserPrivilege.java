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
  private UserPrivilege.Keys key;
  private boolean value = false; // defaults to false, no?


  public UserPrivilege(UserPrivilege.Keys key, boolean value) {

    this.key = key;
    this.value = value;
  }


  @Override
  public String toString() {
    return this.key.toString() + " : " + getValue();
  }
    
  public void setKey(UserPrivilege.Keys key) {
    this.key = key;
  }

  public UserPrivilege.Keys getKey() {
    return this.key;
  }
  public void setValue(boolean value) {
    this.value = value;
  }

  public boolean getValue() {

    return this.value;
  }
}
