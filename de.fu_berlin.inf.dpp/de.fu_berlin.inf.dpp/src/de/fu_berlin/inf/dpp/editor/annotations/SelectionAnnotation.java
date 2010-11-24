package de.fu_berlin.inf.dpp.editor.annotations;

import de.fu_berlin.inf.dpp.User;

/**
 * Marks text selected by both driver and observer.
 * 
 * Configuration of this annotation is done in the plugin-xml.
 * 
 * @author coezbek
 */
public class SelectionAnnotation extends SarosAnnotation {

    protected static final String TYPE = "de.fu_berlin.inf.dpp.annotations.selection";

    public SelectionAnnotation(User source, boolean isCursor) {
        super(SelectionAnnotation.TYPE, true, (isCursor ? "Cursor"
            : "Selection")
            + " of " + source.getHumanReadableName(), source);
    }
}
