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
package de.fu_berlin.inf.dpp.ui.decorators;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;

/**
 * Decorates Shared Project files.
 * 
 * TODO CO EditorManager does not support multiple drivers correctly, thus the
 * LabelDecorator is often wrong.
 * 
 * @see ILightweightLabelDecorator
 * 
 */
public class SharedProjectFileDecorator implements ILightweightLabelDecorator {

    private static final Logger log = Logger
        .getLogger(SharedProjectFileDecorator.class.getName());

    public final ImageDescriptor activeDescriptor = SarosUI
        .getImageDescriptor("icons/bullet_green.png"); // NON-NLS-1

    public final ImageDescriptor passiveDescriptor = SarosUI
        .getImageDescriptor("icons/bullet_yellow.png"); // NON-NLS-1

    protected ISharedProject sharedProject;

    protected List<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();

    protected ISharedProjectListener projectListener = new AbstractSharedProjectListener() {

        @Override
        public void roleChanged(User user, boolean replicated) {
            updateDecoratorsAsync(null);
        }
    };

    protected ISessionListener sessionListener = new ISessionListener() {

        public void sessionStarted(ISharedProject session) {
            sharedProject = session;
            session.addListener(projectListener);
            updateDecoratorsAsync(null);
        }

        public void sessionEnded(ISharedProject session) {
            sharedProject.removeListener(projectListener);
            // Update all
            updateDecoratorsAsync(null);
            sharedProject = null;
        }

        public void invitationReceived(IIncomingInvitationProcess process) {
            // ignore
        }
    };

    protected ISharedEditorListener editorListener = new ISharedEditorListener() {

        IFile oldActiveDriverEditor = null;

        public void activeDriverEditorChanged(IPath path, boolean replicated) {
            try {
                List<IFile> paths = new LinkedList<IFile>();

                if (oldActiveDriverEditor != null) {
                    paths.add(oldActiveDriverEditor);
                }
                if (path != null && sharedProject != null) {
                    IFile newFile = sharedProject.getProject().getFile(path);
                    if (newFile != null
                        && !newFile.equals(oldActiveDriverEditor)) {
                        paths.add(newFile);
                    }
                    oldActiveDriverEditor = newFile;
                }
                updateDecoratorsAsync(paths.toArray());

            } catch (RuntimeException e) {
                log.error("Internal Error in SharedProjectFileDecorator:", e);
            }

        }

        public void driverEditorRemoved(IPath path, boolean replicated) {
            try {
                if (path != null && sharedProject != null) {
                    IFile newFile = sharedProject.getProject().getFile(path);
                    if (newFile != null
                        && newFile.equals(oldActiveDriverEditor)) {
                        oldActiveDriverEditor = null;
                    }
                    if (newFile != null) {
                        updateDecoratorsAsync(new Object[] { newFile });
                    }
                }
            } catch (RuntimeException e) {
                log.error("Internal Error in SharedProjectFileDecorator:", e);
            }

        }

        public void driverEditorSaved(IPath path, boolean replicated) {
            // ignore
        }

        public void followModeChanged(boolean enabled) {
            // ignore
        }
    };

    public SharedProjectFileDecorator() {
        ISessionManager sessionManager = Saros.getDefault().getSessionManager();
        sessionManager.addSessionListener(sessionListener);

        EditorManager.getDefault().addSharedEditorListener(editorListener);
        if (sessionManager.getSharedProject() != null) {
            sessionListener.sessionStarted(sessionManager.getSharedProject());
        }
    }

    public void decorate(Object element, IDecoration decoration) {

        try {
            if (this.sharedProject == null)
                return;

            // Enablement in the Plugin.xml ensures that we only get IFiles
            if (!(element instanceof IFile))
                return;

            IFile file = (IFile) element;
            if (!this.sharedProject.getProject().equals(file.getProject())) {
                return;
            }
            IPath path = file.getProjectRelativePath();
            if (path == null)
                return;

            EditorManager editorManager = EditorManager.getDefault();
            if (path.equals(editorManager.getActiveDriverEditor())) {
                log.trace("Active Deco: " + element);
                decoration.addOverlay(activeDescriptor, IDecoration.TOP_LEFT);
            } else if (editorManager.getDriverEditors().contains(path)) {
                log.trace("Passive Deco: " + element);
                decoration.addOverlay(passiveDescriptor, IDecoration.TOP_LEFT);
            } else {
                log.trace("No Deco: " + element);
            }

        } catch (RuntimeException e) {
            log.error("Internal Error in SharedProjectFileDecorator:", e);
        }
    }

    public void addListener(ILabelProviderListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(ILabelProviderListener listener) {
        this.listeners.remove(listener);
    }

    public void dispose() {
        Saros.getDefault().getSessionManager().removeSessionListener(
            sessionListener);
        EditorManager.getDefault().removeSharedEditorListener(editorListener);
        this.sharedProject = null;
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    protected void updateDecoratorsAsync(final Object[] updateElements) {

        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                try {
                    LabelProviderChangedEvent event = new LabelProviderChangedEvent(
                        SharedProjectFileDecorator.this, updateElements);

                    for (ILabelProviderListener listener : SharedProjectFileDecorator.this.listeners) {
                        listener.labelProviderChanged(event);
                    }
                } catch (RuntimeException e) {
                    log.error("Internal Error in SharedProjectFileDecorator:",
                        e);
                }
            }
        });
    }
}