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

import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SharedProjectListenerDispatch implements ISharedProjectListener {
    protected List<ISharedProjectListener> listeners = new CopyOnWriteArrayList<ISharedProjectListener>();

    @Override
    public void permissionChanged(User user) {
        for (ISharedProjectListener listener : this.listeners) {
            listener.permissionChanged(user);
        }
    }

    @Override
    public void userJoined(User user) {
        for (ISharedProjectListener listener : this.listeners) {
            listener.userJoined(user);
        }
    }

    @Override
    public void userStartedQueuing(User user) {
        for (ISharedProjectListener listener : this.listeners) {
            listener.userStartedQueuing(user);
        }
    }

    @Override
    public void userFinishedProjectNegotiation(User user) {
        for (ISharedProjectListener listener : this.listeners) {
            listener.userFinishedProjectNegotiation(user);
        }
    }

    @Override
    public void userLeft(User user) {
        for (ISharedProjectListener listener : this.listeners) {
            listener.userLeft(user);
        }
    }

    public void add(ISharedProjectListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void remove(ISharedProjectListener listener) {
        this.listeners.remove(listener);
    }
}

