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
package de.fu_berlin.inf.dpp.xmpp;


/**
 * A Jabber-ID.
 * 
 * @author rdjemili
 */
public class JID {
    // TODO check smack stringUtils
    
    private final String jid;
    
    /**
     * Construct a new Jabber-ID with in the format user@host[/resource].
     * Resource is optional.
     * 
     * @param jid
     */
    public JID(String jid) {
        // TODO
        
//        if (jid.indexOf('@') < 0) {
//            throw new IllegalArgumentException("Malformated JID: "+jid);
//        }
        
        this.jid = jid;
    }
    
    public String getJID() { // TODO rename or just use toString
        return jid;
    }
    
    public String getName() {
        return jid.substring(0, jid.indexOf('@'));
    }
    
    /**
     * @return the JID without resource qualifier.
     */
    public String getBase() {
        return getName() + '@' + getDomain();
    }
    
    public String getDomain() {
        int l = jid.indexOf('/');
        
        if (l < 0) {
            l = jid.length();
        }
        
        return jid.substring(jid.indexOf('@') + 1, l);
    }
    
    public String getResource() {
        int l = jid.indexOf('/') + 1;
        
        if (l > 0) {
            return jid.substring(l, jid.length());
        } else {
            return null;
        }
    }
    
    /**
     * @return <code>true</code> if the IDs have the same user and domain.
     * Resource is ignored.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JID) {
            JID jid = (JID)obj;
            return getName().equals(jid.getName()) && getDomain().equals(jid.getDomain());
        }
        
        return false;
    }
    
//    public boolean equalsIgnoreResource(Object obj) {
//        return super.equals(obj);
//    }
    
    @Override
    public String toString() {
        return "JID("+jid+")";
    }
}
