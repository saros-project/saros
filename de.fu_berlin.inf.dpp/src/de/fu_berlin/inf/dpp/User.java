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

import de.fu_berlin.inf.dpp.net.JID;

public class User {
    public enum UserConnectionState {
        UNKNOWN, ONLINE, OFFLINE
    }

    public enum UserRole {
        DRIVER, OBSERVER
    }

    private UserConnectionState presence = UserConnectionState.UNKNOWN;

    private final JID jid;

    private int colorID;

    /**
     * Time stamp when User became offline the last time. In seconds.
     */
    private long offlineTime = 0;

    private UserRole role = UserRole.OBSERVER;

    public User(JID jid, int colorID) {
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
        if (obj instanceof User) {
            User other = (User) obj;
            return this.jid.equals(other.jid);
        }

        return false;
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
}
