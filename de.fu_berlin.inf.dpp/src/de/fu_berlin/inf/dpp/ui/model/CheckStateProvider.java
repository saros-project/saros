package de.fu_berlin.inf.dpp.ui.model;

import org.eclipse.jface.viewers.ICheckStateProvider;

/**
 * Implements a generic {@link ICheckStateProvider}.
 * 
 * @author bkahlert
 */
public class CheckStateProvider implements ICheckStateProvider {

    public boolean isChecked(Object element) {
        return (element instanceof ICheckBoxTreeElement) ? ((ICheckBoxTreeElement) element)
            .isChecked() : false;
    }

    public boolean isGrayed(Object element) {
        return (element instanceof ICheckBoxTreeElement) ? ((ICheckBoxTreeElement) element)
            .isGrayed() : false;
    }

}
