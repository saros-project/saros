package de.fu_berlin.inf.dpp.editor;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;

/**
 * A document listener which informs the given EditorManager of changes before they occur in a
 * document (using documentAboutToBeChanged). This listener can be temporarily disabled which
 * prevents the notification of text change events.
 */
public class StoppableDocumentListener implements IDocumentListener {

  private final EditorManager editorManager;

  private boolean enabled = true;

  StoppableDocumentListener(EditorManager editorManager) {
    this.editorManager = editorManager;
  }

  @Override
  public void documentAboutToBeChanged(final DocumentEvent event) {

    if (!enabled) return;

    String text = event.getText() == null ? "" : event.getText();

    editorManager.textAboutToBeChanged(
        event.getOffset(), text, event.getLength(), event.getDocument());
  }

  @Override
  public void documentChanged(final DocumentEvent event) {
    // do nothing. We handled everything in documentAboutToBeChanged
  }

  /**
   * Enables or disables the forwarding of text changes. Default is enabled.
   *
   * @param enabled <code>true</code> to forward text changes, <code>false</code> otherwise
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
