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

package de.fu_berlin.inf.dpp.intellij.ui.views.buttons;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.ui.actions.LeaveSessionAction;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.NullSessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import org.picocontainer.annotations.Inject;

public class LeaveSessionButton extends SimpleButton {

    public static final String LEAVE_SESSION_ICON_PATH = "/icons/famfamfam/session_leave_tsk.png";

    private final ISessionLifecycleListener sessionLifecycleListener = new NullSessionLifecycleListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            setEnabledFromUIThread(true);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession,
            SessionEndReason reason) {

            setEnabledFromUIThread(false);
        }
    };

    @Inject
    private ISarosSessionManager sessionManager;

    /**
     * Creates a LeaveSessionButton and registers the sessionListener.
     * <p/>
     * LeaveSessionButton is created as disabled.
     */
    public LeaveSessionButton() {
        super(new LeaveSessionAction(), "Leave session",
            LEAVE_SESSION_ICON_PATH, "leave");
        SarosPluginContext.initComponent(this);
        sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
        setEnabled(false);
    }
}
