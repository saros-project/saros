package de.fu_berlin.inf.dpp.ui.model.project;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * {@link IContentProvider} that extends {@link WorkbenchContentProvider} in
 * order to always refresh the whole {@link Viewer}.<br/>
 * {@link WorkbenchContentProvider} does not properly update non
 * {@link AbstractTreeViewer}s.<br/>
 * Updating the whole {@link Viewer} avoids the problem.</li> </ol>
 * 
 * @see WorkbenchContentProvider
 * @author bkahlert
 */
public class ProjectOnlyWorkbenchContentProvider extends
    WorkbenchContentProvider {

    protected Viewer viewer;

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.viewer = viewer;
        super.inputChanged(viewer, oldInput, newInput);
    }

    @Override
    protected void processDelta(IResourceDelta delta) {
        Control ctrl = viewer.getControl();
        if (ctrl == null || ctrl.isDisposed()) {
            return;
        }

        // Are we in the UIThread?
        if (ctrl.getDisplay().getThread() == Thread.currentThread()) {
            viewer.refresh();
        } else {
            ctrl.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    // Abort if this happens after disposes
                    Control ctrl = viewer.getControl();
                    if (ctrl == null || ctrl.isDisposed()) {
                        return;
                    }

                    viewer.refresh();
                }
            });
        }
    }
}
