package de.fu_berlin.inf.dpp.accountManagement;

import java.io.Serializable;

/**
 * Representation of a XMPP-Account.
 * 
 * @author Sebastian Schlaak
 * @author Stefan Rossbach
 */
public final class XMPPAccount implements Serializable {

    private static final long serialVersionUID = 3710620029882513026L;

    private final int id;

    private String username;
    private String password;
    private String server;

    private boolean isActive;

    XMPPAccount(int id, String username, String password, String server) {
        this.id = id;

        if (username == null)
            throw new NullPointerException("user name is null");

        if (password == null)
            throw new NullPointerException("password is null");

        if (server == null)
            throw new NullPointerException("server is null");

        if (!server.toLowerCase().equals(server))
            throw new IllegalArgumentException("server '" + server
                + "' contains upppercase characters");

        this.username = username;
        this.password = password;
        this.server = server;
        this.isActive = false;
    }

    public String getUsername() {
        return username;
    }

    void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    void setPassword(String password) {
        this.password = password;
    }

    public String getServer() {
        return server;
    }

    void setServer(String server) {
        this.server = server;
    }

    public boolean isActive() {
        return isActive;
    }

    void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        if (username.contains("@")) {
            return String.format("%s[%s]", username, server);
        } else {
            return String.format("%s@%s", username, server);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + server.hashCode();
        result = prime * result + username.hashCode();
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

        return this.username.equals(other.username)
            && this.server.equals(other.server);
    }
}
