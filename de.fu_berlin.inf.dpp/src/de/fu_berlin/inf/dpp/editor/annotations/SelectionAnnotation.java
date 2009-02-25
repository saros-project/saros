package de.fu_berlin.inf.dpp.editor.annotations;

/**
 * Marks text selected by both driver and observer.
 * 
 * Configuration of this annotation is done in the plugin-xml.
 * 
 * @author coezbek
 */
public class SelectionAnnotation extends SarosAnnotation {

    // TODO Make this field protected.
    public static final String TYPE = "de.fu_berlin.inf.dpp.annotations.selection";

    public SelectionAnnotation(String source) {
        super(SelectionAnnotation.TYPE, true, createLabel("Selection of",
            source), source);
    }
}
