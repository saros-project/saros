/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
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
package de.fu_berlin.inf.dpp;

import java.util.Date;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * A user is a representation of a person sitting in front of an eclipse
 * instance for the use in a Saros SharedProject session.
 * 
 * A user object always has the following immutable characteristics: S/he
 * belongs to a single SharedProject instance, has a final color, and unchanging
 * JID.
 * 
 * There is one user who is a host, all others are clients.
 * 
 * There is one local user representing the person in front of the current
 * eclipse instance, all others are remote users.
 * 
 * The public and mutable properties are the role (Driver/Observer), time since
 * going off-line and connection state.
 */
public class User {

    private static final Logger log = Logger.getLogger(User.class.getName());

    public enum UserConnectionState {
        UNKNOWN, ONLINE, OFFLINE
    }

    public enum UserRole {
        DRIVER, OBSERVER
    }

    protected final ISharedProject sharedProject;

    protected final JID jid;

    protected final int colorID;

    protected UserConnectionState presence = UserConnectionState.UNKNOWN;

    /**
     * Time stamp when User became offline the last time. In seconds.
     */
    protected long offlineTime = 0;

    protected UserRole role = UserRole.OBSERVER;

    public User(ISharedProject sharedProject, JID jid, int colorID) {
        if (sharedProject == null || jid == null)
            throw new IllegalArgumentException();
        this.sharedProject = sharedProject;
        this.jid = jid;
        this.colorID = colorID;
    }

    public JID getJID() {
        return this.jid;
    }

    /**
     * set the current user role of this user inside the current project.
     * 
     * @param role
     *            (Driver, Observer)
     */
    public void setUserRole(UserRole role) {
        this.role = role;
    }

    /**
     * Gets current project role of this user.
     * 
     * @return role (Driver, Observer)
     */
    public UserRole getUserRole() {
        return this.role;
    }

    /**
     * Utility method to determine whether this user has the UserRole.DRIVER
     * 
     * @return <code>true</code> if this User is driver, <code>false</code>
     *         otherwise.
     */
    public boolean isDriver() {
        return this.role == UserRole.DRIVER;
    }

    /**
     * Utility method to determine whether this user has the UserRole.OBSERVER
     * 
     * @return <code>true</code> if this User is observer, <code>false</code>
     *         otherwise.
     */
    public boolean isObserver() {
        return this.role == UserRole.OBSERVER;
    }

    @Override
    public String toString() {
        return this.jid.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        JID otherJID = null;

        if (obj instanceof String) {
            otherJID = new JID((String) obj);
        }
        if (obj instanceof JID) {
            otherJID = (JID) obj;
        }
        if (obj instanceof User) {
            otherJID = ((User) obj).jid;
        }

        if (otherJID != null) {
            return this.jid.equals(otherJID);
        }

        log.warn(
            "Comparing a User to an Object that is not a User, String or JID",
            new StackTrace());

        return false;
    }

    @Override
    public int hashCode() {
        return this.jid.hashCode();
    }

    public int getColorID() {
        return this.colorID;
    }

    public UserConnectionState getPresence() {
        return this.presence;
    }

    public void setPresence(UserConnectionState presence) {
        this.presence = presence;
        if (this.presence == User.UserConnectionState.OFFLINE) {
            this.offlineTime = new Date().getTime();
        }
    }

    public int getOfflineSeconds() {
        if (this.presence == UserConnectionState.OFFLINE) {
            return (int) (((new Date().getTime()) - this.offlineTime) / 1000);
        } else {
            return 0;
        }
    }

    /**
     * Gets the ISharedProject to which this user belongs.
     */
    public ISharedProject getSharedProject() {
        return sharedProject;
    }

    /**
     * Returns true if this User object identifies the user which is using the
     * local Eclipse instance as opposed to the remote users in different
     * Eclipse instances.
     */
    public boolean isLocal() {
        return this.equals(sharedProject.getLocalUser());
    }

    /**
     * Returns true if this User is not the local user.
     */
    public boolean isRemote() {
        return !isLocal();
    }

    /**
     * Returns true if this user is the one that initiated the SharedProject
     * session and thus is responsible for synchronization, role management,
     */
    public boolean isHost() {
        return this.equals(sharedProject.getHost());
    }

    /**
     * Returns true if this user is not the host.
     */
    public boolean isClient() {
        return !isHost();
    }

    /**
     * Gets the name for a {@link User} for displaying.
     * 
     * If this is the local user "You" is returned, otherwise the nickname, if
     * available and distinct from the JID, and the JID. If no nickname is
     * known, only the JID is returned.
     */
    public String getHumanReadableName() {

        if (isLocal()) {
            return "You";
        }

        /*
         * TODO This should use a subscription based mechanism or cache the
         * nick, to prevent this being called too many times
         */
        String nickName = Util.getNickname(getSharedProject().getSaros(),
            getJID());
        String jidBase = getJID().getBase();

        if (nickName != null && !nickName.equals(jidBase)) {
            return nickName + " (" + jidBase + ")";
        }

        return jidBase;
    }
}
