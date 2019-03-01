package de.fu_berlin.inf.dpp.editor.annotations;

import de.fu_berlin.inf.dpp.editor.internal.ContributionAnnotationManager;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.ModelFormatUtils;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * Marks textual additions and changes of other users. They are meant to inform the user of recent
 * changes made by the other session participants.
 *
 * <p>The {@link ContributionAnnotationManager} takes care of adding and removing these annotations.
 * Their visual appearance is configured in <code>plugin.xml</code>.
 */
public class ContributionAnnotation extends SarosAnnotation {

  protected static final String TYPE = "de.fu_berlin.inf.dpp.annotations.contribution";

  /** The model this annotation belongs to. */
  protected IAnnotationModel model;

  public ContributionAnnotation(User source, IAnnotationModel model) {
    super(
        ContributionAnnotation.TYPE,
        true,
        ModelFormatUtils.format(Messages.ContributionAnnotation_text_contributed_by, source),
        source);
    this.model = model;
  }

  public IAnnotationModel getModel() {
    return model;
  }
}
