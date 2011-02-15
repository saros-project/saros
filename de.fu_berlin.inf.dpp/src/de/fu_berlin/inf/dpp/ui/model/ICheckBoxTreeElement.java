package de.fu_berlin.inf.dpp.ui.model;

import org.eclipse.jface.viewers.ICheckStateProvider;

/**
 * Implementations of this interface are used as elements for
 * {@link TreeContentProvider}s
 * 
 * @author bkahlert
 */
public interface ICheckBoxTreeElement extends ITreeElement {

    /**
     * @see ICheckStateProvider#isChecked(Object)
     */
    public boolean isChecked();

    /**
     * @see ICheckStateProvider#isGrayed(Object)
     */
    public boolean isGrayed();

}
