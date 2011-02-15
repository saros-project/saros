package de.fu_berlin.inf.dpp.ui.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Implementations of this interface are used as elements for
 * {@link TreeContentProvider}s
 * 
 * @author bkahlert
 */
public interface ITreeElement extends IAdaptable {
    /**
     * @see ILabelProvider#getText(Object)
     */
    public String getText();

    /**
     * @see ILabelProvider#getImage(Object)
     */
    public Image getImage();

    /**
     * @see ITreeContentProvider#getParent(Object)
     */
    public ITreeElement getParent();

    /**
     * @see ITreeContentProvider#getChildren(Object)
     */
    public Object[] getChildren();

    /**
     * @see ITreeContentProvider#hasChildren(Object)
     */
    public boolean hasChildren();
}
