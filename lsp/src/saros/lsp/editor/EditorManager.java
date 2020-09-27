package saros.lsp.editor;

import java.util.Collections;
import java.util.Set;
import saros.editor.IEditorManager;
import saros.editor.ISharedEditorListener;
import saros.editor.text.LineRange;
import saros.editor.text.TextSelection;
import saros.filesystem.IFile;
import saros.filesystem.IReferencePoint;
import saros.session.User;

public class EditorManager implements IEditorManager {

  @Override
  public void openEditor(IFile path, boolean activate) {}

  @Override
  public Set<IFile> getOpenEditors() {
    return Collections.emptySet();
  }

  @Override
  public String getContent(IFile path) {
    return null;
  }

  @Override
  public void saveEditors(IReferencePoint project) {}

  @Override
  public void closeEditor(IFile path) {}

  @Override
  public void adjustViewport(IFile path, LineRange range, TextSelection selection) {}

  @Override
  public void jumpToUser(User target) {}

  @Override
  public void addSharedEditorListener(ISharedEditorListener listener) {}

  @Override
  public void removeSharedEditorListener(ISharedEditorListener listener) {}

  @Override
  public String getNormalizedContent(IFile file) {
    return null;
  }
}
