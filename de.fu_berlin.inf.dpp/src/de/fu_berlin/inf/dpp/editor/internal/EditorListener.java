/**
 * 
 */
package de.fu_berlin.inf.dpp.editor.internal;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.ui.IEditorPart;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.editor.EditorManager;

public class EditorListener implements Disposable {

    private final EditorManager manager;

    private final ITextViewer viewer;

    private final IEditorPart part;

    private ITextSelection lastSelection = new TextSelection(-1, -1);

    public EditorListener(IEditorPart part, EditorManager manager) {

        this.part = part;
        this.manager = manager;
        this.viewer = EditorAPI.getViewer(part);

        viewer.getTextWidget().addMouseListener(mouseListener);
        viewer.getTextWidget().addKeyListener(keyListener);
        viewer.getSelectionProvider().addSelectionChangedListener(
            selectionChangedListener);
        viewer.addViewportListener(viewportListener);
    }

    protected final MouseListener mouseListener = new MouseListener() {

        public void mouseDown(MouseEvent e) {
            checkSelection();
        }

        public void mouseUp(MouseEvent e) {
            checkSelection();
        }

        public void mouseDoubleClick(MouseEvent e) {
            // ignore
        }
    };

    protected final KeyListener keyListener = new KeyListener() {
        public void keyReleased(KeyEvent e) {
            checkSelection();
        }

        public void keyPressed(KeyEvent e) {
            // ignore
        }
    };

    protected IViewportListener viewportListener = new IViewportListener() {
        // TODO why doesn't this react to window resizes?
        public void viewportChanged(int verticalOffset) {
            manager.generateViewport(part, EditorAPI.getViewport(viewer));
        }
    };

    protected ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
            checkSelection();
        }
    };

    protected void checkSelection() {

        ITextSelection selection = (ITextSelection) viewer
            .getSelectionProvider().getSelection();

        if (!this.lastSelection.equals(selection)) {
            this.lastSelection = selection;
            this.manager.generateSelection(this.part, selection);
        }
    }
    
    /** TODO This is not called */
    public void dispose() {
        viewer.getTextWidget().removeMouseListener(mouseListener);
        viewer.getTextWidget().removeKeyListener(keyListener);
        viewer.getSelectionProvider().removeSelectionChangedListener(
            selectionChangedListener);
        viewer.removeViewportListener(viewportListener);

    }
}