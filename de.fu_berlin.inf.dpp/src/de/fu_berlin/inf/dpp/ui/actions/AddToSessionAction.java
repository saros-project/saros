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
package de.fu_berlin.inf.dpp.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Add another IProject to the Saros session, on the fly.
 * 
 * This action is created by Eclipse!
 * 
 * @author coezbek, cdohnert
 */
@Component(module = "action")
public class AddToSessionAction implements IObjectActionDelegate {

    private static final Logger log = Logger
        .getLogger(AddToSessionAction.class);

    protected List<IProject> selectedProjects;

    @Inject
    protected SarosSessionManager sessionManager;

    @Inject
    protected Saros saros;

    public AddToSessionAction() {
        super();
        Saros.injectDependenciesOnly(this);
    }

    public void selectionChanged(IAction action, ISelection selection) {

        this.selectedProjects = getProjects(selection);
        boolean allAccessible = true;
        boolean oneIsShared = false;
        if (sessionManager.getSarosSession() == null) {
            action.setEnabled(false);
            return;
        }

        for (IProject p : this.selectedProjects) {
            if (!p.isAccessible()) {
                allAccessible = false;
            }
            if (sessionManager.getSarosSession().isShared(p)) {
                oneIsShared = true;
            }
        }
        /*
         * Enable button if we are connected, have a session, have at least one
         * project selected, we have write access, every selected project is
         * accessible and not already shared
         */
        action
            .setEnabled(saros.isConnected()
                && sessionManager.getSarosSession() != null
                && !this.selectedProjects.isEmpty()
                && allAccessible
                && !oneIsShared
                && sessionManager.getSarosSession().getLocalUser()
                    .hasWriteAccess());
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        // We deal with everything in selectionChanged
    }

    /**
     * @return List of selected Projects. If no project is selected an empty
     *         list is returned
     */
    protected List<IProject> getProjects(ISelection selection) {
        List<IProject> result = new ArrayList<IProject>();
        for (Object o : ((IStructuredSelection) selection).toArray()) {
            if (o instanceof IResource) {
                if (!(result.contains(((IResource) o).getProject()))) {
                    result.add(((IResource) o).getProject());
                }
            }
        }
        return result;
    }

    protected void runSafe() {
        if (this.selectedProjects.size() > 1) {
            String message = "You selected the Projects:\n";
            for (IProject p : this.selectedProjects) {
                message += "- " + p.getName() + "\n";
            }
            message += "\nAdding multiple projects is not supported yet.\nIf you continue only project "
                + this.selectedProjects.get(0) + " will be added to session.";
            if (!Utils.popUpYesNoQuestion("Multiple Projects marked", message,
                false)) {
                return;
            }
        }
        List<IProject> projectsToAdd = new ArrayList<IProject>();
        projectsToAdd.add(this.selectedProjects.get(0));
        sessionManager.addProjectsToSession(projectsToAdd);
    }

    public void run(IAction action) {
        Utils.runSafeSync(log, new Runnable() {
            public void run() {
                runSafe();
            }
        });
    }
}
