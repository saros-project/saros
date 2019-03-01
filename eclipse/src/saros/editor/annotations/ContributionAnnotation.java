package saros.editor.annotations;

import org.eclipse.jface.text.source.IAnnotationModel;
import saros.editor.internal.ContributionAnnotationManager;
import saros.session.User;
import saros.ui.Messages;
import saros.ui.util.ModelFormatUtils;

/**
 * Marks textual additions and changes of other users. They are meant to inform the user of recent
 * changes made by the other session participants.
 *
 * <p>The {@link ContributionAnnotationManager} takes care of adding and removing these annotations.
 * Their visual appearance is configured in <code>plugin.xml</code>.
 */
public class ContributionAnnotation extends SarosAnnotation {

  protected static final String TYPE = "saros.annotations.contribution";

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
