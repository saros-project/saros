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
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
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
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.util.Utils;

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

    protected ISarosSession sarosSession;

    protected List<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            sarosSession = newSarosSession;
            updateDecoratorsAsync(newSarosSession.getProjects().toArray());
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            assert sarosSession == oldSarosSession;
            sarosSession = null;
            updateDecoratorsAsync(oldSarosSession.getProjects().toArray());
        }

        @Override
        public void projectAdded(String projectID) {
            log.debug("PROJECT ADDED: " + projectID);
            updateDecoratorsAsync(sarosSession.getProjects().toArray());
        }
    };

    @Inject
    protected SarosSessionManager sessionManager;

    public SharedProjectDecorator() {
        SarosPluginContext.initComponent(this);

        sessionManager.addSarosSessionListener(sessionListener);
        if (sessionManager.getSarosSession() != null) {
            sessionListener.sessionStarted(sessionManager.getSarosSession());
        }
    }

    public void dispose() {
        sessionManager.removeSarosSessionListener(sessionListener);
    }

    public void decorate(Object element, IDecoration decoration) {
        if (this.sarosSession == null) {
            return;
        }

        if (element instanceof IProject) {
            IProject project = (IProject) element;

            if (this.sarosSession.isShared(project)) {
                decoration.addOverlay(SharedProjectDecorator.projectDescriptor,
                    IDecoration.TOP_LEFT);
            }
        }
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
    }

    protected void updateDecoratorsAsync(final Object[] objects) {
        log.debug("Decoration update");
        Utils.runSafeSWTAsync(log, new Runnable() {
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