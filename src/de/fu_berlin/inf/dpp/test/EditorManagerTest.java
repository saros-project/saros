package de.fu_berlin.inf.dpp.test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorPart;

import de.fu_berlin.inf.dpp.EditorManager;
import de.fu_berlin.inf.dpp.ISharedProject;
import de.fu_berlin.inf.dpp.activities.CursorOffsetActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextLoadActivity;
import de.fu_berlin.inf.dpp.internal.IHumbleEditorManager;

public class EditorManagerTest extends TestCase {
    private IEditorPart          editorPartMock1;
    private IEditorPart          editorPartMock2;

    private Path                 path1;
    private Path                 path2;

    private IHumbleEditorManager humbleEditorMock;
    private ISharedProject       sharedProjectMock;

    @Override
    protected void setUp() throws Exception {
        editorPartMock1 = createMock(IEditorPart.class);
        editorPartMock2 = createMock(IEditorPart.class);
        
        sharedProjectMock = createNiceMock(ISharedProject.class);
        
        humbleEditorMock = createNiceMock(IHumbleEditorManager.class);
        List<IEditorPart> editorParts = new ArrayList<IEditorPart>(2);
        editorParts.add(editorPartMock1);
        editorParts.add(editorPartMock2);
        expect(humbleEditorMock.getOpenedEditors()).andStubReturn(editorParts);
        
        path1 = new Path("foo");
        path2 = new Path("bar");
        expect(humbleEditorMock.getEditorPath(editorPartMock1)).andStubReturn(path1);
        expect(humbleEditorMock.getEditorPath(editorPartMock2)).andStubReturn(path2);
    }
    
    public void testSetDriverPathOnStartIfActiveEditorAndDriver() {
        expect(sharedProjectMock.isDriver()).andStubReturn(true);
        expect(humbleEditorMock.getActiveEditor()).andStubReturn(editorPartMock1);
        replayMocks();

        new EditorManager(sharedProjectMock, humbleEditorMock);
        verifyMocks();
    }
    
    public void testDontSetDriverPathOnStartIfNotDriver() {
        expect(humbleEditorMock.getActiveEditor()).andStubReturn(editorPartMock1);
        replayMocks();

        new EditorManager(sharedProjectMock, humbleEditorMock);
        verifyMocks();
    }
    
    public void testSetDriverPathOnActivationAndDriver() {
        expect(sharedProjectMock.isDriver()).andStubReturn(true);
        
        sharedProjectMock.setDriverPath(path1, false);
        replayMocks();
        
        EditorManager editorManager = new EditorManager(sharedProjectMock, humbleEditorMock);
        editorManager.partActivated(editorPartMock1);
        verifyMocks();
    }
    
    public void testDontSetDriverPathOnActivationIfNotDriver() {
        replayMocks();
        
        EditorManager editorManager = new EditorManager(sharedProjectMock, humbleEditorMock);
        editorManager.partActivated(editorPartMock1);
        verifyMocks();
    }
    
    public void testSetText() {
        IFile fileMock = createMock(IFile.class);
        IProject projectMock = createMock(IProject.class);
        
        expect(sharedProjectMock.getDriverPath()).andStubReturn(path1);
        expect(sharedProjectMock.getProject()).andStubReturn(projectMock);
        expect(projectMock.getFile(path1)).andStubReturn(fileMock);
        
        humbleEditorMock.setText(fileMock, 5, 3, "abc");
        replayMocks();
        replay(projectMock);
        replay(fileMock);
        
        EditorManager editorManager = new EditorManager(sharedProjectMock, humbleEditorMock);
        editorManager.exec(new TextEditActivity(5, "abc", 3));
        verifyMocks();
        verify(projectMock);
        verify(fileMock);
    }
    
    public void testUpdateSelectionAfterSetText() {
        IProject projectMock = createMock(IProject.class);
        IFile file = createMock(IFile.class);
        
        expect(sharedProjectMock.getProject()).andStubReturn(projectMock);
        expect(projectMock.getFile(path1)).andStubReturn(file);
        expect(sharedProjectMock.getDriverPath()).andStubReturn(path1);
        humbleEditorMock.setSelection(editorPartMock1, new TextSelection(5, 0));
        replayMocks();
        
        EditorManager editorManager = new EditorManager(sharedProjectMock, humbleEditorMock);
        editorManager.exec(new TextEditActivity(5, "abc", 3));
        verifyMocks();
    }
    
    public void testSetSelection() {
        expect(sharedProjectMock.getDriverPath()).andStubReturn(path1);
        humbleEditorMock.setSelection(editorPartMock1, new TextSelection(15, 2));
        replayMocks();
        
        EditorManager editorManager = new EditorManager(sharedProjectMock, humbleEditorMock);
        editorManager.exec(new CursorOffsetActivity(15, 2));
        verifyMocks();
    }
    
    public void testAttachListenerToActivatedEditorIfDriver() {
        expect(sharedProjectMock.isDriver()).andStubReturn(true);
        
        humbleEditorMock.connect(editorPartMock1);
        replayMocks();
        
        EditorManager editorManager = new EditorManager(sharedProjectMock, humbleEditorMock);
        editorManager.partActivated(editorPartMock1);
        verifyMocks();
    }
    
    public void testSetAllEditorsToEditable() {
        expect(sharedProjectMock.isDriver()).andStubReturn(true);
        humbleEditorMock.setEditable(editorPartMock1, true);
        humbleEditorMock.setEditable(editorPartMock2, true);
        replayMocks();
        
        new EditorManager(sharedProjectMock, humbleEditorMock);
        verifyMocks();
    }
    
    public void testSetSelectionAfterActivatingDriverEditor() {
        expect(sharedProjectMock.getDriverPath()).andStubReturn(path1);
        expect(sharedProjectMock.getDriverTextSelection()).andStubReturn(new TextSelection(23, 42));
        humbleEditorMock.setSelection(editorPartMock1, new TextSelection(23, 42));
        replayMocks();
        
        EditorManager editorManager = new EditorManager(sharedProjectMock, humbleEditorMock);
        editorManager.partActivated(editorPartMock1);
        verifyMocks();
    }
    
    public void testOpenEditorIfFollowMode() {
        IProject projectMock = createMock(IProject.class);
        IFile fileMock = createMock(IFile.class);
        
        expect(sharedProjectMock.getDriverPath()).andStubReturn(path1);
        expect(sharedProjectMock.getProject()).andStubReturn(projectMock);
        expect(projectMock.getFile(path1)).andStubReturn(fileMock);
        humbleEditorMock.openEditor(fileMock);
        replayMocks();
        replay(projectMock, fileMock);
        
        EditorManager editorManager = new EditorManager(sharedProjectMock, humbleEditorMock);
        editorManager.setEnableFollowing(true);
        editorManager.exec(new TextEditActivity(5, "abc", 3));
        verifyMocks();
        verify(projectMock, fileMock);
    }
    
    private void replayMocks() {
        replay(sharedProjectMock, humbleEditorMock, editorPartMock1, editorPartMock2);
    }

    private void verifyMocks() {
        verify(sharedProjectMock, humbleEditorMock, editorPartMock1, editorPartMock2);
    }
}
