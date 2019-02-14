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
package de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.session.User;
import java.util.ArrayList;
import java.util.List;

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
        result.add(
            new TextEditActivity(
                source, lastOp.getPosition(), operation.getText(), lastOp.getText(), path));
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
  protected ITextOperation combine(ITextOperation op1, ITextOperation op2) {

    if (op1 instanceof InsertOperation && op2 instanceof DeleteOperation) {
      InsertOperation insert = (InsertOperation) op1;
      DeleteOperation delete = (DeleteOperation) op2;

      if (insert.getPosition() == delete.getPosition()) {
        // Ins(5,"ab") + Del(5,"abcd") -> Del(5,"cd")
        if (delete.getText().startsWith(insert.getText())) {
          return new DeleteOperation(
              insert.getPosition(), delete.getText().substring(insert.getTextLength()));
        }
        // Ins(5,"abcd") + Del(5,"ab") -> Ins(5,"cd")
        else if (insert.getText().startsWith(delete.getText())) {
          return new InsertOperation(
              insert.getPosition(), insert.getText().substring(delete.getTextLength()));
        }
      }

    } else if (op1 instanceof InsertOperation && op2 instanceof InsertOperation) {
      InsertOperation insert1 = (InsertOperation) op1;
      InsertOperation insert2 = (InsertOperation) op2;

      // Ins(2,"ab") + Ins(4,"cd") -> Ins(2,"abcd")
      if (insert1.getPosition() + insert1.getTextLength() == insert2.getPosition()) {
        return new InsertOperation(insert1.getPosition(), insert1.getText() + insert2.getText());
      } else if (insert1.getPosition() == insert2.getPosition() + insert2.getTextLength()) {
        return new InsertOperation(insert2.getPosition(), insert2.getText() + insert1.getText());
      }

    } else if (op1 instanceof DeleteOperation && op2 instanceof DeleteOperation) {
      DeleteOperation delete1 = (DeleteOperation) op1;
      DeleteOperation delete2 = (DeleteOperation) op2;

      // Del(5,"ab") + Del(5,"cde") -> Del(5,"abcde")
      if (delete1.getPosition() == delete2.getPosition()) {
        return new DeleteOperation(delete1.getPosition(), delete1.getText() + delete2.getText());
      }
      // Del(8,"c") + Del(6,"ab") -> Del(6,"abc")
      if (delete1.getPosition() == delete2.getPosition() + delete2.getTextLength()) {
        return new DeleteOperation(delete2.getPosition(), delete2.getText() + delete1.getText());
      }
    }
    // Nothing can be combined
    return null;
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
      if (delete.getPosition() == insert.getPosition()) return true;
    }
    return false;
  }

  @Override
  public Operation invert() {
    Operation inverseOperation = new SplitOperation(getSecond().invert(), getFirst().invert());
    return inverseOperation;
  }
}
