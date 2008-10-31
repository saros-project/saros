package de.fu_berlin.inf.dpp.test.zip;

import java.io.File;
import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.fu_berlin.inf.dpp.util.FileZipper;

public class TarFileZipperTest extends TestCase {
    static {
	PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
    }

    public TarFileZipperTest(String name) {
	super(name);
    }

    @Override
    protected void setUp() throws Exception {

	super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
	super.tearDown();
    }

    public void testCreateZipArchive() throws Exception {
	// FileZipper.createZipArchive();
	// FileZipper.readInputStreamsProjectArchive(new
	// File("/home/troll/t_incoming_archive.zip"));
	FileZipper.readInputStreamsProjectArchive(new File(
		"/home/troll/Project.zip"));
	// FileZipper.readZipArchive("/home/troll/t_incoming_archive.zip");
    }

    public void xtestCreateProjectArchive() throws Exception {

	//		
	List<IPath> list = new Vector<IPath>();
	list.add(new Path("/home/troll/test_archiv/ConnectionDateiori79.log"));
	list.add(new Path("/home/troll/test_archiv/ConnectionDateiori80.log"));

	FileZipper.createProjectZipArchive(list, "/home/troll/archive.zip",
		null);
	FileZipper.readInputStreamsProjectArchive(new File(
		"/home/troll/archive.zip"));

    }

}
