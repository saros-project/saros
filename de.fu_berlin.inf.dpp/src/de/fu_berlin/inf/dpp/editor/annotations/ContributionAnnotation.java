package de.fu_berlin.inf.dpp.editor.annotations;

import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * Marks text contributions done by the driver.
 * 
 * @author rdjemili
 */
public class ContributionAnnotation extends AnnotationSaros {
    public static final String TYPE = "de.fu_berlin.inf.dpp.annotations.contribution";

    /** The model this annotation belongs to. */
    protected IAnnotationModel model;

    public ContributionAnnotation(String source, IAnnotationModel model) {
        super(ContributionAnnotation.TYPE, false, "", source);
        this.model = model;
    }

    public IAnnotationModel getModel() {
        return model;
    }
}
