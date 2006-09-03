
package de.fu_berlin.inf.dpp.test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;

import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.IEditorAPI;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.test.stubs.EditorAPIStub;
import de.fu_berlin.inf.dpp.test.stubs.FileStub;

public class EditorManagerTest2 extends TestCase {
    private IEditorPart    editorMock1;
    private IEditorPart    editorMock2;

    private Path           path1;
    private Path           path2;

    private IEditorAPI     editorAPIMock;
    private ISharedProject sharedProjectMock;

    @Override
    protected void setUp() throws Exception {
        editorMock1 = createMock(IEditorPart.class);
        editorMock2 = createMock(IEditorPart.class);
        
        editorAPIMock = new EditorAPIStub();
        sharedProjectMock = createNiceMock(ISharedProject.class);
        
//        Set<IEditorPart> editorParts = new HashSet<IEditorPart>(2);
//        editorParts.add(editorPartMock1);
//        editorParts.add(editorPartMock2);
//        expect(humbleEditorMock.getOpenEditors()).andStubReturn(editorParts);
//
//        path1 = new Path("foo");
//        path2 = new Path("bar");
//        expect(humbleEditorMock.getEditorResource(editorPartMock1)).andStubReturn(path1);
//        expect(humbleEditorMock.getEditorResource(editorPartMock2)).andStubReturn(path2);
    }
    
    public void testActivateDriverEditor() {
        IPath path = new Path("/foo/test");
//        FileStub fileStub = new FileStub("/foo/test", "test content");
//        
//        expect(editorAPIMock.getEditorResource(isA(IEditorPart.class)))
//            .andStubReturn(fileStub);
//        
//        expect(editorAPIMock.getSelection(isA(IEditorPart.class)))
//            .andStubReturn(new TextSelection(1,5));
//        
//        expect(editorAPIMock.getViewport(isA(IEditorPart.class)))
//            .andStubReturn(new LineRange(1,5));
//        
//        Set<IEditorPart> editors = new HashSet<IEditorPart>();
//        editors.add(editorMock1);
//        editors.add(editorMock2);
//        expect(editorAPIMock.getOpenEditors())
//            .andStubReturn(editors);
//        
//        expect(editorMock1.getEditorInput())
//            .andStubReturn(new FileEditorInput(fileStub));
//        
//        expect(sharedProjectMock.isDriver())
//            .andStubReturn(true);
        
//        replayMocks();
        
//        EditorManager editorManager = EditorManager.getDefault(); 
//        	new EditorManager(sharedProjectMock, editorAPIMock);
//        editorManager.driverChanged(new JID("riad@jabber.org"), false);
//        
//        editorManager.partActivated(editorMock1);
//        assertEquals(path, editorManager.getActiveDriverEditor());
        
        
//        expect(sharedProjectMock.isDriver()).andStubReturn(true);
//
//        sharedProjectMock.setActiveDriverEditor(path1, false);
//        replayMocks();
//
//        EditorManager editorManager = createEditorManager();
//        editorManager.partActivated(editorPartMock1);
//        verifyMocks();
    }

//    public void testSetDriverPathOnStartIfActiveEditorAndDriver() {
//        expect(sharedProjectMock.isDriver()).andReturn(true);
//        expect(humbleEditorMock.getActiveEditor()).andReturn(editorPartMock1);
//        replayMocks();
//
//        createEditorManager();
//        verifyMocks();
//    }
//
//    public void testDontSetDriverPathOnStartIfNotDriver() {
//        expect(humbleEditorMock.getActiveEditor()).andStubReturn(editorPartMock1);
//        replayMocks();
//
//        createEditorManager();
//        verifyMocks();
//    }
//
//    public void testSetDriverPathOnActivationAndDriver() {
//        expect(sharedProjectMock.isDriver()).andStubReturn(true);
//
//        sharedProjectMock.setActiveDriverEditor(path1, false);
//        replayMocks();
//
//        EditorManager editorManager = createEditorManager();
//        editorManager.partActivated(editorPartMock1);
//        verifyMocks();
//    }
//
//    public void testDontSetDriverPathOnActivationIfNotDriver() {
//        replayMocks();
//
//        EditorManager editorManager = createEditorManager();
//        editorManager.partActivated(editorPartMock1);
//        verifyMocks();
//    }
//
//    // TODO fix
//    public void testSetText() {
//        IFile fileMock = createMock(IFile.class);
//        IProject projectMock = createMock(IProject.class);
//
//        expect(sharedProjectMock.getActiveDriverEditor()).andStubReturn(path1);
//        expect(sharedProjectMock.getProject()).andStubReturn(projectMock);
//        expect(projectMock.getFile(path1)).andStubReturn(fileMock);
//
//        humbleEditorMock.setText(fileMock, 5, 3, "abc");
//        replayMocks();
//        replay(projectMock);
//        replay(fileMock);
//
//        EditorManager editorManager = createEditorManager();
//        editorManager.exec(new TextEditActivity(5, "abc", 3));
//        verifyMocks();
//        verify(projectMock);
//        verify(fileMock);
//    }
//
//    public void testUpdateSelectionAfterSetText() {
//        IProject projectMock = createMock(IProject.class);
//        IFile file = createMock(IFile.class);
//
//        expect(sharedProjectMock.getProject()).andStubReturn(projectMock);
//        expect(projectMock.getFile(path1)).andStubReturn(file);
//        expect(sharedProjectMock.getActiveDriverEditor()).andStubReturn(path1);
//        humbleEditorMock.setSelection(editorPartMock1, new TextSelection(5, 0));
//        replayMocks();
//
//        EditorManager editorManager = createEditorManager();
//        editorManager.exec(new TextEditActivity(5, "abc", 3));
//        verifyMocks();
//    }
//
//    public void testSetSelection() {
//        expect(sharedProjectMock.getActiveDriverEditor()).andStubReturn(path1);
//        humbleEditorMock.setSelection(editorPartMock1, new TextSelection(15, 2));
//        replayMocks();
//
//        EditorManager editorManager = createEditorManager();
//        editorManager.exec(new TextSelectionActivity(15, 2));
//        verifyMocks();
//    }
//
//    public void testAttachListenerToActivatedEditorIfDriver() {
//        expect(sharedProjectMock.isDriver()).andStubReturn(true);
//
//        humbleEditorMock.addSharedEditorListener(editorPartMock1);
//        replayMocks();
//
//        EditorManager editorManager = createEditorManager();
//        editorManager.partActivated(editorPartMock1);
//        verifyMocks();
//    }
//
//    public void testSetAllEditorsToEditable() {
//        expect(sharedProjectMock.isDriver()).andStubReturn(true);
//        humbleEditorMock.setEditable(editorPartMock1, true);
//        humbleEditorMock.setEditable(editorPartMock2, true);
//        replayMocks();
//
//        createEditorManager();
//        verifyMocks();
//    }
//
//    public void testSetSelectionAfterActivatingDriverEditor() {
//        expect(sharedProjectMock.getActiveDriverEditor()).andStubReturn(path1);
//        expect(sharedProjectMock.getDriverTextSelection()).andStubReturn(new TextSelection(23, 42));
//        humbleEditorMock.setSelection(editorPartMock1, new TextSelection(23, 42));
//        replayMocks();
//
//        EditorManager editorManager = createEditorManager();
//        editorManager.partActivated(editorPartMock1);
//        verifyMocks();
//    }
//
//    public void testOpenEditorIfFollowMode() {
//        IProject projectMock = createMock(IProject.class);
//        IFile fileMock = createMock(IFile.class);
//
//        expect(sharedProjectMock.getActiveDriverEditor()).andStubReturn(path1);
//        expect(sharedProjectMock.getProject()).andStubReturn(projectMock);
//        expect(projectMock.getFile(path1)).andStubReturn(fileMock);
//        humbleEditorMock.openEditor(fileMock);
//        replayMocks();
//        replay(projectMock, fileMock);
//
//        EditorManager editorManager = createEditorManager();
//        editorManager.setEnableFollowing(true);
//        editorManager.exec(new TextEditActivity(5, "abc", 3));
//        verifyMocks();
//        verify(projectMock, fileMock);
//    }
//
//    public void testFireTextLoadActivity() {
//        IActivityListener activityListenerMock = createMock(IActivityListener.class);
//        activityListenerMock.activityCreated(new TextEditActivity(5, "test", 1));
//        replayMocks();
//        replay(activityListenerMock);
//
//        EditorManager editorManager = createEditorManager();
//        editorManager.addActivityListener(activityListenerMock);
//        editorManager.textChanged(5, "test", 1);
//        verifyMocks();
//        verify(activityListenerMock);
//    }
//
//    public void testSendTextSelectionsAfterBecomingDriver() {
//        expect(humbleEditorMock.getActiveEditor()).andStubReturn(editorPartMock2);
//        expect(sharedProjectMock.isDriver()).andReturn(false).times(3).andReturn(true);
//        IActivityListener activityListenerMock = createMock(IActivityListener.class);
//        
//        activityListenerMock.activityCreated(new TextSelectionActivity(5, 10));
//        replayMocks();
//        replay(activityListenerMock);
//        
//        EditorManager editorManager = createEditorManager();
//        editorManager.addActivityListener(activityListenerMock);
//        
//        editorManager.driverChanged(new JID("dummy"), true);
//        editorManager.selectionChanged(new TextSelection(5, 10));
//        
//        verifyMocks();
//        verify(activityListenerMock);
//    }
//    
//    public void testSendTextLoadAfterBecomingDriver() {
//        expect(humbleEditorMock.getActiveEditor()).andStubReturn(editorPartMock2);
//        expect(sharedProjectMock.isDriver()).andReturn(false).times(2).andStubReturn(true);
//        sharedProjectMock.setActiveDriverEditor(path2, false);
//        replayMocks();
//        
//        EditorManager editorManager = createEditorManager();
//        editorManager.driverChanged(new JID("dummy"), true);
//        verifyMocks();
//    }
//    
//    public void testDontConnectMultipleTimesWhileEditorOpen() {
//        expect(sharedProjectMock.isDriver()).andStubReturn(true);
//        humbleEditorMock.addSharedEditorListener(editorPartMock1);
//        humbleEditorMock.addSharedEditorListener(editorPartMock2);
//        replayMocks(); // HACK
//        
//        EditorManager editorManager = createEditorManager();
//        editorManager.partActivated(editorPartMock1);
//        editorManager.partActivated(editorPartMock2);
//        editorManager.partActivated(editorPartMock1);
//        verifyMocks();
//    }
//    
//    private EditorManager createEditorManager() {
//        return new EditorManager(sharedProjectMock, editorAPIMock);
//    }

    private void replayMocks() {
        replay(sharedProjectMock, editorAPIMock, editorMock1, editorMock2);
    }

    private void verifyMocks() {
        verify(sharedProjectMock, editorAPIMock, editorMock1, editorMock2);
    }
}
