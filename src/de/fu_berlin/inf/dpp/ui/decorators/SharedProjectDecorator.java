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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;

import de.fu_berlin.inf.dpp.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.ISharedProject;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SessionManager;
import de.fu_berlin.inf.dpp.listeners.ISessionListener;
import de.fu_berlin.inf.dpp.listeners.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.xmpp.JID;

/**
 * Decorates Shared Project files.
 * 
 * @see ILightweightLabelDecorator
 */
public class SharedProjectDecorator implements ILightweightLabelDecorator, 
    ISessionListener, ISharedProjectListener {
    
    /** The integer value representing the placement options */
	private int                          quadrant;
	private ImageDescriptor              descriptor = 
	    SarosUI.getImageDescriptor("icons/bullet_green.png"); // NON-NLS-1

    private ISharedProject                sharedProject;
    private List<ILabelProviderListener>  listeners = new ArrayList<ILabelProviderListener>();
    
    private IFile                         driverFile;


    public SharedProjectDecorator() {
        SessionManager sessionManager = Saros.getDefault().getSessionManager();
        sessionManager.addSessionListener(this);
        
        if (sessionManager.getSharedProject() != null) {
            sessionStarted(sessionManager.getSharedProject());
        }
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate
	 */
	public void decorate(Object element, IDecoration decoration) {
	    if (sharedProject == null || sharedProject.getDriverPath() == null)
	        return;
        
        IFile file = (IFile)element; // enablement ensures that we only get IFile's
        if (!sharedProject.getProject().equals(file.getProject()))
            return;
        
        IPath path = sharedProject.getDriverPath();
        if (path.equals(file.getProjectRelativePath())) {
            quadrant = IDecoration.TOP_RIGHT;
            decoration.addOverlay(descriptor, quadrant);
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener
	 */
	public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener
	 */
	public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
	}

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener#sessionStarted
     */
    public void sessionStarted(ISharedProject session) {
        sharedProject = session;
        sharedProject.addListener(this);
        
        updateDecorators();
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener#sessionEnded
     */
    public void sessionEnded(ISharedProject session) {
        if (sharedProject != null)
            sharedProject.removeListener(this);
        
        sharedProject = null;
        updateDecorators();
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener#invitationReceived
     */
    public void invitationReceived(IIncomingInvitationProcess process) {
        // ignore
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener#driverChanged
     */
    public void driverChanged(JID driver, boolean replicated) {
        // ignore
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener#driverPathChanged
     */
    public void driverPathChanged(IPath path, boolean replicated) {
        updateDecorators();
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener#userJoined
     */
    public void userJoined(JID user) {
        // ignore
    }
    
    public void userLeft(JID user) {
        // ignore
    }

    private void updateDecorators() {
        IFile file = null;
        if (sharedProject != null && sharedProject.getDriverPath() != null) {
            file = sharedProject.getProject().getFile(sharedProject.getDriverPath());
        }
        
        if (file == driverFile)
            return;
        
        final Object[] updateElements;
        if (driverFile == null) {
            updateElements = new Object[]{file};
        } else if (file == null) {
            updateElements = new Object[]{driverFile};
        } else {
            updateElements = new Object[]{driverFile, file};
        }
        
        driverFile = file;
        
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                LabelProviderChangedEvent event = new LabelProviderChangedEvent(
                    SharedProjectDecorator.this, updateElements);
                
                for (ILabelProviderListener listener : listeners) {
                    listener.labelProviderChanged(event);
                }
            }
        });
    }
}