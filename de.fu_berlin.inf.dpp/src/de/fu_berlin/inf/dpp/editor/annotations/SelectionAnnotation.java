package de.fu_berlin.inf.dpp.editor.annotations;

import org.eclipse.jface.text.source.Annotation;

/**
 * Marks text selected by both driver and observer.
 * 
 * Configuration of this annotation is done in the plugin-xml
 * 
 * @author coezbek
 */
public class SelectionAnnotation extends Annotation {

	public static final String TYPE = "de.fu_berlin.inf.dpp.annotations.selection";

	public SelectionAnnotation() {
		this("");
	}

	public SelectionAnnotation(String username) {
		super(TYPE, false, username);
	}
}
