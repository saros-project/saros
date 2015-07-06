package de.fu_berlin.inf.dpp.ui.model;

import de.fu_berlin.inf.dpp.net.xmpp.JID;

/**
 * Represents a Saros account of the user. In contrast, {@link Contact}
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
     * @param username
     *            the username of the XMPP account
     * @param domain
     *            the domain part of the XMPP account
     */
    public Account(String username, String domain) {
        this.username = username;
        this.domain = domain;
        jid = new JID(username, domain);
    }

    /**
     * @param jid
     *            the jid as string
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
