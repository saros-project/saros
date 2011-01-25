package de.fu_berlin.inf.dpp.editor.annotations;

import org.eclipse.jface.text.source.IAnnotationModel;

import de.fu_berlin.inf.dpp.User;

/**
 * Marks text contributions done by a user with
 * {@link User.Permission#WRITE_ACCESS}.
 * 
 * Configuration of this annotation is done in the plugin-xml.
 * 
 * @author rdjemili
 */
public class ContributionAnnotation extends SarosAnnotation {

    protected static final String TYPE = "de.fu_berlin.inf.dpp.annotations.contribution";

    /**
     * The model this annotation belongs to.
     */
    protected IAnnotationModel model;

    public ContributionAnnotation(User source, IAnnotationModel model) {
        super(ContributionAnnotation.TYPE, true, "Text contributed by "
            + source.getHumanReadableName(), source);
        this.model = model;
    }

    public IAnnotationModel getModel() {
        return model;
    }
}
