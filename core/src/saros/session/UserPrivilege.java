package saros.session;

/**
 * Represent a privilege of a sessions user.
 *
 * <p>A user privilege object has a immutable key and a mutable value. Key is set on instantiation.
 *
 * <p>Privilege key is limited to the defined Keys
 *
 * @param permission
 */
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

    private final Keys key;
    private boolean value = false;

    /**
     * Construct a UserPrivilege instance by supplying one of the privilege Keys and a boolean value.
     *
     * @param key
     * @param value
     */

    public UserPrivilege(Keys key, boolean value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return this.key.toString() + " : " + this.getValue();
    }

    public Keys getKey() {
        return this.key;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return this.value;
    }
}