package saros.server.editor;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import saros.activities.TextEditActivity;
import saros.filesystem.IFile;

/** Representation of an open file on the server. Used by {@link ServerEditorManager}. */
public class Editor {

  private IFile file;
  private GapBuffer content;

  public Editor(IFile file) throws IOException {
    this.file = file;

    try (InputStream input = file.getContents()) {
      content = new GapBuffer(IOUtils.toString(input));
    }
  }

  /**
   * Returns the path of the text file this editor is associated with.
   *
   * @return associated file
   */
  public IFile getFile() {
    return file;
  }

  /**
   * Returns the editor's text content. Depending on whether any text edits have been applied, the
   * content may differ from that of the associated file on disk.
   *
   * @return editor's content
   */
  public String getContent() {
    return content.toString();
  }

  /**
   * Applies an editing operation to the editor's content. For performance reasons, the change is
   * not automatically saved to disk; this allows multiple edits to be collected and then written in
   * one go (by calling {@link #save}).
   *
   * @param edit the text edit operation to apply
   */
  public void applyTextEdit(TextEditActivity edit) {
    if (edit.getReplacedText().length() > 0) {
      content.delete(edit.getOffset(), edit.getReplacedText().length());
    }
    if (edit.getText().length() > 0) {
      content.insert(edit.getOffset(), edit.getText());
    }
  }

  /**
   * Writes the editor's current content to the associated file on disk. This operation is
   * guaranteed to be atomic - it either succeeds completely or doesn't change the workspace at all
   * (in case an exception is thrown).
   *
   * @throws IOException if writing the file fails
   */
  public void save() throws IOException {
    getFile().setContents(IOUtils.toInputStream(content.toString()), true, true);
  }
}
