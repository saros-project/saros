package de.fu_berlin.inf.dpp.editor.annotations;

import de.fu_berlin.inf.dpp.session.User;

/**
 * {@link SarosAnnotation} to use in combination with the {@link RemoteCursorStrategy}. This is only
 * used to identify the corresponding Strategy in a reasonable way.
 */
public class RemoteCursorAnnotation extends SarosAnnotation {

  public static final String TYPE =
      "de.fu_berlin.inf.dpp.editor.annotations.RemoteCursorAnnotation";

  public RemoteCursorAnnotation(User source) {
    super(TYPE, false, "RemoteCursorAnnotation", source);
  }
}
