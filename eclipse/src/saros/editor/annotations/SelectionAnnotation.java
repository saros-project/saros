package saros.editor.annotations;

import saros.editor.internal.LocationAnnotationManager;
import saros.session.User;
import saros.ui.Messages;
import saros.ui.util.ModelFormatUtils;

/**
 * Mimics the text cursor and selection of other session participants.
 *
 * <p>Such annotations are organized by the {@link LocationAnnotationManager}; their visual
 * appearance is configured in <code>plugin.xml</code>.
 */
public class SelectionAnnotation extends SarosAnnotation {

  protected static final String TYPE = "saros.annotations.selection";

  public SelectionAnnotation(User source, boolean isCursor) {
    super(
        SelectionAnnotation.TYPE,
        true,
        ModelFormatUtils.format(
            isCursor
                ? Messages.SelectionAnnotation_cursor_of
                : Messages.SelectionAnnotation_selection_of,
            source),
        source);
  }
}
