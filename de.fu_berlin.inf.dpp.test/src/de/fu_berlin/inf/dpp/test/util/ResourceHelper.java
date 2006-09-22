package de.fu_berlin.inf.dpp.test.util;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

/**
 * A simple helper class for use with JUnit plug-in tests.
 * 
 * @author rdjemili
 */
public class ResourceHelper {
    
    public static IProject createProject(String name) throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject("testProject");
        project.create(null);
        project.open(null);
        
        return project;
    }
    
    public static IFile createFile(IProject project, String path, String content) 
        throws CoreException {
        
        IFile file = project.getFile(new Path(path));
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
        file.create(in, true, null);
        
        return file;
    }
}
