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
package de.fu_berlin.inf.dpp.activities.business;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * An interface for all things that occur in a shared project session such as
 * editing a file, opening or closing editors, changing permissions, etc.
 * 
 * All {@link IActivity}s should be implemented using the value pattern, i.e.
 * created {@link IActivity} should be immutable.
 * 
 * {@link IActivity} are supposed to be "smart" and know about users and
 * session.
 * 
 * @valueObject All IActivity subclasses should be Value Objects, i.e. they
 *              should be immutable
 */
public interface IActivity {

    /**
     * Returns the the user which has caused this activity.
     */
    public User getSource();

    /**
     * The activity will call the receive method of the given receiver with the
     * actual type of this IActivity.
     * 
     * For instance if dispatch is called on a FolderActivity it will call
     * {@link IActivityReceiver#receive(FolderActivity)}
     */
    public void dispatch(IActivityReceiver receiver);

    /**
     * @return a "stupid" IActivityDataObject which can be used to send this
     *         {@link IActivity} to peers.
     */
    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession);

}
