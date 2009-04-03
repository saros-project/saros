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

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Decorates Shared Projects.
 * 
 * @see ILightweightLabelDecorator
 * 
 * @author oezbek
 */
public class SharedProjectDecorator implements ILightweightLabelDecorator {

    private static final Logger log = Logger
        .getLogger(SharedProjectDecorator.class.getName());

    public static final ImageDescriptor projectDescriptor = SarosUI
        .getImageDescriptor("icons/bullet_feed.png"); // NON-NLS-1

    protected ISharedProject sharedProject;

    protected List<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();

    protected ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(ISharedProject project) {
            sharedProject = project;
            updateDecoratorsAsync(project.getProject());
        }

        @Override
        public void sessionEnded(ISharedProject project) {
            assert sharedProject == project;
            sharedProject = null;
            updateDecoratorsAsync(project.getProject());
        }
    };

    public SharedProjectDecorator() {
        ISessionManager sessionManager = Saros.getDefault().getSessionManager();

        sessionManager.addSessionListener(sessionListener);

        if (sessionManager.getSharedProject() != null) {
            sessionListener.sessionStarted(sessionManager.getSharedProject());
        }
    }

    public void decorate(Object element, IDecoration decoration) {

        if (this.sharedProject == null) {
            return;
        }

        // Enablement in the Plugin.xml ensures that we only get IProjects

        if (element instanceof IProject) {
            if (!this.sharedProject.getProject().equals(element)) {
                return;
            }
            decoration.addOverlay(SharedProjectDecorator.projectDescriptor,
                IDecoration.TOP_LEFT);
            return;
        }
    }

    public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
    }

    public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
    }

    public void dispose() {
        Saros.getDefault().getSessionManager().removeSessionListener(
            sessionListener);
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    protected void updateDecoratorsAsync(final IProject project) {
        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                LabelProviderChangedEvent event = new LabelProviderChangedEvent(
                    SharedProjectDecorator.this, new Object[] { project });

                for (ILabelProviderListener listener : listeners) {
                    listener.labelProviderChanged(event);
                }
            }
        });
    }
}