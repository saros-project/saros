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

package de.fu_berlin.inf.dpp.core.project;

import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * A listener for SarosSession life-cycle related events.
 *
 * @author bkahlert
 */
public class AbstractSarosSessionListener implements ISarosSessionListener {

    @Override
    public void postOutgoingInvitationCompleted(IProgressMonitor monitor,
        User user) {

    }

    @Override
    public void sessionStarting(ISarosSession newSarosSession) {
        // do nothing
    }

    @Override
    public void sessionStarted(ISarosSession newSarosSession) {
        // do nothing
    }

    @Override
    public void sessionEnding(ISarosSession oldSarosSession) {
        // do nothing
    }

    @Override
    public void sessionEnded(ISarosSession oldSarosSession) {
        // do nothing
    }

    @Override
    public void projectAdded(String projectID) {
        // do nothing
    }
}
