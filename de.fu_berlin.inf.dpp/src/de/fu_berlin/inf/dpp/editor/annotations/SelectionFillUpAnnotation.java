package de.fu_berlin.inf.dpp.editor.annotations;

import de.fu_berlin.inf.dpp.session.User;

/**
 * This Annotation amends the {@link SelectionAnnotation}. It fills up highlighted lines to the
 * right margin, thus making them a proper selection block. For details concerning the actual
 * drawing see {@link SelectionFillUpStrategy}.
 */
public class SelectionFillUpAnnotation extends SarosAnnotation {

  public static final String TYPE =
      "de.fu_berlin.inf.dpp.editor.annotations.SelectionFillUpAnnotation";

  private int length;
  private int offset;

  public SelectionFillUpAnnotation(User user, int offset, int length) {
    super(TYPE, true, "SelectionFillUpAnnotation", user);

    this.offset = offset;
    this.length = length;
  }

  public int getLength() {
    return length;
  }

  public int getOffset() {
    return offset;
  }

  /**
   * @return <code>true</code> if drawing this Annotation should only clean the canvas and there
   *     should be no attempt to draw anything. This usually indicates that the user deselected some
   *     text, e.g. by clicking somewhere.
   */
  public boolean isDeselection() {
    return (length == 0);
  }
}
