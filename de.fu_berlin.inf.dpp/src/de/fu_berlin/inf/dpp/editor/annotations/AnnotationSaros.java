package de.fu_berlin.inf.dpp.editor.annotations;

import org.eclipse.jface.text.source.Annotation;

public class AnnotationSaros extends Annotation {

	private String source;
	
	AnnotationSaros(String type, boolean isPersistent, String text, String source) {
		super(type, isPersistent, text);
		this.source=source;
	}

	AnnotationSaros(String type, boolean isPersistent, String text) {
		super(type, isPersistent, text);
		this.source=null;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
}
