package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.util.Objects;
import saros.editor.text.TextPosition;
import saros.editor.text.TextSelection;
import saros.filesystem.IFile;
import saros.session.User;

@XStreamAlias("textSelectionActivity")
public class TextSelectionActivity extends AbstractResourceActivity<IFile> {

  @XStreamAlias("sl")
  @XStreamAsAttribute
  private final int startLine;

  @XStreamAlias("so")
  @XStreamAsAttribute
  private final int startInLineOffset;

  @XStreamAlias("el")
  @XStreamAsAttribute
  private final int endLine;

  @XStreamAlias("eo")
  @XStreamAsAttribute
  private final int endInLineOffset;

  @XStreamAlias("bs")
  @XStreamAsAttribute
  private final boolean isBackwardsSelection;

  /**
   * Instantiates a new text selection activity.
   *
   * @param source the user that created the selection
   * @param selection the text selection
   * @param file the file the text was selected in
   * @throws IllegalArgumentException if the source or file is <code>null</code>
   */
  public TextSelectionActivity(User source, TextSelection selection, IFile file) {

    super(source, file);

    if (file == null) throw new IllegalArgumentException("file must not be null");

    TextPosition startPosition = selection.getStartPosition();
    this.startLine = startPosition.getLineNumber();
    this.startInLineOffset = startPosition.getInLineOffset();

    TextPosition endPosition = selection.getEndPosition();
    this.endLine = endPosition.getLineNumber();
    this.endInLineOffset = endPosition.getInLineOffset();

    this.isBackwardsSelection = selection.isBackwardsSelection();
  }

  @Override
  public boolean isValid() {
    return super.isValid() && (getResource() != null);
  }

  /**
   * Returns the text selection contained in this activity.
   *
   * @return the text selection contained in the activity
   */
  public TextSelection getSelection() {
    if (startLine == -1 && startInLineOffset == -1 && endLine == -1 && endInLineOffset == -1) {
      return TextSelection.EMPTY_SELECTION;

    } else if (startLine == -1
        || startInLineOffset == -1
        || endLine == -1
        || endInLineOffset == -1) {
      throw new IllegalStateException(
          "Encountered illegal selection values - sl: "
              + startLine
              + ", so: "
              + startInLineOffset
              + ", el: "
              + endLine
              + ", eo: "
              + endInLineOffset);
    }

    TextPosition startPosition = new TextPosition(startLine, startInLineOffset);
    TextPosition endPosition = new TextPosition(endLine, endInLineOffset);

    return new TextSelection(startPosition, endPosition, isBackwardsSelection);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), startLine, startInLineOffset, endLine, endInLineOffset);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (!(obj instanceof TextSelectionActivity)) return false;

    TextSelectionActivity other = (TextSelectionActivity) obj;

    return this.startLine == other.startLine
        && this.startInLineOffset == other.startInLineOffset
        && this.endLine == other.endLine
        && this.endInLineOffset == other.endInLineOffset;
  }

  @Override
  public String toString() {
    return "TextSelectionActivity(start line: "
        + startLine
        + ", in-line offset: "
        + startInLineOffset
        + ", end line: "
        + endLine
        + ", in-line offset: "
        + endInLineOffset
        + ", is backwards: "
        + isBackwardsSelection
        + ", src: "
        + getSource()
        + ", file: "
        + getResource()
        + ")";
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    /**
     * @JTourBusStop 13, Activity sending, Third dispatch:
     *
     * <p>Each specific activity implementation does the same simple third dispatch: It uses the
     * given receiver to deliver itself to it, so the IActivityReceiver implementation gets a
     * correctly typed activity without having to use "instanceof" constructions.
     */
    receiver.receive(this);
  }
}
