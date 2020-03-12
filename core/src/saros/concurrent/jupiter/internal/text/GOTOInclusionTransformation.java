/*
 * $Id: GOTOInclusionTransformation.java 2434 2005-12-12 07:49:51Z sim $
 *
 * ace - a collaborative editor
 * Copyright (C) 2005 Mark Bigler, Simon Raess, Lukas Zbinden
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package saros.concurrent.jupiter.internal.text;

import java.security.InvalidParameterException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import saros.concurrent.jupiter.InclusionTransformation;
import saros.concurrent.jupiter.Operation;
import saros.editor.text.TextPosition;
import saros.editor.text.TextPositionUtils;

/**
 * Implementation of the GOTO operational transformation functions. The pseudo code can be found in
 * the paper "Achieving Convergence, Causality-preservation, and Intention-preservation in Real-time
 * cooperative Editing Systems" by Chengzheng Sun, Xiaohua Jia, Yanchun Zhang, Yun Yang, and David
 * Chen.
 */
public class GOTOInclusionTransformation implements InclusionTransformation {

  private static final Logger log = Logger.getLogger(GOTOInclusionTransformation.class.getName());

  /**
   * Transform operation <var>op1</var> in the context of another operation <var>op2</var>. The
   * transformed operation <var>op1'</var> is returned.
   *
   * @param op1 the operation to transform
   * @param op2 the operation which is the context for the other one
   * @param param a boolean flag to privilege the first operation <code>op1</code> (i.e. remains
   *     unchanged) when two insert operations are equivalent i.e. they have the same position and
   *     origin index.
   * @return the transformed operation <var>op1'</var>
   */
  @Override
  public Operation transform(Operation op1, Operation op2, Object param) {

    log.trace("Transform " + op1 + " in the context of " + op2 + " (privileged==" + param + ")");

    boolean privileged = (Boolean) param;

    /* A NoOperation is not affected. */
    if (op1 instanceof NoOperation) {
      return new NoOperation();
    }

    /* If the context is null, we return op1 unchanged. */
    if (op2 instanceof NoOperation) {
      return op1;
    }

    if (op1 instanceof SplitOperation) {
      /*
       * Given two operations s1 and s2 to be transformed in the context of op2, we need to
       * calculate s1' as t(s1, op2) and s2' as t(s2, op2') where op2' is t(op2, s1) <code>
       *          O
       *     s1 /   \ op2
       *      O       O
       * s2 /   \   / s1'
       *  O       O
       *    \   / s2'
       *      O
       * </code>
       */
      SplitOperation s = (SplitOperation) op1;
      return new SplitOperation(
          transform(s.getFirst(), op2, param),
          transform(s.getSecond(), transform(op2, s.getFirst(), !privileged), param));
    }
    if (op2 instanceof SplitOperation) {
      /*
       * Given an operation op1 to be transformed in the context of two operations s1 and s2, we
       * need to calculate op1'' as t(op1', s2) where op1' is t(op1, s1) <code>
       *           O
       *      s1 /   \ op1
       *       O       O
       *  s2 /   \   /
       *   O       O
       * op1'\   /
       *       O
       *      </code>
       */
      SplitOperation s = (SplitOperation) op2;
      return transform(transform(op1, s.getFirst(), param), s.getSecond(), param);
    }

    if (op1 instanceof InsertOperation) {
      if (op2 instanceof InsertOperation) {
        return transform((InsertOperation) op1, (InsertOperation) op2, privileged);
      }
      if (op2 instanceof DeleteOperation) {
        return transform((InsertOperation) op1, (DeleteOperation) op2);
      }
    }
    if (op1 instanceof DeleteOperation) {
      if (op2 instanceof InsertOperation) {
        return transform((DeleteOperation) op1, (InsertOperation) op2);
      }
      if (op2 instanceof DeleteOperation) {
        return transform((DeleteOperation) op1, (DeleteOperation) op2);
      }
    }
    throw new InvalidParameterException("op1: " + op1 + ", op2: " + op2);
  }

  @Override
  public TextPosition transformIndex(TextPosition textPosition, Operation op, Object param) {
    if (op instanceof SplitOperation) {
      /*
       * Given an operation op1 to be transformed in the context of two operations s1 and s2, we
       * need to calculate the result op1'' as t(op1', s2) where op1' is t(op1, s1).
       *
       *           O
       *      s1 /   \ op1
       *       O       O
       *  s2 /   \   /
       *   O       O
       * op1'\   /
       *       O
       */

      SplitOperation s = (SplitOperation) op;

      TextPosition transformedPosition = textPosition;

      transformedPosition = transformIndex(transformedPosition, s.getSecond(), param);
      transformedPosition = transformIndex(transformedPosition, s.getFirst(), param);

      return transformedPosition;

    } else if (op instanceof NoOperation) {
      return textPosition;

    } else if (op instanceof InsertOperation) {
      InsertOperation insertOperation = (InsertOperation) op;

      TextPosition insertStartPosition = insertOperation.getStartPosition();

      if (textPosition.compareTo(insertStartPosition) < 0) {
        /*
         * Text position is located before insert operation.
         *
         * -> No adjustment necessary.
         */

        return textPosition;
      }

      /*
       * Text position is located in or behind insert operation.
       *
       * -> Text position must be shifted right by the length of the text of the insert operation.
       */

      return shiftForInsertOperation(textPosition, insertOperation);

    } else if (op instanceof DeleteOperation) {
      DeleteOperation deleteOperation = (DeleteOperation) op;

      TextPosition deleteStartPosition = deleteOperation.getStartPosition();
      TextPosition deleteEndPosition = deleteOperation.getEndPosition();

      if (textPosition.compareTo(deleteStartPosition) <= 0) {
        /*
         * Text position is located before delete operation.
         *
         * -> No adjustment necessary.
         */

        return textPosition;

      } else if (textPosition.compareTo(deleteEndPosition) > 0) {
        /*
         * Text position is located after delete operation.
         *
         * -> Text position must be shifted left by the length of the text of the delete operation.
         */

        return shiftForDeleteOperation(textPosition, deleteOperation);

      } else {
        /*
         * Text position is located in delete operation.
         *
         * -> Text position is set to start of delete operation
         */

        return deleteStartPosition;
      }

    } else {
      throw new IllegalArgumentException("Unsupported Operation type: " + op);
    }
  }

  /**
   * Shifts the given text position right by the length of the given insert operation.
   *
   * <p><b>NOTE:</b> This method does not check whether such a shift is actually necessary. This
   * must be determined by the caller beforehand.
   *
   * <p><b>NOTE:</b> This method does not check its input. It is expected that such checks are done
   * by the caller.
   *
   * @param textPosition the text position to shift
   * @param insertOperation the insert operation to shift the text position for
   * @return the given text position shifted right by the length of the given insert operation
   */
  private TextPosition shiftForInsertOperation(
      TextPosition textPosition, InsertOperation insertOperation) {

    TextPosition startPositionInsert = insertOperation.getStartPosition();

    int lineNumberPosition = textPosition.getLineNumber();
    int inLineOffsetPosition = textPosition.getInLineOffset();

    int inLineOffsetInsertStart = startPositionInsert.getInLineOffset();

    int lineDeltaInsert = insertOperation.getLineDelta();
    int offsetDeltaInsert = insertOperation.getOffsetDelta();

    TextPosition newStartPosition;

    if (lineNumberPosition == startPositionInsert.getLineNumber()) {

      if (lineDeltaInsert == 0) {
        int newInLineOffset = inLineOffsetPosition + offsetDeltaInsert;

        newStartPosition = new TextPosition(lineNumberPosition, newInLineOffset);

      } else {
        int newLineNumber = lineNumberPosition + lineDeltaInsert;
        int newInLineOffset = (inLineOffsetPosition - inLineOffsetInsertStart) + offsetDeltaInsert;

        newStartPosition = new TextPosition(newLineNumber, newInLineOffset);
      }

    } else {
      int newLineNumber = lineNumberPosition + lineDeltaInsert;

      newStartPosition = new TextPosition(newLineNumber, inLineOffsetPosition);
    }

    return newStartPosition;
  }

  /**
   * Shifts the given text position left by the length of the given delete operation.
   *
   * <p><b>NOTE:</b> This method does not check whether such a shift is actually necessary. This
   * must be determined by the caller beforehand.
   *
   * <p><b>NOTE:</b> This method does not check its input. It is expected that such checks are done
   * by the caller.
   *
   * @param textPosition the text position to shift
   * @param deleteOperation the delete operation to shift the text position for
   * @return the given text position shifted left by the length of the given delete operation
   */
  private TextPosition shiftForDeleteOperation(
      TextPosition textPosition, DeleteOperation deleteOperation) {

    TextPosition startPositionDelete = deleteOperation.getStartPosition();
    TextPosition endPositionDelete = deleteOperation.getEndPosition();

    int lineNumberPosition = textPosition.getLineNumber();
    int inLineOffsetPosition = textPosition.getInLineOffset();

    int inLineOffsetStartDelete = startPositionDelete.getInLineOffset();

    int lineNumberEndDelete = endPositionDelete.getLineNumber();
    int inLineOffsetEndDelete = endPositionDelete.getInLineOffset();

    int lineDeltaDelete = deleteOperation.getLineDelta();

    TextPosition newStartPosition;

    if (lineNumberPosition == lineNumberEndDelete) {

      if (lineDeltaDelete == 0) {
        int newInLineOffset = inLineOffsetPosition - deleteOperation.getOffsetDelta();

        newStartPosition = new TextPosition(lineNumberPosition, newInLineOffset);

      } else {
        int newInLineOffset =
            (inLineOffsetPosition - inLineOffsetEndDelete) + inLineOffsetStartDelete;

        newStartPosition = new TextPosition(startPositionDelete.getLineNumber(), newInLineOffset);
      }

    } else {
      int newLineNumber = lineNumberPosition - lineDeltaDelete;

      newStartPosition = new TextPosition(newLineNumber, inLineOffsetPosition);
    }

    return newStartPosition;
  }

  protected Operation transform(
      InsertOperation insA, InsertOperation insB, boolean isTransformPrivileged) {

    TextPosition startA = insA.getStartPosition();
    TextPosition origA = insA.getOriginStartPosition();

    TextPosition startB = insB.getStartPosition();
    TextPosition origB = insB.getOriginStartPosition();

    if (startA.compareTo(startB) < 0
        || startA.compareTo(startB) == 0 && origA.compareTo(origB) < 0
        || startA.compareTo(startB) == 0 && origA.compareTo(origB) == 0 && isTransformPrivileged) {
      /*
       * Operation A starts before operation B.
       *
       * -> No adjustment necessary.
       */

      return insA;

    } else {
      /*
       * Operation A starts in or behind operation B.
       *
       * -> Position of operation A must be shifted right by the length of the text of operation B.
       */

      TextPosition newStartPosition = shiftForInsertOperation(startA, insB);

      return new InsertOperation(
          newStartPosition,
          insA.getLineDelta(),
          insA.getOffsetDelta(),
          insA.getText(),
          insA.getOriginStartPosition());
    }
  }

  protected Operation transform(InsertOperation insA, DeleteOperation delB) {

    TextPosition startA = insA.getStartPosition();
    TextPosition startB = delB.getStartPosition();

    if (startA.compareTo(startB) <= 0) {
      /*
       * Operation A starts before or at the same position like operation B.
       *
       * -> No adjustment necessary.
       */

      return insA;

    } else if (startA.compareTo(delB.getEndPosition()) > 0) {
      /*
       * Operation A starts after operation B.
       *
       * -> Position of operation A must be shifted left by the length of the text of operation B.
       */

      TextPosition newStartPosition = shiftForDeleteOperation(startA, delB);

      return new InsertOperation(
          newStartPosition,
          insA.getLineDelta(),
          insA.getOffsetDelta(),
          insA.getText(),
          insA.getOriginStartPosition());

    } else {
      /*
       * Operation A starts in operation B.
       *
       * -> Position of operation A must be the start position of operation B.
       */

      return new InsertOperation(
          startB,
          insA.getLineDelta(),
          insA.getOffsetDelta(),
          insA.getText(),
          insA.getOriginStartPosition());
    }
  }

  private Operation transform(DeleteOperation delA, InsertOperation insB) {

    TextPosition startA = delA.getStartPosition();
    TextPosition endA = delA.getEndPosition();

    TextPosition startB = insB.getStartPosition();

    String text = delA.getText();

    if (startB.compareTo(endA) >= 0) {
      /*
       * Operation B is completely after operation A.
       *
       * ->  No adjustment necessary.
       */

      return delA;
    } else if (startB.compareTo(startA) <= 0) {
      /*
       * Operation B starts before or at the same position as operation A.
       *
       * -> Position of operation A must be shifted right by the length of the text of operation B.
       */

      TextPosition newStartPosition = shiftForInsertOperation(startA, insB);

      return new DeleteOperation(
          newStartPosition, delA.getLineDelta(), delA.getOffsetDelta(), text);

    } else {
      /*
       * Operation B (insert) is in the range of operation A (delete).
       *
       * -> Operation A must be split up into two delete operations.
       * Example: (A): "123456" -> (A'): "1" & "23456"
       */

      int inLineOffsetStartB = startB.getInLineOffset();

      int lineDeltaBeforeSplit = startB.getLineNumber() - startA.getLineNumber();
      int offsetDeltaBeforeSplit;

      int splitOffset;

      if (lineDeltaBeforeSplit == 0) {
        offsetDeltaBeforeSplit = inLineOffsetStartB - startA.getInLineOffset();

        splitOffset = offsetDeltaBeforeSplit;

      } else {
        offsetDeltaBeforeSplit = inLineOffsetStartB;

        // TODO remove guess once we have implemented content normalization
        String lineSeparator = TextPositionUtils.guessLineSeparator(text);

        int splitLineOffset = StringUtils.ordinalIndexOf(text, lineSeparator, lineDeltaBeforeSplit);

        if (splitLineOffset == -1) {
          throw new IllegalStateException(
              "Could not find line separator " + lineDeltaBeforeSplit + " in text");
        }

        splitOffset = splitLineOffset + lineSeparator.length() + offsetDeltaBeforeSplit;
      }

      String textBeforeSplit = text.substring(0, splitOffset);
      String textAfterSplit = text.substring(splitOffset);

      DeleteOperation del1 =
          new DeleteOperation(
              startA, lineDeltaBeforeSplit, offsetDeltaBeforeSplit, textBeforeSplit);

      int inLineOffsetEndA = endA.getInLineOffset();

      int lineDeltaAfterSplit = delA.getLineDelta() - lineDeltaBeforeSplit;
      int offsetDeltaAfterSplit;

      if (lineDeltaAfterSplit == 0) {
        offsetDeltaAfterSplit = inLineOffsetEndA - inLineOffsetStartB;

      } else {
        offsetDeltaAfterSplit = inLineOffsetEndA;
      }

      /*
       * position of insB has to be adjusted to match the state after the first halve of the
       * deletion was applied
       */
      InsertOperation adjustedInsB =
          new InsertOperation(startA, insB.getLineDelta(), insB.getOffsetDelta(), insB.getText());

      TextPosition newStartPosition = shiftForInsertOperation(startA, adjustedInsB);

      DeleteOperation del2 =
          new DeleteOperation(
              newStartPosition, lineDeltaAfterSplit, offsetDeltaAfterSplit, textAfterSplit);

      return new SplitOperation(del1, del2);
    }
  }

  private Operation transform(DeleteOperation delA, DeleteOperation delB) {
    TextPosition startA = delA.getStartPosition();
    TextPosition endA = delA.getEndPosition();

    TextPosition startB = delB.getStartPosition();
    TextPosition endB = delB.getEndPosition();

    if (startB.compareTo(endA) >= 0) {
      /*
       * Operation A is completely before operation B.
       *
       * -> No adjustment necessary.
       */

      return delA;

    } else if (startA.compareTo(endB) >= 0) {
      /*
       * Operation A starts at the end or after operation B.
       *
       * -> Position of operation A must be shifted left by the length of the text of operation B.
       */

      TextPosition newStartPosition = shiftForDeleteOperation(startA, delB);

      return new DeleteOperation(
          newStartPosition, delA.getLineDelta(), delA.getOffsetDelta(), delA.getText());

    } else {
      /*
       * Operation A and operation B are overlapping.
       */

      if (startB.compareTo(startA) <= 0 && endA.compareTo(endB) <= 0) {
        /*
         * Operation B starts before or at the same position as operation A and ends after or at the
         * same position as operation A.
         *
         * -> Operation A becomes a No-Op
         */

        return new NoOperation();

      } else if (startB.compareTo(startA) <= 0 && endA.compareTo(endB) > 0) {
        /*
         * Operation B starts before or at the same position as operation A and ends before
         * operation A.
         *
         * -> Leading overlap of operation A with operation B must be dropped.
         */

        return dropLeadingOverlap(delA, delB);

      } else if (startB.compareTo(startA) > 0 && endB.compareTo(endA) >= 0) {
        /*
         * Operation B starts after operation A and ends after or at the same position as operation
         * A.
         *
         * -> Trailing overlap of operation A with operation B must be dropped.
         */

        return dropTrailingOverlap(delA, delB);

      } else {
        /*
         * Operation B is fully in operation A.
         *
         * -> Overlap of operation A with operation B must be dropped.
         *
         * Can be done by viewing first part of operation A as having a trailing overhead with
         * operation B and the second part of operation A as having a leading overlap with operation
         *  B and then concatenating the two resulting delete operations.
         */

        DeleteOperation startDeletion = dropTrailingOverlap(delA, delB);
        DeleteOperation endDeletion = dropLeadingOverlap(delA, delB);

        TextPosition startingPosition = startDeletion.getStartPosition();

        int adjustedLineDelta = startDeletion.getLineDelta() + endDeletion.getLineDelta();
        int adjustedOffsetDelta;

        if (endDeletion.getLineDelta() > 0) {
          adjustedOffsetDelta = endDeletion.getOffsetDelta();

        } else {
          adjustedOffsetDelta = startDeletion.getOffsetDelta() + endDeletion.getOffsetDelta();
        }

        String adjustedText = startDeletion.getText() + endDeletion.getText();

        return new DeleteOperation(
            startingPosition, adjustedLineDelta, adjustedOffsetDelta, adjustedText);
      }
    }
  }

  /**
   * Drops the leading overlap of delete operation A with delete operation B.
   *
   * <p>The entire text section between the start of operation A and the end of operation B is
   * dropped, even if operation B starts after the start of operation A.
   *
   * <p><b>NOTE:</b> This method does not check whether such an overlap actually exists. This must
   * be determined by the caller beforehand.
   *
   * <p><b>NOTE:</b> This method does not check its input. It is expected that such checks are done
   * by the caller.
   *
   * @param delA the operation whose leading overlap to drop
   * @param delB the operation overlapping with the start of the first operation
   * @return a new deletion operation containing the content of delete operation A after its leading
   *     overlap with delete operation B was dropped
   */
  private DeleteOperation dropLeadingOverlap(DeleteOperation delA, DeleteOperation delB) {
    TextPosition startA = delA.getStartPosition();

    TextPosition startB = delB.getStartPosition();
    TextPosition endB = delB.getEndPosition();

    int inLineOffsetEndB = endB.getInLineOffset();

    String text = delA.getText();

    int lineDeltaA = delA.getLineDelta();
    int offsetDeltaA = delA.getOffsetDelta();

    int droppedLines = endB.getLineNumber() - startA.getLineNumber();

    String adjustedText;
    int adjustedLineDelta;
    int adjustedOffsetDelta;

    if (droppedLines == 0) {
      int droppedCharacters = inLineOffsetEndB - startA.getInLineOffset();

      adjustedText = text.substring(droppedCharacters);

      if (lineDeltaA == 0) {
        adjustedOffsetDelta = offsetDeltaA - droppedCharacters;
      } else {
        adjustedOffsetDelta = offsetDeltaA;
      }

      adjustedLineDelta = lineDeltaA;

    } else {
      // TODO remove guess once we have implemented content normalization
      String lineSeparator = TextPositionUtils.guessLineSeparator(text);

      int splitLineOffset = StringUtils.ordinalIndexOf(text, lineSeparator, droppedLines);

      if (splitLineOffset == -1) {
        throw new IllegalStateException(
            "Could not find line separator " + droppedLines + " in text");
      }

      int newStartOffset = splitLineOffset + lineSeparator.length() + inLineOffsetEndB;

      adjustedText = text.substring(newStartOffset);

      adjustedLineDelta = lineDeltaA - droppedLines;

      if (adjustedLineDelta == 0) {
        adjustedOffsetDelta = offsetDeltaA - inLineOffsetEndB;
      } else {
        adjustedOffsetDelta = offsetDeltaA;
      }
    }

    return new DeleteOperation(startB, adjustedLineDelta, adjustedOffsetDelta, adjustedText);
  }

  /**
   * Drops the trailing overlap of delete operation A with delete operation B.
   *
   * <p>The entire text section between the start of operation B and the end of operation A is
   * dropped, even if operation B ends before the end of operation A.
   *
   * <p><b>NOTE:</b> This method does not check whether such an overlap actually exists. This must
   * be determined by the caller beforehand.
   *
   * <p><b>NOTE:</b> This method does not check its input. It is expected that such checks are done
   * by the caller.
   *
   * @param delA the operation whose trailing overlap to drop
   * @param delB the operation overlapping with the end of the first operation
   * @return a new deletion operation containing the content of delete operation A after its
   *     trailing overlap with delete operation B was dropped
   */
  private DeleteOperation dropTrailingOverlap(DeleteOperation delA, DeleteOperation delB) {
    TextPosition startA = delA.getStartPosition();
    TextPosition endA = delA.getEndPosition();

    TextPosition startB = delB.getStartPosition();

    int inLineOffsetStartB = startB.getInLineOffset();

    String text = delA.getText();

    int lineDeltaA = delA.getLineDelta();

    int droppedLines = endA.getLineNumber() - startB.getLineNumber();

    String adjustedText;
    int adjustedLineDelta;
    int adjustedOffsetDelta;

    if (droppedLines == 0) {
      int droppedCharacters = endA.getInLineOffset() - inLineOffsetStartB;

      adjustedText = text.substring(0, text.length() - droppedCharacters);

      adjustedOffsetDelta = delA.getOffsetDelta() - droppedCharacters;

      adjustedLineDelta = lineDeltaA;

    } else {
      adjustedLineDelta = lineDeltaA - droppedLines;

      if (adjustedLineDelta == 0) {

        adjustedOffsetDelta = inLineOffsetStartB - startA.getInLineOffset();

        adjustedText = text.substring(0, adjustedOffsetDelta);

      } else {
        // TODO remove guess once we have implemented content normalization
        String lineSeparator = TextPositionUtils.guessLineSeparator(text);

        int splitLineOffset = StringUtils.ordinalIndexOf(text, lineSeparator, adjustedLineDelta);

        if (splitLineOffset == -1) {
          throw new IllegalStateException(
              "Could not find line separator " + adjustedLineDelta + " in text");
        }

        int newEndOffset = splitLineOffset + lineSeparator.length() + inLineOffsetStartB;

        adjustedText = text.substring(0, newEndOffset);

        adjustedOffsetDelta = inLineOffsetStartB;
      }
    }

    return new DeleteOperation(startA, adjustedLineDelta, adjustedOffsetDelta, adjustedText);
  }
}
