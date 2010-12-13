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
        return String.format("%s@%s", username, server);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof XMPPAccount))
            return false;

        XMPPAccount otherAcc = (XMPPAccount) o;

        return this.username.equals(otherAcc.username)
            && this.server.equals(otherAcc.server)
            && this.password.equals(otherAcc.password);
    }
}
