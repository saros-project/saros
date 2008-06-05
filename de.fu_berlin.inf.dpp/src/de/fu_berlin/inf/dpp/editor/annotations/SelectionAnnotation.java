package de.fu_berlin.inf.dpp.editor.annotations;

import de.fu_berlin.inf.dpp.*;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * Marks text selected by both driver and observer.
 * 
 * Configuration of this annotation is done in the plugin-xml
 * 
 * @author coezbek
 */
public class SelectionAnnotation extends AnnotationSaros  {

	// base TYPE name, will be extended for different remote users
	public static final String TYPE = "de.fu_berlin.inf.dpp.annotations.selection";
	
	public SelectionAnnotation() {
		this(null,null);
	}

	public SelectionAnnotation(String label, String username) {
		super(TYPE, false, label,username);	
		
	}

	
}
