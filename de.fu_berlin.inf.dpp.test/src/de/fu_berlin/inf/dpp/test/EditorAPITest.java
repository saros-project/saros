
package de.fu_berlin.inf.dpp.test;

import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorPart;

import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.internal.SharedProject;
import de.fu_berlin.inf.dpp.test.util.ResourceHelper;

/**
 * JUnit Plug-in Test for the EditorAPI.
 * 
 * @author rdjemili
 */
public class EditorAPITest extends TestCase {
    private IProject      project;
    private IFile         file;
    private EditorAPI     editorAPI;
    private EditorManager editorManager;

    @Override
    protected void setUp() throws Exception {
        project = ResourceHelper.createProject("testProject");
        file = ResourceHelper.createFile(project, "foo.txt", "test content");
        
        editorAPI = new EditorAPI();
        
//        SharedProject sharedProject =
//            new SharedProject(null, project, new JID("riad@jabber.org"));

        editorManager = EditorManager.getDefault();
    }
    
    @Override
    protected void tearDown() throws Exception {
        project.delete(true, null);
    }

    public void testOpenEditor() {
        IEditorPart part = editorAPI.openEditor(file);
        assertNotNull(part);
        assertEquals(file, part.getEditorInput().getAdapter(IFile.class));
    }
    
    public void testGetOpenEditors() {
        IEditorPart part = editorAPI.openEditor(file);
        
        Set<IEditorPart> editors = editorAPI.getOpenEditors();
        assertEquals(1, editors.size());
        assertTrue(editors.contains(part));
    }
    
    public void testCloseEditor() {
        IEditorPart part = editorAPI.openEditor(file);
        editorAPI.closeEditor(part);
        assertEquals(0, editorAPI.getOpenEditors().size());
    }
    
    public void testGetActiveEditor() {
        IEditorPart part = editorAPI.openEditor(file);
        assertEquals(part, editorAPI.getActiveEditor());
    }
    
    public void testSetGetSelection() {
        IEditorPart part = editorAPI.openEditor(file);
        TextSelection selection = new TextSelection(1, 3);
        editorAPI.setSelection(part, selection,file.getName());
        
        ITextSelection selection2 = editorAPI.getSelection(part);
        assertEquals(selection.getOffset(), selection2.getOffset());
        assertEquals(selection.getLength(), selection2.getLength());
    }
    
//    public void testSetGetViewport() {
//        IEditorPart part = editorAPI.openEditor(file);
//        ILineRange viewport = new LineRange(1, 2);
//        editorAPI.setSelection(part, selection);
//        
//        ITextSelection selection2 = editorAPI.getSelection(part);
//        assertEquals(selection.getOffset(), selection2.getOffset());
//        assertEquals(selection.getLength(), selection2.getLength());
//    }
    
    public void testName() {
        editorManager.driverChanged(new JID("riad@jabber.org"), false);
        IEditorPart part = editorAPI.openEditor(file);

        // assertEquals(file.getProjectRelativePath(),
        assertEquals(file.getProjectRelativePath(), editorManager.getActiveDriverEditor());
    }
    
}