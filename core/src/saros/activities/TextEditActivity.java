package saros.activities;

import static saros.util.LineSeparatorNormalizationUtil.NORMALIZED_LINE_SEPARATOR;

import java.util.Objects;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.internal.text.DeleteOperation;
import saros.concurrent.jupiter.internal.text.InsertOperation;
import saros.concurrent.jupiter.internal.text.NoOperation;
import saros.concurrent.jupiter.internal.text.SplitOperation;
import saros.editor.text.TextPosition;
import saros.editor.text.TextPositionUtils;
import saros.filesystem.IFile;
import saros.session.User;
import saros.util.LineSeparatorNormalizationUtil;

/**
 * An immutable TextEditActivity.
 *
 * <p>The replaced and new text contained in text edit activities only uses normalized line
 * separators, meaning it doesn't contain any line separators besides the {@link
 * LineSeparatorNormalizationUtil#NORMALIZED_LINE_SEPARATOR}.
 *
 * @see LineSeparatorNormalizationUtil
 */
public class TextEditActivity extends AbstractResourceActivity<IFile> {

  private static final Logger log = Logger.getLogger(TextEditActivity.class);

  protected final String newText;
  protected final String replacedText;

  private final TextPosition startPosition;

  private final int newTextLineDelta;
  private final int newTextOffsetDelta;

  private final int replacedTextLineDelta;
  private final int replacedTextOffsetDelta;

  /**
   * Calculates the line and offset deltas for the given new and replaced text and instantiates a
   * new text edit activity with the given parameters and the calculated deltas.
   *
   * <p>The given replaced and new text must only use normalized line separators, meaning it must
   * not contain any line separators besides the {@link
   * LineSeparatorNormalizationUtil#NORMALIZED_LINE_SEPARATOR}.
   *
   * <p>Uses the normalization line separator to calculate the line delta and offset delta of the
   * given new and replaced text.
   *
   * @param source the user that caused the activity
   * @param startPosition the position at which the text edit activity applies
   * @param newText the new text added with this activity
   * @param replacedText the replaced text removed with this activity
   * @param file the file the activity belongs to
   * @see TextPositionUtils#calculateDeltas(String,String)
   * @see LineSeparatorNormalizationUtil
   */
  public static TextEditActivity buildTextEditActivity(
      User source, TextPosition startPosition, String newText, String replacedText, IFile file) {

    Pair<Integer, Integer> newTextDeltas =
        TextPositionUtils.calculateDeltas(newText, NORMALIZED_LINE_SEPARATOR);

    int newTextLineDelta = newTextDeltas.getLeft();
    int newTextOffsetDelta = newTextDeltas.getRight();

    Pair<Integer, Integer> replacedTextDeltas =
        TextPositionUtils.calculateDeltas(replacedText, NORMALIZED_LINE_SEPARATOR);

    int replacedTextLineDelta = replacedTextDeltas.getLeft();
    int replacedTextOffsetDelta = replacedTextDeltas.getRight();

    return new TextEditActivity(
        source,
        startPosition,
        newTextLineDelta,
        newTextOffsetDelta,
        newText,
        replacedTextLineDelta,
        replacedTextOffsetDelta,
        replacedText,
        file);
  }

  /**
   * Instantiates a new text edit activity with the given parameters.
   *
   * <p>The given replaced and new text must only use normalized line separators, meaning it must
   * not contain any line separators besides the {@link
   * LineSeparatorNormalizationUtil#NORMALIZED_LINE_SEPARATOR}.
   *
   * @param source the user that caused the activity
   * @param startPosition the position at which the text edit activity applies
   * @param newTextLineDelta the number of lines added with the new text
   * @param newTextOffsetDelta the offset delta in the last line of the new text
   * @param newText the new text added with this activity
   * @param replacedTextLineDelta the number of lines removed with the replaced text
   * @param replacedTextOffsetDelta the offset delta in the last line of the replaced text
   * @param replacedText the replaced text removed with this activity
   * @param file the file the activity belongs to
   * @see LineSeparatorNormalizationUtil
   */
  public TextEditActivity(
      User source,
      TextPosition startPosition,
      int newTextLineDelta,
      int newTextOffsetDelta,
      String newText,
      int replacedTextLineDelta,
      int replacedTextOffsetDelta,
      String replacedText,
      IFile file) {

    super(source, file);

    if (startPosition == null || !startPosition.isValid())
      throw new IllegalArgumentException("Start position must be valid");

    if (newTextLineDelta < 0)
      throw new IllegalArgumentException("New text line delta must not be negative");
    if (newTextOffsetDelta < 0)
      throw new IllegalArgumentException("New text offset delta must not be negative");

    if (replacedTextLineDelta < 0)
      throw new IllegalArgumentException("Replaced text line delta must not be negative");
    if (replacedTextOffsetDelta < 0)
      throw new IllegalArgumentException("Replaced text offset delta must not be negative");

    if (newText == null) throw new IllegalArgumentException("Text must not be null");
    if (replacedText == null) throw new IllegalArgumentException("ReplacedText must not be null");

    assert !newText.contains("\r\n");
    assert !replacedText.contains("\r\n");

    if (file == null) throw new IllegalArgumentException("Resource must not be null");

    this.startPosition = startPosition;

    this.newTextLineDelta = newTextLineDelta;
    this.newTextOffsetDelta = newTextOffsetDelta;

    this.newText = newText;

    this.replacedTextLineDelta = replacedTextLineDelta;
    this.replacedTextOffsetDelta = replacedTextOffsetDelta;

    this.replacedText = replacedText;
  }

  /**
   * Returns the position at which the text edit activity applies.
   *
   * @return the position at which the text edit activity applies
   */
  public TextPosition getStartPosition() {
    return startPosition;
  }

  /**
   * Returns the position at which the new text added by this activity ends.
   *
   * @return the position at which the new text added by this activity ends
   */
  public TextPosition getNewEndPosition() {
    if (newTextLineDelta == 0) {
      int lineNumber = startPosition.getLineNumber();
      int inLineOffset = startPosition.getInLineOffset() + newTextOffsetDelta;

      return new TextPosition(lineNumber, inLineOffset);

    } else {
      int lineNumber = startPosition.getLineNumber() + newTextLineDelta;

      return new TextPosition(lineNumber, newTextOffsetDelta);
    }
  }

  /**
   * Returns the new text added by this text activity.
   *
   * <p>The returned text only uses normalized line separators, meaning it doesn't contain any line
   * separators besides the {@link LineSeparatorNormalizationUtil#NORMALIZED_LINE_SEPARATOR}.
   *
   * @return the new text added by this text activity
   * @see LineSeparatorNormalizationUtil
   */
  public String getNewText() {
    return newText;
  }

  /**
   * Returns the replaced text removed by this text activity.
   *
   * <p>The returned text only uses normalized line separators, meaning it doesn't contain any line
   * separators besides the {@link LineSeparatorNormalizationUtil#NORMALIZED_LINE_SEPARATOR}.
   *
   * @return the replaced text removed by this text activity
   * @see LineSeparatorNormalizationUtil
   */
  public String getReplacedText() {
    return replacedText;
  }

  @Override
  public String toString() {
    String newText = StringEscapeUtils.escapeJava(StringUtils.abbreviate(this.newText, 150));
    String oldText = StringEscapeUtils.escapeJava(StringUtils.abbreviate(replacedText, 150));
    return "TextEditActivity(start: "
        + startPosition
        + ", new text line delta: "
        + newTextLineDelta
        + ", new text offset delta: "
        + newTextOffsetDelta
        + ", new: '"
        + newText
        + "', replaced text line delta: "
        + replacedTextLineDelta
        + ", replaced text offset delta: "
        + replacedTextOffsetDelta
        + ", old: '"
        + oldText
        + "', file: "
        + getResource()
        + ", src: "
        + getSource()
        + ")";
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        startPosition,
        newTextLineDelta,
        newTextOffsetDelta,
        newText,
        replacedTextLineDelta,
        replacedTextOffsetDelta,
        replacedText);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (!(obj instanceof TextEditActivity)) return false;

    TextEditActivity other = (TextEditActivity) obj;

    return Objects.equals(this.startPosition, other.startPosition)
        && this.newTextLineDelta == other.newTextLineDelta
        && this.newTextOffsetDelta == other.newTextOffsetDelta
        && Objects.equals(this.newText, other.newText)
        && this.replacedTextLineDelta == other.replacedTextLineDelta
        && this.replacedTextOffsetDelta == other.replacedTextOffsetDelta
        && Objects.equals(this.replacedText, other.replacedText);
  }

  /**
   * Convert this text edit activity to a matching Operation.
   *
   * @see InsertOperation
   * @see DeleteOperation
   * @see SplitOperation
   */
  public Operation toOperation() {

    // delete Activity
    if ((replacedText.length() > 0) && (newText.length() == 0)) {
      return new DeleteOperation(
          startPosition, replacedTextLineDelta, replacedTextOffsetDelta, replacedText);
    }

    // insert Activity
    if ((replacedText.length() == 0) && (newText.length() > 0)) {
      return new InsertOperation(startPosition, newTextLineDelta, newTextOffsetDelta, newText);
    }

    // replace operation has to be split into delete and insert operation
    //noinspection ConstantConditions
    if ((replacedText.length() > 0) && (newText.length() > 0)) {
      return new SplitOperation(
          new DeleteOperation(
              startPosition, replacedTextLineDelta, replacedTextOffsetDelta, replacedText),
          new InsertOperation(startPosition, newTextLineDelta, newTextOffsetDelta, newText));
    }

    log.warn("NoOp Text edit: new '" + newText + "' old '" + replacedText + "'");
    return new NoOperation();
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }
}
