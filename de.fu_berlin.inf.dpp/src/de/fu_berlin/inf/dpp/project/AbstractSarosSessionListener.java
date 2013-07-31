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
package de.fu_berlin.inf.dpp.project;

import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.inf.dpp.User;

/**
 * A listener for SarosSession life-cycle related events.
 * 
 * @author bkahlert
 */
public class AbstractSarosSessionListener implements ISarosSessionListener {

    @Override
    public void preIncomingInvitationCompleted(IProgressMonitor monitor) {
        // do nothing
    }

    @Override
    public void postOutgoingInvitationCompleted(IProgressMonitor monitor,
        User user) {
        // do nothing
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
