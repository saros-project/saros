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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;

/**
 * Decorates Shared Projects.
 * 
 * @see ILightweightLabelDecorator
 * 
 * @author oezbek
 */
@Component(module = "integration")
public class SharedProjectDecorator implements ILightweightLabelDecorator {

    private static final Logger log = Logger
        .getLogger(SharedProjectDecorator.class);

    public static final ImageDescriptor projectDescriptor = ImageManager
        .getImageDescriptor("icons/ovr16/shared.png"); // NON-NLS-1

    protected volatile ISarosSession sarosSession;

    protected List<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            sarosSession = newSarosSession;
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            assert sarosSession == oldSarosSession;
            sarosSession = null;
            updateDecoratorsAsync(resources.toArray());
        }

        @Override
        public void projectAdded(String projectID) {
            log.debug("PROJECT ADDED: " + projectID);
            updateDecoratorsAsync(sarosSession.getProjects().toArray());
            updateDecoratorsAsync(sarosSession.getAllSharedResources()
                .toArray());
        }
    };

    @Inject
    protected ISarosSessionManager sessionManager;

    public SharedProjectDecorator() {
        SarosPluginContext.initComponent(this);

        sessionManager.addSarosSessionListener(sessionListener);
        if (sessionManager.getSarosSession() != null) {
            sessionListener.sessionStarted(sessionManager.getSarosSession());
        }
    }

    @Override
    public void dispose() {
        sessionManager.removeSarosSessionListener(sessionListener);
    }

    List<IResource> resources = new ArrayList<IResource>();

    @Override
    public void decorate(Object element, IDecoration decoration) {
        ISarosSession session = sarosSession;

        if (session == null)
            return;

        IResource resource = (IResource) element;

        if (session.isShared(resource)) {
            resources.add(resource);

            decoration.addOverlay(SharedProjectDecorator.projectDescriptor,
                IDecoration.TOP_LEFT);

            if (resource.getType() == IResource.PROJECT) {
                boolean isCompletelyShared = session
                    .isCompletelyShared(resource.getProject());

                decoration
                    .addSuffix(isCompletelyShared ? Messages.SharedProjectDecorator_shared
                        : Messages.SharedProjectDecorator_shared_partial);
            }
        }
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
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

    protected void updateDecoratorsAsync(final Object[] objects) {
        log.debug("Decoration update");
        SWTUtils.runSafeSWTAsync(log, new Runnable() {
            @Override
            public void run() {
                LabelProviderChangedEvent event = new LabelProviderChangedEvent(
                    SharedProjectDecorator.this, objects);

                for (ILabelProviderListener listener : listeners) {
                    listener.labelProviderChanged(event);
                }
            }
        });
    }
}