package de.fu_berlin.inf.dpp.ui.model.workaround;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Tree;

/**
 * This class is a workaround for a bug in the {@link CheckboxTreeViewer}. If a
 * filter is applied to the viewer it looses the item's check states.
 * <p>
 * This class preserves the item's check states.
 * 
 * @author bkahlert
 */
public class CheckStatePreservingCheckboxTreeViewer extends CheckboxTreeViewer {

    public CheckStatePreservingCheckboxTreeViewer(Tree tree) {
        super(tree);
    }

    /**
     * Saves the checked items, runs the runnable and re-sets the checked items.
     * 
     * @param runnable
     */
    protected void preserveCheckStates(Runnable runnable) {
        Object[] checkElements = this.getCheckedElements();
        runnable.run();
        this.setCheckedElements(checkElements);
    }

    @Override
    public void addFilter(final ViewerFilter filter) {
        preserveCheckStates(new Runnable() {
            public void run() {
                CheckStatePreservingCheckboxTreeViewer.super.addFilter(filter);
            }
        });
    }

    @Override
    public void removeFilter(final ViewerFilter filter) {
        preserveCheckStates(new Runnable() {
            public void run() {
                CheckStatePreservingCheckboxTreeViewer.super
                    .removeFilter(filter);
            }
        });
    }

}
