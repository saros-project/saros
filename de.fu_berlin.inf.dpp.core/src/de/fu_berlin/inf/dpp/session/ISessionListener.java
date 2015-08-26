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
package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.session.User.Permission;

/**
 * Listens for events that can happen during a {@link ISarosSession session}.
 * For life-cycle events like the start and end of a session, use
 * {@link ISessionLifecycleListener}.
 */
public interface ISessionListener {
    /**
     * TODO add some easy way to check if given user with
     * {@link Permission#WRITE_ACCESS} is the client (you)
     */

    /**
     * The user {@link Permission} of the given participant has been changed.
     * This is called after the {@link Permission} of the user has been updated
     * to represent the new state.
     * 
     * @param user
     *            the user whose {@link Permission} changed.
     */
    public void permissionChanged(User user);

    /**
     * Is fired when an user joins the shared project.
     * 
     * @param user
     *            the user that has joined.
     */
    public void userJoined(User user);

    /**
     * Is fired when a user started queuing and is now able to process all
     * Activities
     * 
     * @param user
     *            the user that has joined.
     */
    public void userStartedQueuing(User user);

    /**
     * Is fired when a finished the Project Negotiation
     * 
     * @param user
     *            the user that has joined.
     */
    public void userFinishedProjectNegotiation(User user);

    /**
     * Is fired when the color assigned to a user in the session changed.
     * 
     * @param user
     *            the user whose color changed
     */
    public void userColorChanged(User user);

    /**
     * Is fired when an user leaves the shared project.
     * 
     * @param user
     *            the user that has left.
     */
    public void userLeft(User user);
}
