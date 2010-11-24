/**
 * 
 */
package de.fu_berlin.inf.dpp.editor.internal;

import org.apache.log4j.Logger;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineRange;
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

import de.fu_berlin.inf.dpp.editor.EditorManager;

/**
 * Listener for tracking the selection and viewport of an IEditorPart and
 * reporting any changes to an EditorManager.
 */
public class EditorListener {

    private static final Logger log = Logger.getLogger(EditorListener.class
        .getName());

    protected EditorManager manager;

    protected ITextViewer viewer;

    protected IEditorPart part;

    protected ITextSelection lastSelection = new TextSelection(-1, -1);

    protected ILineRange lastViewport = new LineRange(-1, -1);

    public EditorListener(EditorManager manager) {
        this.manager = manager;
    }

    public void bind(IEditorPart part) {

        if (this.part != null) {
            unbind();
        }

        this.part = part;
        this.viewer = EditorAPI.getViewer(part);

        if (viewer == null) {
            throw new IllegalArgumentException(
                "EditorPart does not provide an ITextViewer!");
        }

        StyledText textWidget = viewer.getTextWidget();
        textWidget.addControlListener(controlListener);
        textWidget.addMouseListener(mouseListener);
        textWidget.addKeyListener(keyListener);
        viewer.addTextListener(textListener);
        viewer.getSelectionProvider().addSelectionChangedListener(
            selectionChangedListener);
        viewer.addViewportListener(viewportListener);
    }

    /**
     * Disconnects all listeners from the underlying EditorPart.
     * 
     * @throws IllegalStateException
     *             if already disposed
     */
    public void unbind() {

        if (part == null) {
            throw new IllegalStateException();
        }

        StyledText textWidget = viewer.getTextWidget();
        textWidget.removeControlListener(controlListener);
        textWidget.removeMouseListener(mouseListener);
        textWidget.removeKeyListener(keyListener);

        viewer.getSelectionProvider().removeSelectionChangedListener(
            selectionChangedListener);
        viewer.removeViewportListener(viewportListener);
        viewer.removeTextListener(textListener);

        viewer = null;
        part = null;
    }

    /**
     * Listens to resize events of the control, because the IViewportListener
     * does not report such events.
     */
    protected ControlListener controlListener = new ControlListener() {

        public void controlMoved(ControlEvent e) {
            generateViewport();
        }

        public void controlResized(ControlEvent e) {
            generateViewport();
        }

    };

    protected final MouseListener mouseListener = new MouseListener() {

        public void mouseDown(MouseEvent e) {
            generateSelection();
        }

        public void mouseUp(MouseEvent e) {
            generateSelection();
        }

        public void mouseDoubleClick(MouseEvent e) {
            // ignore
        }
    };

    protected final KeyListener keyListener = new KeyListener() {
        public void keyReleased(KeyEvent e) {
            generateSelection();
        }

        public void keyPressed(KeyEvent e) {
            // ignore
        }
    };

    protected IViewportListener viewportListener = new IViewportListener() {
        /*
         * This does not report window resizes because of
         * https://bugs.eclipse.org/bugs/show_bug.cgi?id=171018
         */
        public void viewportChanged(int verticalOffset) {
            generateViewport();
        }
    };

    protected ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
            generateSelection();
        }
    };

    /**
     * Listens to newlines being inserted or deleted to inform our listener of
     * updates to the view port
     */
    protected ITextListener textListener = new ITextListener() {

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

        ITextSelection selection = (ITextSelection) viewer
            .getSelectionProvider().getSelection();

        if (!this.lastSelection.equals(selection)) {
            this.lastSelection = selection;
            this.manager.generateSelection(this.part, selection);
        }
    }

    public boolean equals(ILineRange one, ILineRange two) {

        if (one == null)
            return two == null;

        if (two == null)
            return false;

        return one.getNumberOfLines() == two.getNumberOfLines()
            && one.getStartLine() == two.getStartLine();
    }

    protected void generateViewport() {

        ILineRange viewport = EditorAPI.getViewport(viewer);

        if (!equals(viewport, lastViewport)) {
            lastViewport = viewport;
            if (log.isDebugEnabled() && viewport != null) {
                log.debug("Viewport changed: " + viewport.getStartLine() + "+"
                    + viewport.getNumberOfLines());
            }
            manager.generateViewport(part, viewport);
        }
    }
}