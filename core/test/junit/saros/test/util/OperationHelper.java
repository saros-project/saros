package saros.test.util;

import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.internal.text.DeleteOperation;
import saros.concurrent.jupiter.internal.text.InsertOperation;
import saros.concurrent.jupiter.internal.text.NoOperation;
import saros.concurrent.jupiter.internal.text.SplitOperation;
import saros.session.User;

/**
 * Helper class defining static methods to easily instantiate operation objects for testing
 * purposes.
 */
public class OperationHelper {

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
   * Returns an insert operation with the given parameters. The start position will also be used as
   * the origin position.
   *
   * @param position the position for the operation
   * @param text the text for the operation
   * @return an insert operation with the given parameters
   * @see #I(int, String, int)
   */
  public static InsertOperation I(int position, String text) {
    return I(position, text, position);
  }

  /**
   * Returns an insert operation with the given parameters.
   *
   * @param position the position for the operation
   * @param text the text for the operation
   * @param origin the origin for the operation
   * @return an insert operation with the given parameters
   * @see InsertOperation#InsertOperation(int, String, int)
   */
  public static InsertOperation I(int position, String text, int origin) {

    return new InsertOperation(position, text, origin);
  }

  /**
   * Returns a delete operation with the given parameters.
   *
   * @param position the position for the operation
   * @param text the text for the operation
   * @return an insert operation with the given parameters
   * @see DeleteOperation#DeleteOperation(int, String)
   */
  public static DeleteOperation D(int position, String text) {

    return new DeleteOperation(position, text);
  }

  /**
   * Returns a text edit activity with the given parameters.
   *
   * @param source the source to use for the activity
   * @param offset the offset to use for the activity
   * @param text the text to use for the activity
   * @param replacedText the replaced text to use for the activity
   * @param path the path to use for the activity
   * @return a text edit activity with the given parameters
   * @see TextEditActivity#TextEditActivity(User, int, String, String, SPath)
   */
  public static TextEditActivity T(
      User source, int offset, String text, String replacedText, SPath path) {

    return new TextEditActivity(source, offset, text, replacedText, path);
  }
}
