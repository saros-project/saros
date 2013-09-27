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
package de.fu_berlin.inf.dpp.ui.decorators;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.editor.RemoteEditorManager;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;

/**
 * Decorates project files and their parent folders belonging to the active
 * editors of the remote users.
 * 
 * @see ILightweightLabelDecorator
 */
@Component(module = "eclipse")
// TODO rename this class, requires change in the plugin.xml too
public class SharedProjectFileDecorator implements ILightweightLabelDecorator {

    private static final Logger LOG = Logger
        .getLogger(SharedProjectFileDecorator.class.getName());

    private static final String IMAGE_PATH = "icons/ovr16/dot.png"; //$NON-NLS-1$

    private final AtomicReference<ISarosSession> sarosSession = new AtomicReference<ISarosSession>();

    private final Set<Object> decoratedElements;

    private final List<ILabelProviderListener> listeners = new CopyOnWriteArrayList<ILabelProviderListener>();

    // add +1 for default color
    private final MemoryImageDescriptor[] imageDescriptors = new MemoryImageDescriptor[SarosSession.MAX_USERCOLORS + 1];

    /** default image descriptor index pointing to a neutral color */
    private final int defaultImageDescriptorIndex = imageDescriptors.length - 1;

    @Inject
    private EditorManager editorManager;

    @Inject
    private ISarosSessionManager sessionManager;

    private static class MemoryImageDescriptor extends ImageDescriptor {

        private final ImageData data;

        public MemoryImageDescriptor(ImageData data) {
            this.data = data;
        }

        @Override
        public ImageData getImageData() {
            return data;
        }
    }

    private ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            sarosSession.set(newSarosSession);

            if (!decoratedElements.isEmpty()) {
                LOG.warn("Set of files to decorate not empty on session start. "
                    + decoratedElements.toString());
                // update remaining files
                updateDecoratorsAsync(decoratedElements.toArray());
            }
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            sarosSession.set(null);
            // Update all
            updateDecoratorsAsync(decoratedElements.toArray());
        }

        @Override
        public void projectAdded(String projectID) {
            updateDecoratorsAsync(decoratedElements.toArray());
        }
    };

    private ISharedEditorListener editorListener = new AbstractSharedEditorListener() {

        Map<User, SPath> lastActiveOpenEditors = new HashMap<User, SPath>();

        @Override
        public void activeEditorChanged(User user, SPath path) {
            Set<IResource> resources = new HashSet<IResource>();

            LOG.trace("User: " + user + " activated an editor -> " + path);

            if (path == null) {
                LOG.warn("path object should not be null");
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

            LOG.trace("User: " + user + " closed an editor -> " + path);

            if (path == null) {
                LOG.warn("path object should not be null");
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

        @Override
        public void colorChanged() {
            updateDecoratorsAsync(decoratedElements.toArray());
        }
    };

    public SharedProjectFileDecorator() {

        SarosPluginContext.initComponent(this);

        decoratedElements = new HashSet<Object>();

        sessionManager.addSarosSessionListener(sessionListener);

        editorManager.addSharedEditorListener(editorListener);

        if (sessionManager.getSarosSession() != null)
            sessionListener.sessionStarted(sessionManager.getSarosSession());

        Image tintImage = ImageManager.getImage(IMAGE_PATH);

        for (int i = 0; i <= SarosSession.MAX_USERCOLORS; i++) {
            Color tintColor = SarosAnnotation.getUserColor(i);
            Image tintedImage = tintImage(tintImage, tintColor);
            imageDescriptors[i] = new MemoryImageDescriptor(
                tintedImage.getImageData());
            tintColor.dispose();
            tintedImage.dispose();
        }

        tintImage.dispose();
    }

    private void updateDecorations(User user) {

        Set<IResource> resources = new HashSet<IResource>();

        if (sarosSession.get() == null)
            return;

        for (SPath path : editorManager.getRemoteOpenEditors(user))
            addResourceAndParents(resources, path.getResource());

        updateDecoratorsAsync(resources.toArray());
    }

    @Override
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
            LOG.warn("resource should not be null");
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

            Set<SPath> activeRemoteEditors;

            /*
             * this may cause a NPE although it would not matter because this
             * only happens when the session already ended and so this method is
             * 1. be called again even if the NPE happens 2. returns false in
             * case of an exception
             */
            // openRemoteEditors = editorManager.getRemoteOpenEditors();

            RemoteEditorManager remoteEditorManager = editorManager
                .getRemoteEditorManager();

            if (remoteEditorManager == null)
                activeRemoteEditors = new HashSet<SPath>();
            else
                activeRemoteEditors = remoteEditorManager
                    .getRemoteActiveEditors();

            int imageDescriptorIndex = -1;

            for (SPath activeRemoteEditor : activeRemoteEditors) {

                if (!resource.getFullPath().isPrefixOf(
                    activeRemoteEditor.getFullPath()))
                    continue;

                if (!activeRemoteEditor.getFile().exists())
                    continue;

                int currentImageDescriptorIndex = getImageDescriptorIndex(editorManager
                    .getRemoteActiveEditorUsers(activeRemoteEditor));

                if (imageDescriptorIndex == -1)
                    imageDescriptorIndex = currentImageDescriptorIndex;

                if (currentImageDescriptorIndex != imageDescriptorIndex)
                    imageDescriptorIndex = defaultImageDescriptorIndex;

                if (imageDescriptorIndex == defaultImageDescriptorIndex)
                    break;
            }

            if (imageDescriptorIndex != -1) {

                if (LOG.isTraceEnabled())
                    LOG.trace("decorated " + element + " [idx="
                        + imageDescriptorIndex + "]");

                decoration.addOverlay(imageDescriptors[imageDescriptorIndex],
                    IDecoration.TOP_RIGHT);

                return true;
            } else if (LOG.isTraceEnabled())
                LOG.trace("not decorated " + element);

        } catch (RuntimeException e) {
            LOG.error("Internal Error in SharedProjectFileDecorator:", e);
        }
        return false;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void dispose() {
        sessionManager.removeSarosSessionListener(sessionListener);
        editorManager.removeSharedEditorListener(editorListener);
        sarosSession.set(null);
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    private void updateDecoratorsAsync(final Object[] updateElements) {

        SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
            @Override
            public void run() {
                LabelProviderChangedEvent event = new LabelProviderChangedEvent(
                    SharedProjectFileDecorator.this, updateElements);

                for (ILabelProviderListener listener : listeners) {
                    listener.labelProviderChanged(event);
                }
            }
        });
    }

    private int getImageDescriptorIndex(Collection<User> collection) {

        if (collection.size() != 1)
            return defaultImageDescriptorIndex;

        User user = collection.iterator().next();

        int colorID = user.getColorID();

        if (colorID < 0 || colorID >= SarosSession.MAX_USERCOLORS)
            return defaultImageDescriptorIndex;

        return colorID;

    }

    // TODO move to an utility class
    /**
     * Returns null, if the image is not a RGB image with 8 Bit per color value
     */
    private static Image tintImage(Image image, Color color) {
        ImageData data = image.getImageData();
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        if (data.depth < 24 || !data.palette.isDirect)
            return null;
        int rs = data.palette.redShift;
        int gs = data.palette.greenShift;
        int bs = data.palette.blueShift;
        int rm = data.palette.redMask;
        int gm = data.palette.greenMask;
        int bm = data.palette.blueMask;
        if (rs < 0)
            rs = ~rs + 1;
        if (gs < 0)
            gs = ~gs + 1;
        if (bs < 0)
            bs = ~bs + 1;
        for (int x = 0; x < data.width; x++) {
            for (int y = 0; y < data.height; y++) {
                int p = data.getPixel(x, y);
                int r = (p & rm) >>> rs;
                int g = (p & gm) >>> gs;
                int b = (p & bm) >>> bs;
                r = ((r * red) >>> 8);
                g = ((g * green) >>> 8);
                b = ((b * blue) >>> 8);
                data.setPixel(x, y, (r << rs) | (g << gs) | (b << bs));
            }
        }
        return new Image(Display.getCurrent(), data);
    }
}