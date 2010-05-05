/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 * (c) Stephan Lau - 2010
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

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.SessionView.SessionViewTableViewer;
import de.fu_berlin.inf.dpp.util.EclipseUtils;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.VideoSharingSession;

/**
 * @author s-lau
 */
@Component(module = "action")
public class VideoSharingAction extends Action {

    private static final Logger log = Logger
        .getLogger(VideoSharingAction.class);

    public static final String ACTION_ID = VideoSharingAction.class.getName();

    public static final String TOOLTIP_START_SESSION = "Share your screen with selected user";
    public static final String TOOLTIP_STOP_SESSION = "Stop session with user ";

    protected VideoSharing videoSharing;
    protected SessionManager sessionManager;

    protected User selectedUser = null;

    public VideoSharingAction(SessionViewTableViewer sessionTable,
        VideoSharing videoSharing, SessionManager sessionManager) {
        setId(ACTION_ID);

        this.videoSharing = videoSharing;
        this.sessionManager = sessionManager;

        sessionTable
            .addSelectionChangedListener(new ISelectionChangedListener() {

                public void selectionChanged(SelectionChangedEvent event) {
                    ISelection selection = event.getSelection();

                    if (selection instanceof StructuredSelection) {
                        StructuredSelection users = (StructuredSelection) selection;
                        if (users.size() == 1)
                            selectedUser = (User) users.getFirstElement();

                        updateState();
                    }
                }
            });

        videoSharing.getSession().add(
            new ValueChangeListener<VideoSharingSession>() {

                public void setValue(VideoSharingSession newValue) {
                    updateState();
                }
            });

        updateState();

    }

    protected void updateState() {
        VideoSharingSession videoSharingSession = videoSharing.getSession()
            .getValue();

        if (videoSharingSession != null) {
            setToolTipText(TOOLTIP_STOP_SESSION
                + videoSharingSession.getRemoteUser());
            setImageDescriptor(SarosUI
                .getImageDescriptor("icons/monitor_stop.png"));
            setEnabled(true);
        } else {
            setToolTipText(TOOLTIP_START_SESSION);
            setImageDescriptor(SarosUI
                .getImageDescriptor("icons/monitor_go.png"));
            setEnabled(selectedUser != null);
        }
    }

    @Override
    public void run() {
        Util.runSafeAsync(log, new Runnable() {
            public void run() {
                VideoSharingSession videoSharingSession = videoSharing
                    .getSession().getValue();
                if (videoSharingSession == null && videoSharing.ready()) {
                    if (selectedUser != null)
                        try {
                            videoSharing.startSharing(selectedUser);
                        } catch (final SarosCancellationException e) {
                            Util.runSafeSWTAsync(log, new Runnable() {
                                public void run() {
                                    EclipseUtils.openInformationMessageDialog(
                                        EditorAPI.getShell(),
                                        "Remote user rejected", e.getMessage());
                                }
                            });

                            log.error("Could not establish screensharing: ", e);
                        }
                } else {
                    if (videoSharingSession != null) {
                        switch (videoSharingSession.getMode()) {
                        case LOCAL:
                        case HOST: //$FALL-THROUGH$
                            videoSharingSession.dispose();
                            break;
                        case CLIENT:
                            videoSharingSession.requestStop();
                            break;
                        }
                        videoSharingSession.dispose();
                    }
                }
                updateState();
            }
        });
    }

}
