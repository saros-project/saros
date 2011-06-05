package de.fu_berlin.inf.dpp.test.util;

import static com.google.common.collect.Lists.newArrayList;
import static de.fu_berlin.inf.dpp.test.util.SarosTestUtils.submonitor;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.fu_berlin.inf.dpp.test.fakes.EclipseWorkspaceFakeFacade;
import de.fu_berlin.inf.dpp.util.FileZipper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;

/**
 * @author cordes
 */
public class WorkspaceFakeObjectTest {

    @Before
    public void setup() {
        EclipseWorkspaceFakeFacade.deleteWorkspaces();
    }

    @After
    public void tearDown() {
        EclipseWorkspaceFakeFacade.deleteWorkspaces();
    }

    @Test
    public void testMoveFile() throws CoreException {
        IWorkspace workspace = EclipseWorkspaceFakeFacade
            .createWorkspace("testworkspace");
        IProject project = workspace.getRoot().getProject("testproject");
        EclipseWorkspaceFakeFacade.addSomeProjectData(project);

        assertTrue(project.getFile("src/Person.java").exists());
        assertFalse(project.getFile("src/Person_moved.java").exists());

        IFile file = project.getFile("src/Person.java");
        file.move(new Path("src/Person_moved.java"), false, submonitor());

        assertFalse(project.getFile("src/Person.java").exists());
        assertTrue(project.getFile("src/Person_moved.java").exists());
    }

    @Test
    public void testMoveFolder() throws CoreException {
        IWorkspace workspace = EclipseWorkspaceFakeFacade
            .createWorkspace("testworkspace");
        IProject project = workspace.getRoot().getProject("testproject");
        EclipseWorkspaceFakeFacade.addSomeProjectData(project);

        assertTrue(project.getFolder("src").exists());
        assertFalse(project.getFolder("src_moved").exists());

        IFolder srcFolder = project.getFolder("src");
        srcFolder.move(new Path("src_moved"), false, submonitor());

        assertFalse(project.getFolder("src").exists());
        assertTrue(project.getFolder("src_moved").exists());
        assertEquals(2, project.getFolder("src_moved").members().length);
    }

    @Test
    public void test() throws CoreException {
        IWorkspace workspace = EclipseWorkspaceFakeFacade
            .createWorkspace("testworkspace");
        IProject project = workspace.getRoot().getProject("testproject");
        EclipseWorkspaceFakeFacade.addSomeProjectData(project);
        assertNotNull(project);

        assertEquals(new Path(""), project.getProjectRelativePath());
        assertSame(project, project.getProject());
        assertTrue(project.isOpen());
        assertTrue(project.exists());
        assertEquals("UTF-8", project.getDefaultCharset());

        IFile file = project.getFile("src/Person.java");
        assertNotNull(file);

        assertEquals(2, project.members().length);
        assertEquals("Address.java", project.members()[0].getName());
        assertEquals("Person.java", project.members()[1].getName());

        assertTrue(project.getFile("src/Person.java").exists());
        assertTrue(project.getFile(new Path("src/Person.java")).exists());
        assertFalse(project.getFile("src/FileWhichNotExists.java").exists());
    }

    @Test
    public void testFileCreation() throws CoreException, IOException {
        IWorkspace workspace = EclipseWorkspaceFakeFacade
            .createWorkspace("testworkspace");
        IProject project = workspace.getRoot().getProject("testproject");

        IFile file = project.getFile("src/FileWhichNotExists.java");

        assertFalse(file.exists());

        InputStream inputStream = new ByteArrayInputStream("Das ist ein Test"
            .getBytes("UTF-8"));
        file.create(inputStream, 0, submonitor());

        assertTrue(file.exists());
        String content = org.apache.commons.io.FileUtils
            .readFileToString(new File(file.getLocation().toPortableString()));
        assertEquals("Das ist ein Test", content);
    }

    @Test
    public void testEmptyProject() throws CoreException {
        IWorkspace workspace = EclipseWorkspaceFakeFacade
            .createWorkspace("testworkspace");
        IProject project = workspace.getRoot().getProject("testproject");
        assertEquals(0, project.members().length);
        assertEquals("testproject", project.getName());
    }

    @Test
    public void testFileFake() throws CoreException {
        IWorkspace workspace = EclipseWorkspaceFakeFacade
            .createWorkspace("testworkspace");
        IProject project = workspace.getRoot().getProject("testproject");
        EclipseWorkspaceFakeFacade.addSomeProjectData(project);
        IFile file = project.getFile("src/Person.java");
        assertNotNull(file);
        assertTrue(file.exists());
        assertFalse(file.isDerived());
        assertEquals(new Path("src/Person.java"), file.getProjectRelativePath());
        assertTrue(file.getLocation().toPortableString().contains(
            "testproject/src/Person.java"));
        assertNotNull(file.getContents());
    }

    @Test
    public void testWorkspaceFake() throws CoreException {
        IWorkspace workspace = EclipseWorkspaceFakeFacade
            .createWorkspace("testworkspace");

        final AtomicBoolean wasRun = new AtomicBoolean(false);

        workspace.run(new IWorkspaceRunnable() {
            public void run(IProgressMonitor iProgressMonitor)
                throws CoreException {
                wasRun.set(true);

            }
        }, submonitor());

        assertTrue(wasRun.get());

        IWorkspaceRoot root = workspace.getRoot();
        assertEquals(0, root.getProjects().length);

        IProject project = root.getProject("test");

        assertTrue(project.exists());
        assertEquals(0, project.members().length);

        assertEquals(1, root.getProjects().length);
    }

    @Test
    public void testFolderFake() throws CoreException {
        IWorkspace workspace = EclipseWorkspaceFakeFacade
            .createWorkspace("testworkspace");
        IProject project = workspace.getRoot().getProject("testproject");
        IFolder folder = project.getFolder("src/testfolder");
        assertFalse(folder.exists());
        folder.create(false, false, submonitor());
        assertTrue(folder.exists());
    }

    @Test
    public void testCreateProjectZipArchive() throws IOException,
        SarosCancellationException {
        File archive = File.createTempFile("SarosSyncArchive-test", ".zip");

        assertEquals(0, archive.length());

        IWorkspace workspace = EclipseWorkspaceFakeFacade
            .createWorkspace("testworkspace");
        IProject project = workspace.getRoot().getProject("testproject");
        EclipseWorkspaceFakeFacade.addSomeProjectData(project);

        List<IPath> toSend = newArrayList();
        toSend.add(new Path("src/Person.java"));
        FileZipper.createProjectZipArchive(toSend, archive, project,
            submonitor());

        Assert.assertTrue(archive.length() > 0);
    }
}
