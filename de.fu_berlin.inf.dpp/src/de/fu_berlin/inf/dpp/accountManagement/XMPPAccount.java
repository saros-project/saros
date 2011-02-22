package de.fu_berlin.inf.dpp.accountManagement;

import java.io.Serializable;

/**
 * Representation of a XMPP-Account.
 * 
 * @author Sebastian Schlaak
 */
public class XMPPAccount implements Serializable {

    private static final long serialVersionUID = 3710620029882513026L;
    public static final int NOT_INITIALIZED = -1;

    int id;
    String username;
    String password;
    String server;
    boolean isActive;

    public XMPPAccount(String username, String password, String server) {
        this.username = username;
        this.password = password;
        this.server = server;
        this.isActive = false;
        id = NOT_INITIALIZED;
    }

    public XMPPAccount() {
        // default constructor
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        if (username.contains("@")) {
            return String.format("%s [%s]", username, server);
        } else {
            return String.format("%s@%s", username, server);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((server == null) ? 0 : server.hashCode());
        result = prime * result
            + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        XMPPAccount other = (XMPPAccount) obj;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (server == null) {
            if (other.server != null)
                return false;
        } else if (!server.equals(other.server))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }
}
