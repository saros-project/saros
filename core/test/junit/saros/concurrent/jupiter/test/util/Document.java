package saros.concurrent.jupiter.test.util;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import saros.activities.TextEditActivity;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.internal.text.ITextOperation;
import saros.concurrent.jupiter.internal.text.SplitOperation;
import saros.editor.text.TextPositionUtils;
import saros.filesystem.IFile;
import saros.session.User;
import saros.test.util.OperationHelper;

/** this class represent a document object for testing. */
public class Document {

  /** Listener for jupiter document actions. */
  public interface JupiterDocumentListener {

    public void documentAction(User user);

    public String getUser();
  }

  private static final Logger log = Logger.getLogger(Document.class.getName());

  /** document state. */
  private final StringBuffer doc;

  private final IFile file;

  /**
   * constructor to init doc.
   *
   * @param initState start document state.
   */
  public Document(String initState, IFile file) {
    this.doc = new StringBuffer(initState);
    this.file = file;
  }

  /**
   * return string representation of current doc state.
   *
   * @return string of current doc state.
   */
  public String getDocument() {
    return doc.toString();
  }

  @Override
  public String toString() {
    return doc.toString();
  }

  /**
   * Execute Operation on document state.
   *
   * @param op the operation to execute
   */
  public void execOperation(Operation op) {
    User dummy = JupiterTestCase.createUser("dummy");

    checkOperationDeltas(op);

    List<TextEditActivity> activities = op.toTextEdit(file, dummy);

    String lineSeparator = OperationHelper.EOL;

    for (TextEditActivity activity : activities) {

      int start =
          TextPositionUtils.calculateOffset(
              doc.toString(), activity.getStartPosition(), lineSeparator);
      int end = start + activity.getReplacedText().length();

      String is = doc.toString().substring(start, end);

      if (!is.equals(activity.getReplacedText())) {
        log.warn("Text should be '" + activity.getReplacedText() + "' is '" + is + "'");
        throw new RuntimeException(
            "Text should be '" + activity.getReplacedText() + "' is '" + is + "'");
      }

      doc.replace(start, end, activity.getNewText());
    }
  }

  /**
   * Checks that the line and offset delta of all contained text operations are valid.
   *
   * <p>This check is done to ensure that the deltas for all operations are calculated correctly.
   * Differences in line or offset delta won't lead to issues when applying the operation to the
   * text but would lead to issue when further transforming the operation or using it to transform
   * other operations. As further transformations are not always done as part of the test setup, the
   * deltas are checked explicitly.
   *
   * @param op the operation to check
   * @throws IllegalStateException if the line or offset delta of a contained text operation does
   *     not match its text
   */
  private void checkOperationDeltas(Operation op) {
    if (op instanceof SplitOperation) {
      SplitOperation splitOperation = (SplitOperation) op;

      checkOperationDeltas(splitOperation.getFirst());
      checkOperationDeltas(splitOperation.getSecond());

    } else if (op instanceof ITextOperation) {
      ITextOperation textOperation = (ITextOperation) op;

      String text = textOperation.getText();

      String lineSeparator = TextPositionUtils.guessLineSeparator(text);
      Pair<Integer, Integer> expectedDeltas =
          TextPositionUtils.calculateDeltas(text, lineSeparator);
      int expectedLineDelta = expectedDeltas.getLeft();
      int expectedOffsetDelta = expectedDeltas.getRight();

      int actualLineDelta = textOperation.getLineDelta();

      if (actualLineDelta != expectedLineDelta) {
        throw new IllegalStateException(
            "wrong line delta for text operation; expected: "
                + expectedLineDelta
                + ", actual: "
                + actualLineDelta
                + "; operation: "
                + op);
      }

      int actualOffsetDelta = textOperation.getOffsetDelta();

      if (actualOffsetDelta != expectedOffsetDelta) {
        throw new IllegalStateException(
            "wrong offset delta for text operation; expected: "
                + expectedOffsetDelta
                + ", actual: "
                + actualOffsetDelta
                + "; operation: "
                + op);
      }
    }
  }
}
