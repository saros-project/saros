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

class EditorListener implements IViewportListener, MouseListener, KeyListener,
    ISelectionChangedListener {

    private final EditorManager manager;

    private final EditorAPI editorAPI;

    private final ITextViewer viewer;

    private ITextSelection lastSelection = new TextSelection(-1, -1);

    public EditorListener(EditorAPI editorAPI, EditorManager manager,
        ITextViewer viewer) {
        this.manager = manager;
        this.viewer = viewer;
        this.editorAPI = editorAPI;

        viewer.getTextWidget().addMouseListener(this);
        viewer.getTextWidget().addKeyListener(this);
        viewer.getSelectionProvider().addSelectionChangedListener(this);
        viewer.addViewportListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.IViewportListener
     */
    public void viewportChanged(int verticalOffset) {
        // TODO why doesnt this react to window resizes?

        IPath editor = manager.getPathOfDocument(this.viewer.getDocument());

        manager.viewportChanged(editor, this.editorAPI.getViewport(viewer));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.MouseListener
     */
    public void mouseDown(MouseEvent e) {
        checkSelection();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.MouseListener
     */
    public void mouseUp(MouseEvent e) {
        checkSelection();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.MouseListener
     */
    public void mouseDoubleClick(MouseEvent e) {
        // ignore
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.KeyListener
     */
    public void keyReleased(KeyEvent e) {
        checkSelection();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.KeyListener
     */
    public void keyPressed(KeyEvent e) {
        // ignore
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ISelectionChangedListener
     */
    public void selectionChanged(SelectionChangedEvent event) {
        checkSelection();
    }

    private void checkSelection() {
        ISelectionProvider sp = this.viewer.getSelectionProvider();
        ITextSelection selection = (ITextSelection) sp.getSelection();

        if (!this.lastSelection.equals(selection)) {
            this.lastSelection = selection;
            this.manager.selectionChanged(selection, sp);
        }
    }
}