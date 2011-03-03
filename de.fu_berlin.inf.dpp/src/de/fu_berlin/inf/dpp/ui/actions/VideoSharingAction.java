/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universit�t Berlin - Fachbereich Mathematik und Informatik - 2010
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
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.observables.VideoSessionObservable;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.SessionView.SessionViewTableViewer;
import de.fu_berlin.inf.dpp.util.Utils;
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

    protected static final String ACTION_ID = VideoSharingAction.class
        .getName();
    public static final String TOOLTIP_START_SESSION = "Share your screen with selected buddy";
    public static final String TOOLTIP_STOP_SESSION = "Stop session with buddy";

    @Inject
    protected VideoSharing videoSharing;
    @Inject
    protected VideoSessionObservable sessionObservable;

    protected SessionViewTableViewer viewer;
    protected User selectedUser = null;

    public VideoSharingAction(SessionViewTableViewer viewer) {
        super();
        SarosPluginContext.initComponent(this);
        setId(ACTION_ID);
        changeButton();
        setEnabled(false);
        this.viewer = viewer;
        viewer.addSelectionChangedListener(selectionListener);
        sessionObservable.add(changeListener);
    }

    protected ValueChangeListener<VideoSharingSession> changeListener = new ValueChangeListener<VideoSharingSession>() {
        public void setValue(VideoSharingSession newValue) {
            changeButton();
        }
    };

    protected ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
            ISelection selection = event.getSelection();
            if (selection instanceof StructuredSelection) {
                StructuredSelection users = (StructuredSelection) selection;
                if (users.isEmpty())
                    selectedUser = null;
                if (users.size() == 1)
                    selectedUser = (User) users.getFirstElement();
                setEnabled(shouldEnable());
            }

        }
    };

    protected boolean shouldEnable() {
        return (selectedUser != null && !selectedUser.isLocal());
    }

    @Override
    public void run() {
        Utils.runSafeAsync(log, new Runnable() {
            public void run() {
                VideoSharingSession videoSharingSession = sessionObservable
                    .getValue();
                if (videoSharing.ready()) {
                    try {
                        setEnabled(false);
                        videoSharing.startSharing(selectedUser);
                    } catch (final SarosCancellationException e) {
                        Utils.popUpFailureMessage(
                            "Could not establish screensharing",
                            e.getMessage(), false);
                        log.error("Could not establish screensharing: ", e);
                    }
                } else {
                    switch (videoSharingSession.getMode()) {
                    case LOCAL:
                    case HOST: // $FALL-THROUGH$
                        break;
                    case CLIENT:
                        videoSharingSession.requestStop();
                        break;
                    }
                    videoSharingSession.dispose();
                }
                setEnabled(shouldEnable());
                changeButton();
            }
        });
    }

    protected void changeButton() {
        if (sessionObservable.getValue() != null) {
            setImageDescriptor(ImageManager
                .getImageDescriptor("icons/elcl16/stopvideo.png"));
            setToolTipText(TOOLTIP_STOP_SESSION);
            return;
        }
        setImageDescriptor(ImageManager
            .getImageDescriptor("icons/elcl16/startvideo.png"));
        setToolTipText(TOOLTIP_START_SESSION);
    }

}
