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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Decorates Shared Project files.
 * 
 * TODO CO SharedProjectFileDecorator does support multiple users but the
 * awareness shows all drivers and the person followed which is kind of
 * confusing.
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
            updateDecoratorsAsync(new Object[] { sharedProject.getProject() });
        }
    };

    protected ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(ISharedProject project) {
            sharedProject = project;
            project.addListener(projectListener);
            // Update all
            updateDecoratorsAsync(new Object[] { project.getProject() });
        }

        @Override
        public void sessionEnded(ISharedProject project) {
            assert sharedProject == project;
            sharedProject = null;
            project.removeListener(projectListener);
            // Update all
            updateDecoratorsAsync(new Object[] { project.getProject() });

        }
    };

    protected ISharedEditorListener editorListener = new ISharedEditorListener() {

        Map<User, IFile> oldActiveEditors = new HashMap<User, IFile>();

        public void activeEditorChanged(User user, IPath path) {
            try {
                List<IFile> paths = new LinkedList<IFile>();

                IFile oldActiveEditor = oldActiveEditors.get(user);
                if (oldActiveEditor != null) {
                    paths.add(oldActiveEditor);
                }

                IFile newFile = null;
                if (path != null && sharedProject != null) {
                    newFile = sharedProject.getProject().getFile(path);
                    if (newFile != null && !newFile.equals(oldActiveEditor)) {
                        paths.add(newFile);
                    }
                }
                oldActiveEditors.put(user, newFile);
                updateDecoratorsAsync(paths.toArray());

            } catch (RuntimeException e) {
                log.error("Internal Error in SharedProjectFileDecorator:", e);
            }
        }

        public void editorRemoved(User user, IPath path) {
            try {
                if (path != null && sharedProject != null) {
                    IFile newFile = sharedProject.getProject().getFile(path);
                    IFile oldActiveEditor = oldActiveEditors.get(user);
                    if (newFile != null) {
                        if (newFile.equals(oldActiveEditor)) {
                            oldActiveEditors.put(user, null);
                        }
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

        public void followModeChanged(User user) {
            // ignore
        }
    };

    @Inject
    protected Saros saros;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected ISessionManager sessionManager;

    public SharedProjectFileDecorator() {

        Saros.getDefault().reinject(this);

        sessionManager.addSessionListener(sessionListener);

        editorManager.addSharedEditorListener(editorListener);
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

            if (containsUserToDisplay(editorManager
                .getRemoteActiveEditorUsers(path))) {
                log.trace("Active Deco: " + element);
                decoration.addOverlay(activeDescriptor, IDecoration.TOP_LEFT);
            } else if (containsUserToDisplay(editorManager
                .getRemoteOpenEditorUsers(path))) {
                log.trace("Passive Deco: " + element);
                decoration.addOverlay(passiveDescriptor, IDecoration.TOP_LEFT);
            } else {
                log.trace("No Deco: " + element);
            }

        } catch (RuntimeException e) {
            log.error("Internal Error in SharedProjectFileDecorator:", e);
        }
    }

    private boolean containsUserToDisplay(List<User> activeUsers) {

        for (User user : activeUsers) {
            if (user.isDriver() || user.equals(editorManager.getFollowedUser())) {
                return true;
            }
        }
        return false;
    }

    public void addListener(ILabelProviderListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(ILabelProviderListener listener) {
        this.listeners.remove(listener);
    }

    public void dispose() {
        sessionManager.removeSessionListener(sessionListener);
        editorManager.removeSharedEditorListener(editorListener);
        // TODO clean up better
        this.sharedProject = null;
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    protected void updateDecoratorsAsync(final Object[] updateElements) {

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                LabelProviderChangedEvent event = new LabelProviderChangedEvent(
                    SharedProjectFileDecorator.this, updateElements);

                for (ILabelProviderListener listener : SharedProjectFileDecorator.this.listeners) {
                    listener.labelProviderChanged(event);
                }
            }
        });
    }
}