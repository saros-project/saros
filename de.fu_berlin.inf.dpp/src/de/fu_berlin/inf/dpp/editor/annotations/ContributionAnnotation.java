package de.fu_berlin.inf.dpp.editor.annotations;

import org.eclipse.jface.text.source.IAnnotationModel;

import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.ModelFormatUtils;

/**
 * Marks text contributions done by a user with {@link Permission#WRITE_ACCESS}.
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
        super(ContributionAnnotation.TYPE, true, ModelFormatUtils.format(
            Messages.ContributionAnnotation_text_contributed_by, source),
            source);
        this.model = model;
    }

    public IAnnotationModel getModel() {
        return model;
    }
}
