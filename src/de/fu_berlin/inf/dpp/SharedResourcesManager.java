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
package de.fu_berlin.inf.dpp;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IncomingResourceAddActivity;
import de.fu_berlin.inf.dpp.activities.ResourceAddActivity;
import de.fu_berlin.inf.dpp.activities.ResourceRemoveActivity;

public class SharedResourcesManager implements IResourceChangeListener, IActivityProvider {
    private ISharedProject          sharedProject;

    private boolean                 replicationInProgess = false;
    private List<IActivityListener> listeners = new LinkedList<IActivityListener>();
    
    private class ResourceDeltaVisitor implements IResourceDeltaVisitor {
        
        public boolean visit(IResourceDelta delta) throws CoreException {
            if (replicationInProgess) 
                return false;
            
            if (delta.getResource().getProject() != sharedProject.getProject())
                return true;
            
            if (delta.getKind() == IResourceDelta.REMOVED) {
                handleRemoved(delta);
                
            } else if (delta.getKind() == IResourceDelta.ADDED) {
                handleAdded(delta);
            }
            
            return delta.getKind() > 0;
        }

        private void handleAdded(IResourceDelta delta) {
            IPath path = delta.getProjectRelativePath();
            
            ResourceAddActivity activity = new ResourceAddActivity(path);
            for (IActivityListener listener : listeners) {
                listener.activityCreated(activity);
            }
        }

        private void handleRemoved(IResourceDelta delta) {
            ResourceRemoveActivity activity = new ResourceRemoveActivity(
                delta.getProjectRelativePath());
            
            for (IActivityListener listener : listeners) {
                listener.activityCreated(activity);
            }
            
            closeRemovedEditors();
        }
    }
    
    public SharedResourcesManager(ISharedProject project) {
        sharedProject = project;
        
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void addActivityListener(IActivityListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void removeActivityListener(IActivityListener listener) {
        listeners.remove(listener);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.resources.IResourceChangeListener
     */
    public void resourceChanged(IResourceChangeEvent event) {
        try {
            event.getDelta().accept(new ResourceDeltaVisitor());
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void exec(IActivity activity) {
        if (activity instanceof IncomingResourceAddActivity) {
            exec((IncomingResourceAddActivity)activity);
            
        } else if (activity instanceof ResourceRemoveActivity) {
            exec((ResourceRemoveActivity)activity);
        }
    }

    private void exec(IncomingResourceAddActivity activity) {
        replicationInProgess = true;
        
        try {
            IFile file = sharedProject.getProject().getFile(activity.getPath());
            InputStream in = activity.getContents();
            
            if (file.exists()) {
                file.setContents(in, IResource.FORCE, new NullProgressMonitor());
            } else {
                file.create(in, true, new NullProgressMonitor());
            }
            
        } catch (CoreException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        
        replicationInProgess = false;
    }
    
    private void exec(ResourceRemoveActivity activity) {
        replicationInProgess = true;
        
        try {
            
            IFile file = sharedProject.getProject().getFile(activity.getPath());
            // TODO check if this triggers the resource listener
            file.delete(true, new NullProgressMonitor());
            closeRemovedEditors();
            
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        replicationInProgess = false;
    }
    
    private void closeRemovedEditors() {
        // TODO
    }
}
