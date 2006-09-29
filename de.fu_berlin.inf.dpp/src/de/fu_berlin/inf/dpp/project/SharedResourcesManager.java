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
package de.fu_berlin.inf.dpp.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;

/**
 * This manager is responsible for handling all resource changes that aren't
 * handled by the EditorManager, that is for changes that aren't done by
 * entering text in an text editor. It creates and executes file activities and
 * folder activities.
 * 
 * @author rdjemili
 */
public class SharedResourcesManager 
    implements IResourceChangeListener, IActivityProvider {
    
    private static Logger log = Logger.getLogger(
        SharedResourcesManager.class.getName());
    
    /**
     * Should be set to <code>true</code> while executing resource changes to
     * avoid an infinite resource event loop.
     */
    private boolean                 replicationInProgess = false;
    
    private ISharedProject          sharedProject;
    private ResourceDeltaVisitor    visitor = new ResourceDeltaVisitor();
    private List<IActivityListener> listeners = 
        new LinkedList<IActivityListener>();
    
    /**
     * Listens for resource changes in shared project.
     */
    private class ResourceDeltaVisitor implements IResourceDeltaVisitor {
        
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResourceDeltaVisitor
         */
        public boolean visit(IResourceDelta delta) {
            assert sharedProject != null;
            
            if (replicationInProgess || !sharedProject.isDriver()) 
                return false;
            
            IResource resource = delta.getResource();
            if (resource.getProject() == null) // work space root
                return true;
            
            if (resource.getProject() != sharedProject.getProject())
                return false;
            
            if (resource.isDerived())
                return false;
            
            IPath path = delta.getProjectRelativePath();
            int kind = delta.getKind();
            
            IActivity activity = null;
            if (resource instanceof IFile) {
                activity = handleFileDelta(path, kind);
                
            } else if (resource instanceof IFolder) {
                activity = handleFolderDelta(path, kind);
            }
            
            fireActivity(activity);
            closeRemovedEditors();
                
            return delta.getKind() > 0;
        }

        private IActivity handleFolderDelta(IPath path, int kind) {
            switch(kind) {
            case IResourceDelta.ADDED:
                return new FolderActivity(FolderActivity.Type.Created, path);
                
            case IResourceDelta.REMOVED:
                return new FolderActivity(FolderActivity.Type.Removed, path);
                
            default:
                return null;
            }
        }

        private IActivity handleFileDelta(IPath path, int kind) {
            switch(kind) {
            case IResourceDelta.CHANGED:
            case IResourceDelta.ADDED:
                // ignore opened files because otherwise we might send CHANGED
                // events for files that are also handled by the editor manager.
                if (EditorManager.getDefault().isOpened(path))
                    return null;
                
                return new FileActivity(FileActivity.Type.Created, path);
                
            case IResourceDelta.REMOVED:
                return new FileActivity(FileActivity.Type.Removed, path);
                
            default:
                return null;
            }
        }

        private void fireActivity(IActivity activity) {
            if (activity == null)
                return;
            
            for (IActivityListener listener : listeners)
                listener.activityCreated(activity);
        }
    }
    
    public SharedResourcesManager() {
    	Saros.getDefault().getSessionManager().addSessionListener(this);
	}
    
    /* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.ISessionListener
	 */
	public void sessionStarted(ISharedProject session) {
		sharedProject = session;
		sharedProject.getActivityManager().addProvider(this);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.ISessionListener
	 */
	public void sessionEnded(ISharedProject session) {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		sharedProject.getActivityManager().removeProvider(this);
		sharedProject = null;
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.ISessionListener
	 */
	public void invitationReceived(IIncomingInvitationProcess invitation) {
		// ignore
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
            event.getDelta().accept(visitor);
        } catch (CoreException e) {
            log.log(Level.SEVERE, "Couldn't handle resource change.", e);
        }
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void exec(IActivity activity) {
        try {
            replicationInProgess = true;
            
            if (activity instanceof FileActivity)
                exec((FileActivity)activity);
            
            else if (activity instanceof FolderActivity)
                exec((FolderActivity)activity);
            
        } catch (CoreException e) {
            log.log(Level.SEVERE, "Failed to execute resource activity.", e);
            
        } finally {
            replicationInProgess = false;
        }
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.IActivityProvider
     */
    public IActivity fromXML(XmlPullParser parser) {
    	try {
    		if (parser.getName().equals("file")) {
                return parseFile(parser);
                
            } else if (parser.getName().equals("folder")) {
            	return parseFolder(parser);
            }
    		
    	} catch (IOException e) {
    		log.severe("Couldn't parse message");
    	} catch (XmlPullParserException e) {
    		log.severe("Couldn't parse message");
    	}
    	
    	return null;
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.IActivityProvider
	 */
	public String toXML(IActivity activity) {
		if (activity instanceof FileActivity) {
            FileActivity fileActivity = (FileActivity)activity;
            return "<file " + 
                "path=\""+fileActivity.getPath()+"\" " +
                "type=\""+fileActivity.getType()+"\" />";
            
        } else if (activity instanceof FolderActivity) {
            FolderActivity folderActivity = (FolderActivity)activity;
            return "<folder " +
                "path=\""+folderActivity.getPath()+"\" " +
                "type=\""+folderActivity.getType()+"\" />";
        }
		
		return null;
	}

	private void exec(FileActivity activity) throws CoreException {
        IProject project = sharedProject.getProject();
        IFile file = project.getFile(activity.getPath());

        if (activity.getType() == FileActivity.Type.Created) {
            InputStream in = activity.getContents();
            if (file.exists()) {
                file.setContents(in, IResource.FORCE, null);
            } else {
                file.create(in, true, new NullProgressMonitor());
            }

        } else if (activity.getType() == FileActivity.Type.Removed) {
            file.delete(false, new NullProgressMonitor());
        }

        closeRemovedEditors();
    }
    
    private void exec(FolderActivity activity) throws CoreException {
        IProject project = sharedProject.getProject();
        IFolder folder = project.getFolder(activity.getPath());
        
        if (activity.getType() == FolderActivity.Type.Created) {
            folder.create(true, true, new NullProgressMonitor());
        } else if (activity.getType() == FolderActivity.Type.Removed) {
            folder.delete(true, new NullProgressMonitor());
        }
    }
    
    private FileActivity parseFile(XmlPullParser parser)
    	throws XmlPullParserException, IOException {

    	IPath path = new Path(parser.getAttributeValue(null, "path"));
    	return new FileActivity(
			FileActivity.Type.valueOf(parser.getAttributeValue(null, "type")),
			path);
    }

    private FolderActivity parseFolder(XmlPullParser parser) {
    	Path path = new Path(parser.getAttributeValue(null, "path"));

    	return new FolderActivity(
			FolderActivity.Type.valueOf(parser.getAttributeValue(null, "type")),
			path);
    }
    
    /**
     * Checks if given project is a Java project and returns the output location
     * of that Java project.
     * 
     * @param the project for which the output location should be fetched.
     * @return the output location of given Java project. <code>null</code> if
     * given project has no Java nature.
     */
    private static IPath getJavaOutputLocation(IProject project) {
        // we are using IResource#isDerived() instead. this should probably be
        // removed
        
        try {
            IProjectNature javaNature = project.getNature(
                JavaCore.NATURE_ID);
            
            IJavaProject javaProject = (IJavaProject)javaNature;
            IPath path = javaProject.getOutputLocation();
            return path.removeFirstSegments(1);
            
        } catch (CoreException e) {
            // ignore - not a Java project
            return null;
        }
    }

    private void closeRemovedEditors() {
        // TODO
    }
}
