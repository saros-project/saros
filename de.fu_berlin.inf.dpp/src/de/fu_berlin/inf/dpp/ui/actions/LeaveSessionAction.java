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
package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.NullSessionLifecycleListener;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;

/**
 * Leaves the current Saros session. Is deactivated if there is no running
 * session.
 * 
 * @author rdjemili
 * @author oezbek
 */
@Component(module = "action")
public class LeaveSessionAction extends Action implements Disposable {

    public static final String ACTION_ID = LeaveSessionAction.class.getName();

    @Inject
    private ISarosSessionManager sessionManager;

    private final ISessionLifecycleListener sessionLifecycleListener = new NullSessionLifecycleListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            updateEnablement();
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            updateEnablement();
        }
    };

    public LeaveSessionAction() {
        setId(ACTION_ID);
        setToolTipText(Messages.LeaveSessionAction_leave_session_tooltip);
        setImageDescriptor(new ImageDescriptor() {
            @Override
            public ImageData getImageData() {
                return ImageManager.ELCL_PROJECT_SHARE_LEAVE.getImageData();
            }
        });

        SarosPluginContext.initComponent(this);
        sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
        updateEnablement();
    }

    @Override
    public void run() {
        CollaborationUtils.leaveSession();
    }

    @Override
    public void dispose() {
        sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);
    }

    private void updateEnablement() {
        ISarosSession session = sessionManager.getSarosSession();

        if (session == null) {
            setEnabled(false);
            return;
        }

        if (session.isHost()) {
            setToolTipText(Messages.LeaveSessionAction_stop_session_tooltip);
            setImageDescriptor(new ImageDescriptor() {
                @Override
                public ImageData getImageData() {
                    return ImageManager.ELCL_PROJECT_SHARE_TERMINATE
                        .getImageData();
                }
            });
        } else {
            setToolTipText(Messages.LeaveSessionAction_leave_session_tooltip);
            setImageDescriptor(new ImageDescriptor() {
                @Override
                public ImageData getImageData() {
                    return ImageManager.ELCL_PROJECT_SHARE_LEAVE.getImageData();
                }
            });
        }
        setEnabled(true);
    }
}
