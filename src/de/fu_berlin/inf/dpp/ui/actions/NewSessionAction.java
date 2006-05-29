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
import de.fu_berlin.inf.dpp.SessionManager;

public class NewSessionAction implements IObjectActionDelegate {
    private IStructuredSelection selection;

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
            SessionManager sessionManager = Saros.getDefault().getSessionManager();
            sessionManager.startSession((IProject)selection.getFirstElement());
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * Defined in IActionDelegate
     */
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = (IStructuredSelection)selection;
        
        action.setEnabled(Saros.getDefault().isConnected() && isProjectSelected());
    }
    
    private boolean isProjectSelected() {
        Object selectedElement = selection.getFirstElement();
        return selectedElement instanceof IProject || selectedElement instanceof IJavaProject;
    }
}
