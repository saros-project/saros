package de.fu_berlin.inf.dpp.test.net;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import de.fu_berlin.inf.dpp.test.util.ResourceHelper;
import junit.framework.TestCase;

public class Test extends TestCase {

	private static Logger logger = Logger.getLogger(Test.class.toString());
	
	public void testWorkspace() throws CoreException{
		logger.log(Level.ALL, "init");
//		logger.debug("hello");
		IProject project = ResourceHelper.createDefaultProject();
//		IFile file = ResourceHelper.createFile(project, "testfile.txt", "dies ist ein Testfile");
		logger.log(Level.ALL, "logtest");
		assertTrue(project.exists());
//		file.delete(true, new NullProgressMonitor());
//		assertFalse(file.exists());
	}
}
