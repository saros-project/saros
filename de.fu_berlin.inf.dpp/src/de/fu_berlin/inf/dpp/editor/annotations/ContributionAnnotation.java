package de.fu_berlin.inf.dpp.editor.annotations;

import org.eclipse.jface.text.source.Annotation;

public class ContributionAnnotation extends Annotation {
    public static final String TYPE = "de.fu_berlin.inf.dpp.annotation.user";
    
    public ContributionAnnotation(String username) {
        super(TYPE, false, username);
    }
}
