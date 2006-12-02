package de.fu_berlin.inf.dpp.editor.annotations;

import org.eclipse.jface.text.source.Annotation;

/**
 * Marks text contributions done by the driver.
 * 
 * @author rdjemili
 */
public class ContributionAnnotation extends Annotation {
	public static final String TYPE = "de.fu_berlin.inf.dpp.annotations.contribution";

	public ContributionAnnotation() {
		this("");
	}

	public ContributionAnnotation(String username) {
		super(TYPE, false, username);
	}
}
