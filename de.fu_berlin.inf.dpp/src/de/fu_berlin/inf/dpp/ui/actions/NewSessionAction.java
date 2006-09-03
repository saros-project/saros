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
package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.project.SessionManager;

public class NewSessionAction implements IObjectActionDelegate {
    private IProject selectedProject;
    
    /* (non-Javadoc)
     * Defined in IActionDelegate
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    /* (non-Javadoc)
     * Defined in IActionDelegate
     */
    public void run(IAction action) {
        try {
            SessionManager sm = Saros.getDefault().getSessionManager();
            sm.startSession(selectedProject);
            
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * Defined in IActionDelegate
     */
    public void selectionChanged(IAction action, ISelection selection) {
        selectedProject = getProject(selection);
        
        SessionManager sm = Saros.getDefault().getSessionManager();
        boolean running = sm.getSharedProject() != null;
        boolean connected = Saros.getDefault().isConnected();
        
        action.setEnabled(connected && !running && selectedProject != null && 
            selectedProject.isAccessible());
    }

    private IProject getProject(ISelection selection) {
        Object element = ((IStructuredSelection)selection).getFirstElement();
        if (element instanceof IProject) {
            return (IProject)element;
        } else if (element instanceof IJavaProject) {
            return ((IJavaProject)element).getProject();
        }
        
        return null;
    }
}
