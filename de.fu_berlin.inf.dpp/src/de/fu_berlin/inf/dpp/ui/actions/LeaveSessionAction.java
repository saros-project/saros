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
package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;

/**
 * Leaves the current Saros session. Is deactivated if there is no running
 * session.
 * 
 * @author rdjemili
 * @author oezbek
 */
@Component(module = "action")
public class LeaveSessionAction extends Action {

    @Inject
    protected SarosSessionManager sessionManager;

    public LeaveSessionAction() {
        setToolTipText("Leave Session");
        setImageDescriptor(new ImageDescriptor() {
            @Override
            public ImageData getImageData() {
                return ImageManager.ELCL_PROJECT_SHARE_LEAVE.getImageData();
            }
        });

        SarosPluginContext.initComponent(this);

        sessionManager
            .addSarosSessionListener(new AbstractSarosSessionListener() {
                @Override
                public void sessionStarted(ISarosSession newSarosSession) {
                    updateEnablement();
                }

                @Override
                public void sessionEnded(ISarosSession oldSarosSession) {
                    updateEnablement();
                }
            });

        updateEnablement();
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        CollaborationUtils.leaveSession(sessionManager);
    }

    protected void updateEnablement() {
        if (sessionManager.getSarosSession() != null) {
            if (sessionManager.getSarosSession().isHost()) {
                setToolTipText("Stop Session...");
                setImageDescriptor(new ImageDescriptor() {
                    @Override
                    public ImageData getImageData() {
                        return ImageManager.ELCL_PROJECT_SHARE_TERMINATE
                            .getImageData();
                    }
                });
            } else {
                setToolTipText("Leave Session...");
                setImageDescriptor(new ImageDescriptor() {
                    @Override
                    public ImageData getImageData() {
                        return ImageManager.ELCL_PROJECT_SHARE_LEAVE
                            .getImageData();
                    }
                });
            }
            setEnabled(true);
        } else {
            setEnabled(false);
        }

    }

}
