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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Decorates Shared Project files.
 * 
 * TODO CO SharedProjectFileDecorator does support multiple users but the
 * awareness shows all users with {@link Permission#WRITE_ACCESS} and the person
 * followed which is kind of confusing.
 * 
 * @see ILightweightLabelDecorator *
 */
@Component(module = "integration")
public class SharedProjectFileDecorator implements ILightweightLabelDecorator {

    private static final Logger log = Logger
        .getLogger(SharedProjectFileDecorator.class.getName());

    public final ImageDescriptor activeDescriptor = ImageManager
        .getImageDescriptor("icons/ovr16/activeproject_obj.png"); // NON-NLS-1

    public final ImageDescriptor passiveDescriptor = ImageManager
        .getImageDescriptor("icons/ovr16/passiveproject_obj.png"); // NON-NLS-1

    private AtomicReference<ISarosSession> sarosSession = new AtomicReference<ISarosSession>();

    private Set<Object> decoratedElements;

    private List<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();

    /**
     * SharedProjectListener responsible for triggering an update on the
     * decorations if there is a {@link Permission} change.
     */
    protected ISharedProjectListener projectListener = new AbstractSharedProjectListener() {

        @Override
        public void permissionChanged(User user) {
            updateDecorations(user);
        }
    };

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            sarosSession.set(newSarosSession);
            newSarosSession.addListener(projectListener);

            if (!decoratedElements.isEmpty()) {
                log.warn("Set of files to decorate not empty on session start. "
                    + decoratedElements.toString());
                // update remaining files
                updateDecoratorsAsync(decoratedElements.toArray());
            }
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            sarosSession.set(null);
            oldSarosSession.removeListener(projectListener);
            // Update all
            updateDecoratorsAsync(decoratedElements.toArray());
        }

        @Override
        public void projectAdded(String projectID) {
            updateDecoratorsAsync(decoratedElements.toArray());
        }
    };

    protected ISharedEditorListener editorListener = new AbstractSharedEditorListener() {

        Map<User, SPath> lastActiveOpenEditors = new HashMap<User, SPath>();

        @Override
        public void activeEditorChanged(User user, SPath path) {
            Set<IResource> resources = new HashSet<IResource>();

            log.trace("User: " + user + " activated an editor -> " + path);

            if (path == null) {
                log.warn("path object should not be null");
                return;
            }

            if (sarosSession.get() == null)
                return;

            SPath lastActiveEditor = lastActiveOpenEditors.get(user);

            if (lastActiveEditor != null)
                addResourceAndParents(resources, lastActiveEditor.getResource());

            lastActiveOpenEditors.put(user, path);
            addResourceAndParents(resources, path.getResource());
            updateDecoratorsAsync(resources.toArray());
        }

        @Override
        public void editorRemoved(User user, SPath path) {
            Set<IResource> resources = new HashSet<IResource>();

            log.trace("User: " + user + " closed an editor -> " + path);

            if (path == null) {
                log.warn("path object should not be null");
                return;
            }

            if (sarosSession.get() == null)
                return;

            lastActiveOpenEditors.put(user, null);
            addResourceAndParents(resources, path.getResource());
            updateDecoratorsAsync(resources.toArray());

        }

        @Override
        public void followModeChanged(User user, boolean isFollowed) {
            updateDecorations(user);
        }
    };

    @Inject
    private EditorManager editorManager;

    @Inject
    private ISarosSessionManager sessionManager;

    public SharedProjectFileDecorator() {

        SarosPluginContext.initComponent(this);

        this.decoratedElements = new HashSet<Object>();

        sessionManager.addSarosSessionListener(sessionListener);

        editorManager.addSharedEditorListener(editorListener);

        if (sessionManager.getSarosSession() != null)
            sessionListener.sessionStarted(sessionManager.getSarosSession());
    }

    private void updateDecorations(User user) {

        Set<IResource> resources = new HashSet<IResource>();

        if (sarosSession.get() == null)
            return;

        for (SPath path : editorManager.getRemoteOpenEditors(user))
            addResourceAndParents(resources, path.getResource());

        updateDecoratorsAsync(resources.toArray());
    }

    public void decorate(Object element, IDecoration decoration) {
        if (decorateInternal(element, decoration)) {
            decoratedElements.add(element);
        } else {
            decoratedElements.remove(element);
        }
    }

    private void addResourceAndParents(Set<IResource> resources,
        IResource resource) {

        if (resource == null) {
            log.warn("resource should not be null");
            return;
        }

        resources.add(resource);
        IResource parent = resource.getParent();

        while (parent != null) {
            resources.add(parent);
            parent = parent.getParent();
        }
    }

    private boolean decorateInternal(Object element, IDecoration decoration) {
        try {

            ISarosSession session = sarosSession.get();
            if (session == null)
                return false;

            IResource resource = (IResource) element;

            if (!session.isShared(resource))
                return false;

            Set<SPath> openRemoteEditors = editorManager.getRemoteOpenEditors();

            // TODO refactor the body of these to loops to a method
            for (SPath path : openRemoteEditors) {
                if (!resource.getFullPath().isPrefixOf(path.getFullPath()))
                    continue;

                if (!path.getFile().exists())
                    continue;

                if (containsUserToDisplay(editorManager
                    .getRemoteActiveEditorUsers(path))) {
                    log.trace("Active Deco: " + element);
                    decoration.addOverlay(activeDescriptor,
                        IDecoration.TOP_RIGHT);
                    return true;
                }
            }

            for (SPath path : openRemoteEditors) {
                if (!resource.getFullPath().isPrefixOf(path.getFullPath()))
                    continue;

                if (!path.getFile().exists())
                    continue;

                if (containsUserToDisplay(editorManager
                    .getRemoteOpenEditorUsers(path))) {
                    log.trace("Passive Deco: " + element);
                    decoration.addOverlay(passiveDescriptor,
                        IDecoration.TOP_RIGHT);
                    return true;
                }
            }

            log.trace("No Deco: " + element);

        } catch (RuntimeException e) {
            log.error("Internal Error in SharedProjectFileDecorator:", e);
        }
        return false;
    }

    private boolean containsUserToDisplay(List<User> activeUsers) {

        for (User user : activeUsers) {
            if (user.hasWriteAccess()
                || user.equals(editorManager.getFollowedUser())) {
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
        sessionManager.removeSarosSessionListener(sessionListener);
        editorManager.removeSharedEditorListener(editorListener);
        // TODO clean up better
        this.sarosSession = null;
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    protected void updateDecoratorsAsync(final Object[] updateElements) {

        Utils.runSafeSWTAsync(log, new Runnable() {
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