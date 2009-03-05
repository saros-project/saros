/**
 * 
 */
package de.fu_berlin.inf.dpp.editor.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;

import de.fu_berlin.inf.dpp.editor.EditorManager;

public class EditorListener {

    private final EditorManager manager;

    private final EditorAPI editorAPI;

    private final ITextViewer viewer;

    private ITextSelection lastSelection = new TextSelection(-1, -1);

    public EditorListener(EditorAPI editorAPI, EditorManager manager,
        ITextViewer viewer) {
        this.manager = manager;
        this.viewer = viewer;
        this.editorAPI = editorAPI;

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
        public void viewportChanged(int verticalOffset) {
            // TODO why doesn't this react to window resizes?
            IPath editor = manager.getPathOfDocument(viewer.getDocument());

            manager.viewportChanged(editor, editorAPI.getViewport(viewer));
        }
    };

    protected ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
            checkSelection();
        }
    };

    protected void checkSelection() {
        ISelectionProvider sp = this.viewer.getSelectionProvider();
        ITextSelection selection = (ITextSelection) sp.getSelection();

        if (!this.lastSelection.equals(selection)) {
            this.lastSelection = selection;
            this.manager.selectionChanged(selection, sp);
        }
    }
}