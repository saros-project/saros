/** */
package de.fu_berlin.inf.dpp.editor;

import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import org.apache.log4j.Logger;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.ui.IEditorPart;

/**
 * Listener for tracking the selection and viewport of an IEditorPart and reporting any changes to
 * an EditorManager.
 */
public class EditorListener {

  private static final Logger log = Logger.getLogger(EditorListener.class.getName());

  protected final EditorManager manager;

  protected ITextViewer viewer;

  protected IEditorPart part;

  protected ITextSelection lastSelection = TextSelection.emptySelection();

  private LineRange lastViewport;

  protected boolean isUnsupportedEditor;

  public EditorListener(EditorManager manager) {
    this.manager = manager;
  }

  /**
   * Connects all selection listeners to the given {@linkplain IEditorPart editor part}. If an
   * editor part was already bound it will be unbound and replaced with the given editor part.
   *
   * @see #unbind()
   * @param part the editor part to observe
   * @return <code>true</code> if the selection listeners were successfully installed, <code>false
   *     </code> if the selection listeners could not be installed
   */
  public boolean bind(final IEditorPart part) {

    if (this.part != null) unbind();

    final ITextViewer viewer = EditorAPI.getViewer(part);

    if (viewer == null) {
      log.warn(
          "could not attach selection listeners to editor part:"
              + part
              + " , could not retrieve text widget");
      return false;
    }

    this.part = part;
    this.viewer = viewer;

    final StyledText textWidget = viewer.getTextWidget();

    textWidget.addControlListener(controlListener);
    textWidget.addMouseListener(mouseListener);
    textWidget.addKeyListener(keyListener);

    viewer.addTextListener(textListener);
    viewer.getSelectionProvider().addSelectionChangedListener(selectionChangedListener);
    viewer.addViewportListener(viewportListener);

    return true;
  }

  /**
   * Disconnects all selection listeners from the underlying {@linkplain IEditorPart editor part}.
   *
   * @see #bind(IEditorPart)
   */
  public void unbind() {

    if (part == null) return;

    StyledText textWidget = viewer.getTextWidget();
    textWidget.removeControlListener(controlListener);
    textWidget.removeMouseListener(mouseListener);
    textWidget.removeKeyListener(keyListener);

    viewer.getSelectionProvider().removeSelectionChangedListener(selectionChangedListener);
    viewer.removeViewportListener(viewportListener);
    viewer.removeTextListener(textListener);

    viewer = null;
    part = null;
  }

  /**
   * Listens to resize events of the control, because the IViewportListener does not report such
   * events.
   */
  protected ControlListener controlListener =
      new ControlListener() {

        @Override
        public void controlMoved(ControlEvent e) {
          generateViewport();
        }

        @Override
        public void controlResized(ControlEvent e) {
          generateViewport();
        }
      };

  protected final MouseListener mouseListener =
      new MouseListener() {

        @Override
        public void mouseDown(MouseEvent e) {
          generateSelection();
        }

        @Override
        public void mouseUp(MouseEvent e) {
          generateSelection();
        }

        @Override
        public void mouseDoubleClick(MouseEvent e) {
          // ignore
        }
      };

  protected final KeyListener keyListener =
      new KeyListener() {
        @Override
        public void keyReleased(KeyEvent e) {
          generateSelection();
        }

        @Override
        public void keyPressed(KeyEvent e) {
          // ignore
        }
      };

  protected IViewportListener viewportListener =
      new IViewportListener() {
        /*
         * This does not report window resizes because of
         * https://bugs.eclipse.org/bugs/show_bug.cgi?id=171018
         */
        @Override
        public void viewportChanged(int verticalOffset) {
          generateViewport();
        }
      };

  protected ISelectionChangedListener selectionChangedListener =
      new ISelectionChangedListener() {
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
          generateSelection();
        }
      };

  /**
   * Listens to newlines being inserted or deleted to inform our listener of updates to the view
   * port
   */
  protected ITextListener textListener =
      new ITextListener() {

        @Override
        public void textChanged(TextEvent event) {

          String text = event.getText();
          String replaced = event.getReplacedText();
          if ((text != null && text.indexOf('\n') != -1)
              || (replaced != null && replaced.indexOf('\n') != -1)) {
            generateViewport();
          }
        }
      };

  protected void generateSelection() {
    ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();

    if (!lastSelection.equals(selection)) {
      lastSelection = selection;

      manager.generateSelection(
          part,
          new de.fu_berlin.inf.dpp.editor.text.TextSelection(
              selection.getOffset(), selection.getLength()));
    }
  }

  private void generateViewport() {
    LineRange viewport = EditorAPI.getViewport(viewer);

    if (viewport.equals(lastViewport)) return;

    lastViewport = viewport;

    if (log.isDebugEnabled()) {
      log.debug("Viewport changed: " + viewport.getStartLine() + "+" + viewport.getNumberOfLines());
    }

    manager.generateViewport(part, viewport);
  }
}
