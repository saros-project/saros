package de.fu_berlin.inf.dpp.ui.widgets.viewer;

import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * This abstract class is used for {@link Composite} that are based on a central
 * {@link Viewer}. It can be directly used as a {@link IPostSelectionProvider}.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NONE and those supported by {@link StructuredViewer}</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * @author bkahlert
 * 
 */
public abstract class ViewerComposite extends Composite {
    public static final int VIEWER_STYLE = SWT.BORDER | SWT.NO_SCROLL
        | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK | SWT.MULTI;

    protected StructuredViewer viewer;

    public ViewerComposite(Composite parent, int style) {
        super(parent, style & ~VIEWER_STYLE);

        createViewer(style & VIEWER_STYLE);
        configureViewer();
    }

    /**
     * Creates the viewer
     * 
     * @param style
     */
    protected abstract void createViewer(int style);

    /**
     * Configures the viewer (like adding listeners)
     */
    protected abstract void configureViewer();

    /**
     * Return the internal {@link Viewer}
     */
    public StructuredViewer getViewer() {
        return this.viewer;
    }

    @Override
    public boolean setFocus() {
        if (this.viewer == null || this.viewer.getControl() == null)
            return false;
        return this.viewer.getControl().setFocus();
    }
}
