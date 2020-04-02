/*
 * $Id: SplitOperation.java 2434 2005-12-12 07:49:51Z sim $
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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.concurrent.jupiter.Operation;
import saros.editor.text.TextPosition;
import saros.session.User;

/**
 * The SplitOperation contains two operations to be performed after each other. It is used when an
 * operation needs to be split up under certain transformation conditions.
 *
 * @see Operation
 */
@XStreamAlias("splitOp")
public class SplitOperation implements Operation {

  /** The first operation. */
  protected Operation op1;

  /** The second operation. */
  protected Operation op2;

  public SplitOperation(Operation op1, Operation op2) {
    this.op1 = op1;
    this.op2 = op2;
  }

  public Operation getFirst() {
    return this.op1;
  }

  public Operation getSecond() {
    return this.op2;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "Split(" + this.op1 + ", " + this.op2 + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    SplitOperation other = (SplitOperation) obj;
    if (op1 == null) {
      if (other.op1 != null) return false;
    } else if (!op1.equals(other.op1)) return false;
    if (op2 == null) {
      if (other.op2 != null) return false;
    } else if (!op2.equals(other.op2)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((op1 == null) ? 0 : op1.hashCode());
    result = prime * result + ((op2 == null) ? 0 : op2.hashCode());
    return result;
  }

  @Override
  public List<ITextOperation> getTextOperations() {
    List<ITextOperation> result = new ArrayList<ITextOperation>();

    result.addAll(getFirst().getTextOperations());
    result.addAll(getSecond().getTextOperations());

    return result;
  }

  @Override
  public List<TextEditActivity> toTextEdit(SPath path, User source) {

    List<TextEditActivity> result = new ArrayList<TextEditActivity>();

    /*
     * remember the last operation for trying to combine it with the next
     * one
     */
    ITextOperation lastOp = null;

    List<ITextOperation> compressed = new ArrayList<ITextOperation>();

    for (ITextOperation operation : this.getTextOperations()) {

      // the first operation in the list
      if (lastOp == null) {
        lastOp = operation;
        continue;
      }

      ITextOperation combined = combine(lastOp, operation);
      if (combined != null) {
        lastOp = combined;
        continue;
      } else {
        compressed.add(lastOp);
        lastOp = operation;
      }
    }
    if (lastOp != null) compressed.add(lastOp);

    lastOp = null;

    for (ITextOperation operation : compressed) {

      // the first operation in the list
      if (lastOp == null) {
        lastOp = operation;
        continue;
      }

      if (isReplace(lastOp, operation)) {
        TextPosition startPosition = lastOp.getStartPosition();

        int newTextLineDelta = operation.getLineDelta();
        int newTextOffsetDelta = operation.getOffsetDelta();

        String text = operation.getText();

        int replacedTextLineDelta = lastOp.getLineDelta();
        int replacedTextOffsetDelta = lastOp.getOffsetDelta();

        String replacedText = lastOp.getText();

        TextEditActivity textReplaceActivity =
            new TextEditActivity(
                source,
                startPosition,
                newTextLineDelta,
                newTextOffsetDelta,
                text,
                replacedTextLineDelta,
                replacedTextOffsetDelta,
                replacedText,
                path);

        result.add(textReplaceActivity);
        lastOp = null;
        continue;
      } else {
        // Cannot combine two operations to a replace
        result.addAll(lastOp.toTextEdit(path, source));
        lastOp = operation;
      }
    }
    if (lastOp != null) result.addAll(lastOp.toTextEdit(path, source));

    return result;
  }

  /**
   * @return a combined ITextOperation representing both op1 and op2 or null if the two operations
   *     cannot be combined
   */
  // TODO finish optimization; handle other cases that can be combined
  protected ITextOperation combine(ITextOperation op1, ITextOperation op2) {

    if (op1 instanceof InsertOperation && op2 instanceof DeleteOperation) {
      InsertOperation insert = (InsertOperation) op1;
      DeleteOperation delete = (DeleteOperation) op2;

      if (insert.getStartPosition().compareTo(delete.getStartPosition()) == 0) {
        // Case 1: Ins(5,"ab") + Del(5,"abcd") -> Del(5,"cd")
        if (delete.getText().startsWith(insert.getText())) {

          String adjustedText = delete.getText().substring(insert.getText().length());

          int newLineDelta = delete.getLineDelta() - insert.getLineDelta();
          int newOffsetDelta;

          if (newLineDelta == 0) {
            newOffsetDelta = delete.getOffsetDelta() - insert.getOffsetDelta();

          } else {
            newOffsetDelta = delete.getOffsetDelta();
          }

          return new DeleteOperation(
              insert.getStartPosition(), newLineDelta, newOffsetDelta, adjustedText);
        }
        // Case 2: Ins(5,"abcd") + Del(5,"ab") -> Ins(5,"cd")
        else if (insert.getText().startsWith(delete.getText())) {

          String adjustedText = insert.getText().substring(delete.getText().length());

          int newLineDelta = insert.getLineDelta() - delete.getLineDelta();
          int newOffsetDelta;

          if (newLineDelta == 0) {
            newOffsetDelta = insert.getOffsetDelta() - delete.getOffsetDelta();

          } else {
            newOffsetDelta = insert.getOffsetDelta();
          }

          return new InsertOperation(
              insert.getStartPosition(), newLineDelta, newOffsetDelta, adjustedText);
        }
      }

    } else if (op1 instanceof InsertOperation && op2 instanceof InsertOperation) {
      InsertOperation insert1 = (InsertOperation) op1;
      InsertOperation insert2 = (InsertOperation) op2;

      // Case 1: Ins(2,"ab") + Ins(4,"cd") -> Ins(2,"abcd")
      if (insert1.getEndPosition().compareTo(insert2.getStartPosition()) == 0) {
        return concatenateInsertOperations(insert1, insert2);
      }

      // Case 2: Ins(4,"cd") + Ins(4,"ab") -> Ins(4,"abcd")
      if (insert1.getStartPosition().compareTo(insert2.getStartPosition()) == 0) {
        return concatenateInsertOperations(insert2, insert1);
      }

    } else if (op1 instanceof DeleteOperation && op2 instanceof DeleteOperation) {
      DeleteOperation delete1 = (DeleteOperation) op1;
      DeleteOperation delete2 = (DeleteOperation) op2;

      // Case 1: Del(5,"ab") + Del(5,"cde") -> Del(5,"abcde")
      if (delete1.getStartPosition().compareTo(delete2.getStartPosition()) == 0) {
        return concatenateDeleteOperations(delete1, delete2);
      }

      // Case 2: Del(8,"c") + Del(6,"ab") -> Del(6,"abc")
      if (delete1.getStartPosition().compareTo(delete2.getEndPosition()) == 0) {
        return concatenateDeleteOperations(delete2, delete1);
      }
    }
    // Nothing can be combined
    return null;
  }

  /**
   * Concatenates the given insert operations. To do so, the second operation is appended to the
   * first.
   *
   * <p><b>NOTE:</b> This method does not check whether the given operations can actually be
   * concatenated (i.e. are located seamlessly next to each other). It is expected that this has
   * already be done by the caller.
   *
   * @param op1 the first operation
   * @param op2 the second operation
   * @return an insert operation representing the concatenation of the two given insert operations
   */
  private InsertOperation concatenateInsertOperations(InsertOperation op1, InsertOperation op2) {
    int newLineDelta;
    int newOffsetDelta;

    if (op2.getLineDelta() == 0) {
      newLineDelta = op1.getLineDelta();
      newOffsetDelta = op1.getOffsetDelta() + op2.getOffsetDelta();

    } else {
      newLineDelta = op1.getLineDelta() + op2.getLineDelta();
      newOffsetDelta = op2.getOffsetDelta();
    }

    String newText = op1.getText() + op2.getText();

    TextPosition startPosition = op1.getStartPosition();

    return new InsertOperation(startPosition, newLineDelta, newOffsetDelta, newText);
  }

  /**
   * Concatenates the given delete operations. To do so, the second operation is appended to the
   * first.
   *
   * <p><b>NOTE:</b> This method does not check whether the given operations can actually be
   * concatenated (i.e. are located seamlessly next to each other). It is expected that this has
   * already be done by the caller.
   *
   * @param op1 the first operation
   * @param op2 the second operation
   * @return a delete operation representing the concatenation of the two given delete operations
   */
  private DeleteOperation concatenateDeleteOperations(DeleteOperation op1, DeleteOperation op2) {
    int newLineDelta;
    int newOffsetDelta;

    if (op2.getLineDelta() == 0) {
      newLineDelta = op1.getLineDelta();
      newOffsetDelta = op1.getOffsetDelta() + op2.getOffsetDelta();

    } else {
      newLineDelta = op1.getLineDelta() + op2.getLineDelta();
      newOffsetDelta = op2.getOffsetDelta();
    }

    String newText = op1.getText() + op2.getText();

    TextPosition startPosition = op1.getStartPosition();

    return new DeleteOperation(startPosition, newLineDelta, newOffsetDelta, newText);
  }

  /**
   * @param op1
   * @param op2
   * @return true if the combination of op1 and op2 describe a text replace
   */
  protected boolean isReplace(Operation op1, Operation op2) {
    if (op1 == null) return false;
    if (op2 == null) return false;

    if (op1 instanceof DeleteOperation && op2 instanceof InsertOperation) {
      DeleteOperation delete = (DeleteOperation) op1;
      InsertOperation insert = (InsertOperation) op2;

      // Del(8,"abc") + Ins(8,"ghijk") -> Replace "abc" with
      // "ghijk"
      if (Objects.equals(delete.getStartPosition(), insert.getStartPosition())) return true;
    }
    return false;
  }

  @Override
  public Operation invert() {
    Operation inverseOperation = new SplitOperation(getSecond().invert(), getFirst().invert());
    return inverseOperation;
  }
}
