package de.fu_berlin.inf.dpp.test.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class Test extends TestCase {

    private static Logger logger = Logger.getLogger(Test.class.toString());

    public void testWorkspace() throws CoreException {
	logger.log(Level.ALL, "init");

	IProject project = ResourceHelper.createDefaultProject();
	// IFile file = ResourceHelper.createFile(project, "testfile.txt",
	// "dies ist ein Testfile");
	logger.log(Level.ALL, "logtest");

	assertTrue(project.exists());
	// file.delete(true, new NullProgressMonitor());
	// assertFalse(file.exists());
    }

}
