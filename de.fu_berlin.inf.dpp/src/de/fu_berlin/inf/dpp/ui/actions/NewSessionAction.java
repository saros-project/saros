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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.jivesoftware.smack.XMPPException;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Start to share a project (a "session").
 * 
 * @author rdjemili
 * 
 *         This action is created by Eclipse!
 * 
 */
public class NewSessionAction implements IObjectActionDelegate {

    private static final Logger log = Logger.getLogger(NewSessionAction.class
        .getName());

    protected IProject selectedProject;

    @Inject
    protected SessionManager sessionManager;

    @Inject
    protected Saros saros;

    /*
     * (non-Javadoc) Defined in IActionDelegate
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        // We deal with everything in selectionChanged
    }

    public NewSessionAction() {
        super();
        Saros.reinject(this);
    }

    /**
     * @review runSafe OK
     */
    public void run(IAction action) {
        Util.runSafeSync(log, new Runnable() {
            public void run() {
                runNewSession();
            }
        });
    }

    public void runNewSession() {
        try {
            sessionManager.startSession(this.selectedProject);
        } catch (final XMPPException e) {
            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    ErrorDialog.openError(EditorAPI.getShell(),
                        "Error Starting Session",
                        "Session could not be started", new Status(
                            IStatus.ERROR, "de.fu_berlin.inf.dpp",
                            IStatus.ERROR, e.getMessage(), e));
                }
            });
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        this.selectedProject = getProject(selection);

        boolean running = sessionManager.getSharedProject() != null;
        boolean connected = saros.isConnected();

        // TODO This action should rather connect if not already connected
        // instead of being disabled.

        action.setEnabled(connected && !running
            && (this.selectedProject != null)
            && this.selectedProject.isAccessible());
    }

    protected IProject getProject(ISelection selection) {
        Object element = ((IStructuredSelection) selection).getFirstElement();
        if (element instanceof IResource) {
            return ((IResource) element).getProject();
        }
        return null;
    }
}
