package de.fu_berlin.inf.dpp.test.util;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import de.fu_berlin.inf.dpp.test.fakes.EclipseWorkspaceFakeFacade;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.junit.After;
import org.junit.Test;

/**
 * @author cordes
 */
public class EclipseWorkspaceFakeFacadeTest {

    @After
    public void tearDown() {
        EclipseWorkspaceFakeFacade.deleteWorkspaces();
    }

    @Test
    public void test() {
        IWorkspace workspace1 = EclipseWorkspaceFakeFacade
            .createWorkspace("alice");
        IWorkspace workspace2 = EclipseWorkspaceFakeFacade.createWorkspace("bob");

        assertTrue(workspace1.getRoot().exists());
        assertTrue(workspace2.getRoot().exists());

        EclipseWorkspaceFakeFacade.deleteWorkspaces();

        assertFalse(workspace1.getRoot().exists());
        assertFalse(workspace2.getRoot().exists());
    }

    @Test
    public void testCreateTestProject() {
        IWorkspace workspace = EclipseWorkspaceFakeFacade.createWorkspace("alice");
        IProject project = workspace.getRoot().getProject("testproject");
        assertTrue(project.exists());
        assertFalse(project.getFile("src/Person.java").exists());

        EclipseWorkspaceFakeFacade.addSomeProjectData(project);

        assertTrue(project.getFile("src/Person.java").exists());

    }

}
