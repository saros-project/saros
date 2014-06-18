/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.core.project.internal;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.session.User;

import java.util.List;

/**
 * Callback interface used by the {@link ActivityHandler} to notify the logic
 * that an activity can now be sent or executed. The implementing class is
 * responsible for proper thread synchronization as the callback methods may be
 * called by multiple threads simultaneously.
 *
 * @author Stefan Rossbach
 */
public interface IActivityHandlerCallback {

    /**
     * Gets called when an activity should be send to several session users.
     *
     * @param recipients a list containing the users that should receive the activity
     * @param activity   the activity to send
     */
    public void send(List<User> recipients, IActivity activity);

    /**
     * Gets called when an activity should be executed.
     *
     * @param activity the activity to execute
     */
    public void execute(IActivity activity);
}

