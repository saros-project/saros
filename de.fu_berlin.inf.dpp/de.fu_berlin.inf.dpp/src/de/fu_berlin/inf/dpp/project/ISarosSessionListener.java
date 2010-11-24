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
package de.fu_berlin.inf.dpp.project;

/**
 * A listener for SarosSession life-cycle related events.
 * 
 * @author rdjemili
 * @author bkahlert
 */
public interface ISarosSessionListener {

    /**
     * Is fired when a new session is about to start.
     * 
     * @param newSarosSession
     *            the session that is created. Is never <code>null</code>.
     * 
     */
    public void sessionStarting(ISarosSession newSarosSession);

    /**
     * Is fired when a new session started.
     * 
     * @param newSarosSession
     *            the session that has been created. Is never <code>null</code>.
     * 
     */
    public void sessionStarted(ISarosSession newSarosSession);

    /**
     * Is fired when a session is about to be ended. Reasons for this can be
     * that the session was closed or that the user left by himself.
     * 
     * @param oldSarosSession
     *            the session that has just been left. Is never
     *            <code>null</code>.
     */
    public void sessionEnding(ISarosSession oldSarosSession);

    /**
     * Is fired when a session ended. Reasons for this can be that the session
     * was closed or that the user left by himself.
     * 
     * @param oldSarosSession
     *            the session that has just been left. Is never
     *            <code>null</code>.
     */
    public void sessionEnded(ISarosSession oldSarosSession);
}
