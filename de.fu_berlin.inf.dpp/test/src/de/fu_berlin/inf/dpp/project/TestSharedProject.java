package de.fu_berlin.inf.dpp.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.test.stubs.SarosSessionStub;

public class TestSharedProject {
    private SarosSessionStub session;
    private IProject project;
    private NullProgressMonitor monitor = new NullProgressMonitor();
    private ByteArrayInputStream fileContents;

    public @Before
    void setUp() throws Exception {
        session = new SarosSessionStub();

        project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject("TestProject");
        project.create(monitor);
        project.open(monitor);

        fileContents = new ByteArrayInputStream(
            "Hello Kitty!".getBytes("UTF-8"));
    }

    public @After
    void tearDown() throws Exception {
        project.delete(true, monitor);
        project = null;

        session = null;
    }

    public @Test
    void resourceCreation() throws Exception {
        // Create a file and folder handles.
        IFolder hello = project.getFolder(new Path("hello"));
        IFile kitty = project.getFile(new Path("hello/kitty"));

        // Create a file and folder.
        hello.create(true, true, monitor);
        kitty.create(fileContents, true, monitor);

        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject("TestProject");
        final Path path = new Path("hello/kitty");
        IResource resource = project.findMember(path);
        assertTrue(resource.exists());
        assertTrue(ResourcesPlugin.getWorkspace().getRoot()
            .findMember(new Path("TestProject/hello/kitty")).exists());
    }

    public @Test
    void createSharedProject() throws Exception {
        session.setWriteAccess(true);
        session.setUseVersionControl(false);

        SharedProject sharedProject = new SharedProject(project, session);
        assertTrue(sharedProject.contains(project));
        assertTrue(sharedProject.checkIntegrity());
        assertEquals(null, sharedProject.getVCSAdapter());
    }

    public @Test
    void addFolder() throws Exception {
        session.setWriteAccess(true);
        session.setUseVersionControl(false);
        SharedProject sharedProject = new SharedProject(project, session);

        // Create a folder handle.
        IFolder hello = project.getFolder(new Path("hello"));
        assertFalse(sharedProject.contains(hello));
        // Create the actual folder.
        hello.create(true, true, monitor);
        sharedProject.add(hello);
        assertTrue(sharedProject.contains(hello));
        assertTrue(sharedProject.checkIntegrity());
    }

    public @Test(expected = IllegalArgumentException.class)
    void checkUpdateFails() throws Exception {
        session.setWriteAccess(true);
        session.setUseVersionControl(false);
        SharedProject sharedProject = new SharedProject(project, session);
        IFile kitty = project.getFile(new Path("kitty"));
        sharedProject.updateRevision(kitty, "123");
    }

    public @Test
    void addFile() throws Exception {
        session.setWriteAccess(true);
        session.setUseVersionControl(false);
        SharedProject sharedProject = new SharedProject(project, session);

        // Create a file and folder handles.
        IFolder hello = project.getFolder(new Path("hello"));
        IFile kitty = project.getFile(new Path("hello/kitty"));
        assertFalse(sharedProject.contains(kitty));

        // Create a file and folder.
        hello.create(true, true, monitor);
        kitty.create(fileContents, true, monitor);
        sharedProject.add(hello);
        sharedProject.add(kitty);
        assertTrue(sharedProject.contains(kitty));
        assertTrue(sharedProject.checkIntegrity());
    }

    public @Test
    void delete() throws Exception {
        session.setWriteAccess(true);
        session.setUseVersionControl(false);
        SharedProject sharedProject = new SharedProject(project, session);

        // Create a file and folder handles.
        IFolder hello = project.getFolder(new Path("hello"));
        IFile kitty = project.getFile(new Path("hello/kitty"));
        hello.create(true, true, monitor);
        kitty.create(fileContents, true, monitor);
        sharedProject.add(hello);
        sharedProject.add(kitty);

        assertTrue(sharedProject.contains(kitty));
        sharedProject.remove(kitty);
        assertFalse(sharedProject.contains(kitty));

        assertTrue(sharedProject.contains(hello));
        sharedProject.remove(hello);
        assertFalse(sharedProject.contains(hello));
    }
}
