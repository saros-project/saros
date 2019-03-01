package saros.editor.annotations;

import saros.session.User;

/**
 * {@link SarosAnnotation} to use in combination with the {@link RemoteCursorStrategy}. This is only
 * used to identify the corresponding Strategy in a reasonable way.
 */
public class RemoteCursorAnnotation extends SarosAnnotation {

  public static final String TYPE = "saros.editor.annotations.RemoteCursorAnnotation";

  public RemoteCursorAnnotation(User source) {
    super(TYPE, false, "RemoteCursorAnnotation", source);
  }
}
