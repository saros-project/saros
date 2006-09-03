/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.net;

import org.jivesoftware.smack.util.StringUtils;


/**
 * A Jabber ID which is used to identify the users of the Jabber network.
 * 
 * @author rdjemili
 */
public class JID {
    private final String jid;
    
    /**
     * Construct a new Jabber-ID
     * 
     * @param jid the Jabber ID in the format of user@host[/resource]. Resource
     * is optional.
     */
    public JID(String jid) {
        // TODO check for malformated string
        this.jid = jid;
    }
    
    /**
     * @return the name segment of this Jabber ID.
     * @see StringUtils#parseName(String))
     */
    public String getName() {
        return StringUtils.parseName(jid);
    }
    
    /**
     * @return the Jabber ID without resource qualifier.
     * @see StringUtils#parseBareAddress(String)
     */
    public String getBase() {
        return StringUtils.parseBareAddress(jid);
    }
    
    /**
     * @return the domain segment of this Jabber ID.
     * @see StringUtils#parseServer(String)
     */
    public String getDomain() {
        return StringUtils.parseServer(jid);
    }
    
    /**
     * @return the resource segment of this Jabber ID or the empty string if
     * there is none.
     * @see StringUtils#parseResource(String)
     */
    public String getResource() {
        return StringUtils.parseResource(jid);
    }
    
    /**
     * @return <code>true</code> if the IDs have the same user and domain.
     * Resource is ignored.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JID) {
            JID other = (JID)obj;
            return getBase().equals(other.getBase());
        }
        
        return false;
    }
    
    /** 
     * @return the complete string that was used to construct this object.
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return jid;
    }
}
