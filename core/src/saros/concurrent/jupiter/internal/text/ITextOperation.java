package saros.concurrent.jupiter.internal.text;

import saros.concurrent.jupiter.Operation;
import saros.editor.text.TextPosition;

/** An ITextOperation is an operation which describes a text change. */
public interface ITextOperation extends Operation {

  /**
   * Returns the text to be removed/added by the operation.
   *
   * @return the text to be removed/added by the operation
   */
  String getText();

  /**
   * Returns the start position of the operation.
   *
   * @return the start position of the operation
   */
  TextPosition getStartPosition();

  /**
   * Returns the end position of the text modified by the operation.
   *
   * @return the end position of the operation
   */
  TextPosition getEndPosition();

  /**
   * Returns the line delta for the operation. The line delta describes how many lines are
   * removed/added by the operation.
   *
   * @return the line delta for the operation
   */
  int getLineDelta();

  /**
   * Returns the offset delta for the operation.
   *
   * <p>If the operation text does not contain any line breaks (lineDelta=0), this is the relative
   * offset delta to the start of the operation.
   *
   * <p>If the operation text does contain line breaks (lineDelta>0), this is the (absolute) in-line
   * offset in the last line of the operation text.
   */
  int getOffsetDelta();
}
