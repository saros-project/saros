package de.fu_berlin.inf.dpp.editor.annotations;

import de.fu_berlin.inf.dpp.editor.internal.LocationAnnotationManager;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.ModelFormatUtils;

/**
 * Mimics the text cursor and selection of other session participants.
 *
 * <p>Such annotations are organized by the {@link LocationAnnotationManager}; their visual
 * appearance is configured in <code>plugin.xml</code>.
 */
public class SelectionAnnotation extends SarosAnnotation {

  protected static final String TYPE = "de.fu_berlin.inf.dpp.annotations.selection";

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
