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

import de.fu_berlin.inf.dpp.net.JID;


public class User {
	public enum UserConnectionState {UNKNOWN,ONLINE,OFFLINE};
	public enum UserRole {DRIVER,OBSERVER};
	private UserConnectionState presence = UserConnectionState.UNKNOWN;
	
	private JID jid;
	private int colorid=0;
	private long offlineTime=0;
	private UserRole role = UserRole.OBSERVER;

	public User(JID jid) {
		this.jid = jid;
	}

	public JID getJid() {
		return jid;
	}

	/**
	 * set the current user role of this user inside the
	 * current project.
	 * @param role (Driver, Observer)
	 */
	public void setUserRole(UserRole role){
		this.role = role;
	}
	
	/**
	 * Gets current project role of this user.
	 * @return role (Driver, Observer)
	 */
	public UserRole getUserRole(){
		return this.role;
	}

	@Override
	public String toString() {
		return jid.getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof User) {
			User other = (User) obj;
			return jid.equals(other.jid);
		}

		return false;
	}
	
	public int getColorID(){
		return colorid;
	}
	
	public void setColorID(int c) {
		colorid=c;
	}
	
	
	public UserConnectionState getPresence() {
		return presence;
	}
	
	public void setPresence(UserConnectionState p) {
		presence=p;
		if (presence==User.UserConnectionState.OFFLINE)
			offlineTime = (new java.util.Date().getTime() ) ;
	}
	public int getOfflineSecs() {
		return (int)(( (new java.util.Date().getTime() ) - offlineTime) / 1000) ;
	}

}
