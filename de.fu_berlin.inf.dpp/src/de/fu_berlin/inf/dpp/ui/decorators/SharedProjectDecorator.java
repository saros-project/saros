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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;

/**
 * Decorates Shared Project files.
 * 
 * @see ILightweightLabelDecorator
 */
public class SharedProjectDecorator implements ILightweightLabelDecorator, ISessionListener,
	ISharedEditorListener {

	private ImageDescriptor activeDescriptor = SarosUI.getImageDescriptor("icons/bullet_green.png"); // NON-NLS-1

	private ImageDescriptor passiveDescriptor = SarosUI
		.getImageDescriptor("icons/bullet_yellow.png"); // NON-NLS-1

	private ISharedProject sharedProject;

	private List<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();

	public SharedProjectDecorator() {
		SessionManager sessionManager = Saros.getDefault().getSessionManager();
		sessionManager.addSessionListener(this);

		if (sessionManager.getSharedProject() != null) {
			sessionStarted(sessionManager.getSharedProject());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate
	 */
	public void decorate(Object element, IDecoration decoration) {
		if (sharedProject == null)
			return;

		IFile file = (IFile) element; // enablement ensures that we only get
		// IFile's
		if (!sharedProject.getProject().equals(file.getProject()))
			return;

		IPath path = file.getProjectRelativePath();
		if (path != null) {
			EditorManager editorManager = EditorManager.getDefault();
			if (path.equals(editorManager.getActiveDriverEditor())) {
				decoration.addOverlay(activeDescriptor, IDecoration.TOP_LEFT);
			} else if (editorManager.getDriverEditors().contains(path)) {
				decoration.addOverlay(passiveDescriptor, IDecoration.TOP_LEFT);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener
	 */
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener
	 */
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.listeners.ISessionListener#sessionStarted
	 */
	public void sessionStarted(ISharedProject session) {
		sharedProject = session;
		EditorManager.getDefault().addSharedEditorListener(this);

		updateDecorators(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.listeners.ISessionListener#sessionEnded
	 */
	public void sessionEnded(ISharedProject session) {
		if (sharedProject != null)
			EditorManager.getDefault().removeSharedEditorListener(this);

		sharedProject = null;
		updateDecorators(null);
	}

	public void invitationReceived(IIncomingInvitationProcess process) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProjectListener
	 */
	public void activeDriverEditorChanged(IPath path, boolean replicated) {
		updateDecorators(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProjectListener
	 */
	public void driverEditorRemoved(IPath path, boolean replicated) {
		updateDecorators(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProjectListener
	 */
	public void driverEditorSaved(IPath path, boolean replicated) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.editor.ISharedEditorListener
	 */
	public void followModeChanged(boolean enabled) {
		// ignore
	}

	private void updateDecorators(final Object[] updateElements) {
		// Set<IFile> changedFiles = new HashSet<IFile>();
		//        
		// if (sharedProject != null) {
		// if (sharedProject.getActiveDriverEditor() != null) {
		// activeFile = sharedProject.getProject().getFile(
		// sharedProject.getActiveDriverEditor());
		//                
		// } else if (sharedProject.getDriverEditors() != null) {
		// passiveFiles =
		// }
		// }
		//        
		// final Object[] updateElements;
		// if (activeDriverFile == null) {
		// updateElements = new Object[]{activeFile};
		// } else if (activeFile == null) {
		// updateElements = new Object[]{activeDriverFile};
		// } else {
		// updateElements = new Object[]{activeDriverFile, activeFile};
		// }
		//        
		// activeDriverFile = activeFile;

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