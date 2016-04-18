package de.fu_berlin.inf.dpp.ui.model;

import de.fu_berlin.inf.dpp.net.xmpp.JID;

/**
 * Represents a Saros XMPP account of the user. In contrast, {@link Contact}
 * represents user in the contact list of such an account.
 * 
 * This class is immutable.
 * 
 * Maybe in the future both classes will be merged.
 */
public class Account {

    private final String username;
    private final String domain;

    private final JID jid;

    /**
     * Creates a new account model from given username and domain. If the
     * username or domain is null this will throw an IllegalArgumentException.
     * 
     * @param username
     *            the username of the XMPP account
     * @param domain
     *            the domain part of the XMPP account
     * @throw IllegalArgumentException if username or domain is null
     */
    public Account(String username, String domain) {
        this.username = username;
        this.domain = domain;
        jid = new JID(username, domain);
    }

    /**
     * Creates a new account model from given jid. If jid is nullthis will throw
     * an IllegalArgumentException.
     * 
     * @param jid
     *            the jid as string
     * 
     * @throw IllegalArgumentException if jid is null
     */
    public Account(String jid) {
        this.jid = new JID(jid);
        username = this.jid.getName();
        domain = this.jid.getDomain();
    }

    public String getUsername() {
        return username;
    }

    public String getDomain() {
        return domain;
    }

    public String getBareJid() {
        return jid.getBareJID().toString();
    }
}
