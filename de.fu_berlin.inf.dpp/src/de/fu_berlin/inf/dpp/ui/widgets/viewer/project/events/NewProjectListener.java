package de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;

import de.fu_berlin.inf.dpp.ui.util.WizardUtils;

/**
 * Listener used to get the project by the
 * {@link WizardUtils#openNewProjectWizard()} call.
 * <p>
 * Copyright (c) 2005, 2006 Subclipse project and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * Contributors: Subclipse project committers - initial API and implementation
 */
public class NewProjectListener implements IResourceChangeListener {
    private IProject newProject = null;

    /**
     * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
     */
    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getType() != IResourceChangeEvent.POST_CHANGE)
            return;

        IResourceDelta root = event.getDelta();
        IResourceDelta[] projectDeltas = root.getAffectedChildren();
        for (int i = 0; i < projectDeltas.length; i++) {
            IResourceDelta delta = projectDeltas[i];
            IResource resource = delta.getResource();
            if (delta.getKind() == IResourceDelta.ADDED) {
                newProject = (IProject) resource;
            }
        }
    }

    /**
     * Gets the newProject.
     * 
     * @return Returns a IProject
     */
    public IProject getNewProject() {
        return newProject;
    }
}