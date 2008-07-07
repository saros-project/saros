
package de.fu_berlin.inf.dpp.test;

import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorPart;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.internal.SharedProject;
import de.fu_berlin.inf.dpp.test.util.ResourceHelper;

public class EditorManagerTest extends TestCase {
    private static final JID MY_JID    = new JID("riad@jabber.org");
    private static final JID OTHER_JID = new JID("bob@jabber.org");

    private IProject         project;
    private IFile            file1;
    private IFile            file2;
    private EditorManager    editorManager;

    private EditorAPI        editorAPI;
    private SharedProject    sharedProject;
    private ISessionManager   sessionManager;

    @Override
    protected void setUp() throws Exception {
        project = ResourceHelper.createProject("testProject");
        file1 = ResourceHelper.createFile(project, "foo.txt", "test content");
        file2 = ResourceHelper.createFile(project, "bar.java", "public class Test{}");

        sharedProject = new SharedProject(null, project, MY_JID);
        editorManager = EditorManager.getDefault();
        editorAPI = new EditorAPI();

        sessionManager = Saros.getDefault().getSessionManager();
    }

    @Override
    protected void tearDown() throws Exception {
        sessionManager.leaveSession();
        project.delete(true, null);
        closeOpenEditors();
    }

    public void testSetActivateDriverEditorIfDriver() {
        editorAPI.openEditor(file1);
        assertEquals(file1.getProjectRelativePath(), editorManager.getActiveDriverEditor());
    }

    public void testDontSetActivateDriverEditorIfNotDriver() {
        User user = new User(OTHER_JID);
        sharedProject.addUser(user);
        sharedProject.setDriver(user, true);
        editorAPI.openEditor(file1);

        assertFalse(file1.getProjectRelativePath().equals(editorManager.getActiveDriverEditor()));
    }

    public void testSetDriverEditorOnStartIfActiveEditorAndDriver() throws Exception {
        editorAPI.openEditor(file1);
        editorAPI.openEditor(file2);
        
        sessionManager.leaveSession();
        sharedProject = new SharedProject(null, project, MY_JID);
        editorManager = EditorManager.getDefault();
        
        assertEquals(file2.getProjectRelativePath(), editorManager.getActiveDriverEditor());
    }
    
    public void testDontSetDriverPathOnStartIfNotDriver() {
        editorAPI.openEditor(file1);
        
        sessionManager.leaveSession();
        sharedProject = new SharedProject(null, project, OTHER_JID);
        editorManager = EditorManager.getDefault();
        
        assertNull(editorManager.getActiveDriverEditor());
    }

    private void closeOpenEditors() {
        Set<IEditorPart> openEditors = editorAPI.getOpenEditors();
        for (IEditorPart part : openEditors) {
            editorAPI.closeEditor(part);
        }
    }
}
