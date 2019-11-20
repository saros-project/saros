package saros.session;

public class UserPrivilege {
	
	public enum Privilege {
	    WRITE_ACCESS,
	    READONLY_ACCESS,
	    SHARE_DOCUMENT,
	    INVITE_USER,
	    GRANT_PERMISSION,
	    JOIN_SESSION,
	    START_SESSION_SERVER,
	    STOP_SESSION_SERVER,
	    DELETE_SESSION_DATA,
	    CONFIGURE_SERVER
	  }

	protected Privilege key;
	protected Boolean value = false; // defaults to false, no?

    public UserPrivilege(Privilege key, Boolean value) {
        this.key = key;
        this.value = value;
    }

    public UserPrivilege() {
        
    }
    
    public String toString() {
        return this.key.toString() + " : " + getValue();
    }
    
    public void setKey(Privilege key) {
        this.key = key;
    }

    public Privilege getKey() {
        return this.key;
    }

    // null becomes false
    public void setValue(Boolean value) {
        if (value == true) {
            this.value = value;
        } else if (value == null) {
            this.value = false;
        }
    }

    public Boolean getValue() {
        return this.value;
    }
}
