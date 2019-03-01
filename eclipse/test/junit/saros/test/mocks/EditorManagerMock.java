package saros.test.mocks;

import java.util.List;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import saros.editor.EditorManager;
import saros.editor.ISharedEditorListener;

public class EditorManagerMock {
  public static EditorManager createMock(final List<Object> editorListeners) {
    EditorManager editorManager = EasyMock.createMock(EditorManager.class);
    editorManager.addSharedEditorListener(EasyMock.isA(ISharedEditorListener.class));
    EasyMock.expectLastCall()
        .andAnswer(
            new IAnswer<Object>() {

              @Override
              public Object answer() throws Throwable {
                editorListeners.add(EasyMock.getCurrentArguments()[0]);
                return null;
              }
            })
        .anyTimes();
    editorManager.removeSharedEditorListener(EasyMock.isA(ISharedEditorListener.class));
    EasyMock.expectLastCall()
        .andAnswer(
            new IAnswer<Object>() {

              @Override
              public Object answer() throws Throwable {
                editorListeners.remove(EasyMock.getCurrentArguments()[0]);
                return null;
              }
            })
        .anyTimes();
    EasyMock.replay(editorManager);
    return editorManager;
  }
}
