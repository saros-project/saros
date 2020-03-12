package saros.test.util;

import org.apache.commons.lang3.tuple.Pair;
import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.internal.text.DeleteOperation;
import saros.concurrent.jupiter.internal.text.InsertOperation;
import saros.concurrent.jupiter.internal.text.NoOperation;
import saros.concurrent.jupiter.internal.text.SplitOperation;
import saros.editor.text.TextPosition;
import saros.editor.text.TextPositionUtils;
import saros.session.User;

/**
 * Helper class defining static methods to easily instantiate operation objects for testing
 * purposes.
 */
public class OperationHelper {

  /** Line separator assumed to be used by test documents. */
  public static final String LINE_SEPARATOR = "\n";

  /**
   * Returns a no-operation.
   *
   * @return a no-operation
   */
  public static Operation NOP() {
    return new NoOperation();
  }

  /**
   * Returns a split operations containing the two passed operations.
   *
   * @param first the first operation
   * @param second the second operation
   * @return a split operations containing the two passed operations
   */
  public static SplitOperation S(Operation first, Operation second) {
    return new SplitOperation(first, second);
  }

  /**
   * Returns an insert operation with the given parameters. The start position will be located in
   * line 0.
   *
   * @param inLineOffset the in-line offset for the operation
   * @param text the text for the operation
   * @return an insert operation with the given parameters
   * @see #I(int, String, int)
   */
  public static InsertOperation I(int inLineOffset, String text) {
    return I(inLineOffset, text, inLineOffset);
  }

  /**
   * Returns an insert operation with the given parameters. The start and origin position will be
   * located in line 0.
   *
   * @param inLineOffset the in-line offset for the operation
   * @param text the text for the operation
   * @param originInLineOffset the origin in-line offset for the operation
   * @return an insert operation with the given parameters
   * @see #I(int, int, String, int, int)
   */
  public static InsertOperation I(int inLineOffset, String text, int originInLineOffset) {
    return I(0, inLineOffset, text, 0, originInLineOffset);
  }

  /**
   * Returns an insert operation with the given parameters.
   *
   * @param lineNumber the line number for the operation
   * @param inLineOffset the in-line offset for the operation
   * @param text the text for the operation
   * @param originLineNumber the origin line number for the operation
   * @param originInLineOffset the origin in-line offset for the operation
   * @return an insert operation with the given parameters
   * @see #I(TextPosition, String, TextPosition)
   */
  public static InsertOperation I(
      int lineNumber, int inLineOffset, String text, int originLineNumber, int originInLineOffset) {

    return I(
        new TextPosition(lineNumber, inLineOffset),
        text,
        new TextPosition(originLineNumber, originInLineOffset));
  }

  /**
   * Returns an insert operation with the given parameters.
   *
   * @param position the position for the operation
   * @param text the text for the operation
   * @param origin the origin for the operation
   * @return an insert operation with the given parameters
   * @see InsertOperation#InsertOperation(TextPosition, int,int, String, TextPosition)
   */
  public static InsertOperation I(TextPosition position, String text, TextPosition origin) {
    Pair<Integer, Integer> deltas = TextPositionUtils.calculateDeltas(text, LINE_SEPARATOR);

    int lineDelta = deltas.getLeft();
    int offsetDelta = deltas.getRight();

    return new InsertOperation(position, lineDelta, offsetDelta, text, origin);
  }

  /**
   * Returns a delete operation with the given parameters. The start position for the operation will
   * be located in line 0.
   *
   * @param inLineOffset the in-line offset for the operation
   * @param text the text for the operation
   * @return a delete operation with the given parameters
   * @see #D(int, int, String)
   */
  public static DeleteOperation D(int inLineOffset, String text) {
    return D(0, inLineOffset, text);
  }

  /**
   * Returns a delete operation with the given parameters.
   *
   * @param lineNumber the line number for the operation
   * @param inLineOffset the in-line offset for the operation
   * @param text the text for the operation
   * @return a delete operation with the given parameters
   * @see #D(TextPosition, String)
   */
  public static DeleteOperation D(int lineNumber, int inLineOffset, String text) {
    return D(new TextPosition(lineNumber, inLineOffset), text);
  }

  /**
   * Returns a delete operation with the given parameters.
   *
   * @param position the position for the operation
   * @param text the text for the operation
   * @return an insert operation with the given parameters
   * @see DeleteOperation#DeleteOperation(TextPosition, int, int, String)
   */
  public static DeleteOperation D(TextPosition position, String text) {
    Pair<Integer, Integer> deltas = TextPositionUtils.calculateDeltas(text, LINE_SEPARATOR);

    int lineDelta = deltas.getLeft();
    int offsetDelta = deltas.getRight();

    return new DeleteOperation(position, lineDelta, offsetDelta, text);
  }

  /**
   * Returns a text edit activity with the given parameters. The start position for the operation
   * will be located in line 0.
   *
   * @param source the source to use for the activity
   * @param inLineOffset the in-line offset to use for the activity
   * @param text the text to use for the activity
   * @param replacedText the replaced text to use for the activity
   * @param path the path to use for the activity
   * @return a text edit activity with the given parameters
   * @see #T(User, TextPosition, String, String, SPath)
   */
  public static TextEditActivity T(
      User source, int inLineOffset, String text, String replacedText, SPath path) {

    TextPosition startPosition = new TextPosition(0, inLineOffset);

    return T(source, startPosition, text, replacedText, path);
  }

  /**
   * Returns a text edit activity with the given parameters.
   *
   * @param source the source to use for the activity
   * @param position the position to use for the activity
   * @param text the text to use for the activity
   * @param replacedText the replaced text to use for the activity
   * @param path the path to use for the activity
   * @return a text edit activity with the given parameters
   * @see TextEditActivity#buildTextEditActivity(User, TextPosition, String, String, SPath, String)
   */
  public static TextEditActivity T(
      User source, TextPosition position, String text, String replacedText, SPath path) {

    return TextEditActivity.buildTextEditActivity(
        source, position, text, replacedText, path, LINE_SEPARATOR);
  }
}
