package de.fu_berlin.inf.dpp.ui.model;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;


/**
 * Empty element for use with {@link Viewer Viewers}
 * 
 * @author bkahlert
 */
public class EmptyElement extends TreeElement {

    protected String text;

    public EmptyElement(String text) {
        this.text = text;
    }

    @Override
    public StyledString getStyledText() {
        return new StyledString(this.text);
    }
}
