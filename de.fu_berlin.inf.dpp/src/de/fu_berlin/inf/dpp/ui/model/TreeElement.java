package de.fu_berlin.inf.dpp.ui.model;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Image;

/**
 * This class implements a default {@link ITreeElement}
 * 
 * @author bkahlert
 */
public abstract class TreeElement implements ITreeElement {

    public String getText() {
        return null;
    }

    public Image getImage() {
        return null;
    }

    public ITreeElement getParent() {
        return null;
    }

    public Object[] getChildren() {
        return new Object[0];
    }

    public boolean hasChildren() {
        return false;
    }

    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
        IAdapterManager manager = Platform.getAdapterManager();
        return manager.getAdapter(this, adapter);
    }

}
