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

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Add another IProject to the Saros session, on the fly.
 * 
 * This is a HACK to test Saros multi project support! Need better UI and sync
 * for added project.
 * 
 * This action is created by Eclipse!
 * 
 * @author coezbek
 */
@Component(module = "action")
public class AddToSessionAction implements IObjectActionDelegate {

    private static final Logger log = Logger
        .getLogger(AddToSessionAction.class);

    protected IProject selectedProject;

    @Inject
    protected SarosSessionManager sessionManager;

    @Inject
    protected Saros saros;

    public AddToSessionAction() {
        super();
        Saros.injectDependenciesOnly(this);
    }

    public void selectionChanged(IAction action, ISelection selection) {

        this.selectedProject = getProject(selection);

        action
            .setEnabled(saros.isConnected()
                && sessionManager.getSarosSession() != null
                && (this.selectedProject != null)
                && this.selectedProject.isAccessible()
                && !sessionManager.getSarosSession().isShared(
                    this.selectedProject));
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        // We deal with everything in selectionChanged
    }

    protected IProject getProject(ISelection selection) {
        Object element = ((IStructuredSelection) selection).getFirstElement();
        if (element instanceof IResource) {
            return ((IResource) element).getProject();
        }
        return null;
    }

    protected void runSafe() {
        final ISarosSession sarosSession = sessionManager.getSarosSession();

        if (sarosSession.isHost()) {
            sarosSession.addSharedProject(this.selectedProject,
                this.selectedProject.getName());

            log.info("Added project: " + this.selectedProject.getName()
                + " using its name");

        } else {
            Shell shell = EditorAPI.getShell();

            assert shell != null : "Action should not be run if the display is disposed";

            String message = "Enter the name of the project on the host side (case-sensitive):";

            InputDialog dialog = new InputDialog(shell, "Set new nickname",
                message, this.selectedProject.getName(), new IInputValidator() {
                    public String isValid(String newText) {
                        if (newText == null || newText.trim().length() == 0) {
                            return "Saros needs the name of the project on the host side!";
                        } else {
                            if (sarosSession.getProject(newText) != null)
                                return "Project Name is already used to share a project!";
                        }
                        // Okay...
                        return null;
                    }
                });

            if (dialog.open() == Window.OK) {
                sarosSession.addSharedProject(this.selectedProject,
                    dialog.getValue());
                log.info("Added project: " + this.selectedProject.getName()
                    + " using ID: " + dialog.getValue());
            }
        }
    }

    public void run(IAction action) {
        Util.runSafeSync(log, new Runnable() {
            public void run() {
                runSafe();
            }
        });
    }
}
