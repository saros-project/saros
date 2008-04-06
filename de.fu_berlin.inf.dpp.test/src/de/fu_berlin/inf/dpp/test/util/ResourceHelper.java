package de.fu_berlin.inf.dpp.test.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * A simple helper class for use with JUnit plug-in tests.
 * 
 * @author rdjemili
 */
public class ResourceHelper {
    
	public static Logger logger = Logger.getLogger(ResourceHelper.class.toString());
	
	public static String TEST_PROJECT = "JUnitTestProject";
	public static String RECEIVED_TEST_PROJECT = "JUnitReceivedTestProject";
	
	
	private static void initProject(IProject localProject) throws CoreException{
		IFile file = createFile(localProject, "First.java", "public class First{ /* erste Testklasse*/ }");
//		if (file.exists()) {
////			file.setReadOnly(false);
//			
//			file.setContents(input, IResource.FORCE, new NullProgressMonitor());
//		} else {
//			file.create(input, true, new NullProgressMonitor());
//		}
	}
	
    public static IProject createProject(String name) throws CoreException {
    	
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(name);
        if(!project.exists()){
        	logger.info("project will create");
        	project.create(null);
        	project.open(null);
        	initProject(project);
        }
//        project.create(null);
        project.open(null);
        
        return project;
    }
    
    public static IProject createDefaultProject() throws CoreException {
    	return createProject(TEST_PROJECT);
    }
    
    public static IProject getProject(String name) throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(name);
        if(!project.exists()){
        	project.create(null);
        }
//        project.create(null);
        project.open(null);
        
        return project;
    }
    
    public static IProject getDefaultProject() throws CoreException {
    	
    	return getProject(TEST_PROJECT);
    }
    
    public static IFile getDefaultFile() throws CoreException{
    	IProject project = getDefaultProject();
    	IFile file = project.getFile("First.java");
    	if(!file.exists()){
    		initProject(project);
    		file = project.getFile("First.java");
    	}
    	return file;
    }
    
    public static IFile getFile(String path) throws CoreException{
    	IProject project = getDefaultProject();
    	IFile file = project.getFile(path);
    	if(!file.exists()){
    		return null;
    	}
    	return file;
    }
    
    public static IFile createFile(IProject project, String path, String content) 
        throws CoreException {
        
        IFile file = project.getFile(new Path(path));
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
        file.create(in, true, null);
        
        return file;
    }
    
    public static IFile createFile(IProject project, String path, InputStream input) 
    throws CoreException {
    
    IFile file = project.getFile(new Path(path));
//    ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
    file.create(input, true, null);
    
    
    return file;
}
}
