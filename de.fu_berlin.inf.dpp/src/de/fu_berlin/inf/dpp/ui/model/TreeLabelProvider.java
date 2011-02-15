package de.fu_berlin.inf.dpp.ui.model;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Instances of this class are used in conjunction with {@link ITreeElement}s as
 * generic {@link TreeLabelProvider}s
 * 
 * @author bkahlert
 */
public class TreeLabelProvider extends LabelProvider {
    @Override
    public String getText(Object element) {
        return (element instanceof ITreeElement) ? ((ITreeElement) element)
            .getText() : null;
    }

    @Override
    public Image getImage(Object element) {
        return (element instanceof ITreeElement) ? ((ITreeElement) element)
            .getImage() : null;
    }
}
