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
package de.fu_berlin.inf.dpp.listeners;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.xmpp.JID;

/**
 * Listens for events that can happen inside a SharedProject session.
 * 
 * @author rdjemili
 */
public interface ISharedProjectListener {
    
    /**
     * The driver role of one participant has changed.
     * 
     * @param driver the new driver.
     * 
     * @param replicated <code>false</code> if this event was created by this
     * client. <code>true</code> if it was created by another client and only
     * replicated to this client.
     */
    public void driverChanged(JID driver, boolean replicated);
    
    /**
     * The resource that the driver is currently editting has changed.
     * 
     * @param path the project-relative path of the resource that is the new
     * driver resource.
     * 
     * @param replicated <code>false</code> if this event was created by this
     * client. <code>true</code> if it was created by another client and only
     * replicated to this client.
     */
    public void driverPathChanged(IPath path, boolean replicated);
    
    /**
     * @param user the user that has joined.
     */
    public void userJoined(JID user);
    
    /**
     * @param user the user that has left.
     */
    public void userLeft(JID user);
}
