package de.fu_berlin.inf.dpp.editor.annotations;

import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * Marks text contributions done by a driver.
 * 
 * Configuration of this annotation is done in the plugin-xml.
 * 
 * @author rdjemili
 */
public class ContributionAnnotation extends SarosAnnotation {
    // TODO Make this field protected.
    public static final String TYPE = "de.fu_berlin.inf.dpp.annotations.contribution";

    /** The model this annotation belongs to. */
    protected IAnnotationModel model;

    public ContributionAnnotation(String source, IAnnotationModel model) {
        super(ContributionAnnotation.TYPE, true, "", source);
        this.model = model;
    }

    public IAnnotationModel getModel() {
        return model;
    }
}
