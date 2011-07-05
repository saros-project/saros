package de.fu_berlin.inf.dpp.test.xmpp;

/**
 * A XmppUser holds all relevant information which you to create an account or login
 * on a XMPP-Server.
 * 
 * @author cordes
 */
public class XmppUser {

    private String username;
    private String password;
    private String serverAdress;

    public String getUsername() {
        return username;
    }

    protected void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    protected void setPassword(String password) {
        this.password = password;
    }

    public String getServerAdress() {
        return serverAdress;
    }

    protected void setServerAdress(String serverAdress) {
        this.serverAdress = serverAdress;
    }
}
