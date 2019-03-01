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
package de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text;

import de.fu_berlin.inf.dpp.concurrent.jupiter.InclusionTransformation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import java.security.InvalidParameterException;
import org.apache.log4j.Logger;

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

    /** A NoOperation is not affected. */
    if (op1 instanceof NoOperation) {
      return new NoOperation();
    }

    /** If the context is null, we return op1 unchanged. */
    if (op2 instanceof NoOperation) {
      return op1;
    }

    if (op1 instanceof SplitOperation) {
      /**
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
      /**
       * Given an operation op1 to be transformed in the context of two operations s1 and s2, we
       * need to calculate op1' as t(op1', s2) where op1' is t(op1, s1) <code>
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
  public int transformIndex(int index, Operation op, Object param) {
    if (op instanceof SplitOperation) {
      SplitOperation s = (SplitOperation) op;
      index = transformIndex(index, s.getSecond(), param);
      index = transformIndex(index, s.getFirst(), param);
      return index;
    } else if (op instanceof NoOperation) {
      return index;
    } else if (op instanceof InsertOperation) {
      int pos = ((InsertOperation) op).getPosition();
      if (index < pos) {
        return index;
      } else {
        return index + ((InsertOperation) op).getTextLength();
      }
    } else if (op instanceof DeleteOperation) {
      int pos = ((DeleteOperation) op).getPosition();
      if (index <= pos) {
        return index;
      } else {
        return index - ((DeleteOperation) op).getTextLength();
      }
    } else {
      throw new IllegalArgumentException("Unsupported Operation type: " + op);
    }
  }

  protected Operation transform(
      InsertOperation insA, InsertOperation insB, boolean isTransformPrivileged) {

    int posA = insA.getPosition();
    int posB = insB.getPosition();
    int lenB = insB.getTextLength();
    if ((posA < posB)
        || ((posA == posB) && (insA.getOrigin() < insB.getOrigin()))
        || ((posA == posB) && (insA.getOrigin() == insB.getOrigin()) && isTransformPrivileged)) {
      /*
       * Operation A starts before operation B.
       */
      return insA;
    } else {
      /*
       * Operation A starts in or behind operation B. Index of operation
       * A' must be increased by the length of the text of operation B.
       */
      return new InsertOperation(posA + lenB, insA.getText(), insA.getOrigin());
    }
  }

  protected Operation transform(InsertOperation insA, DeleteOperation delB) {

    int posA = insA.getPosition();
    int posB = delB.getPosition();
    int lenB = delB.getTextLength();

    if (posA <= posB) {
      /*
       * Operation A starts before or at the same position like operation
       */
      return insA;
    } else if (posA > (posB + lenB)) {
      /*
       * Operation A starts after operation B. Index of operation A' must
       * be reduced by the length of the text of operation B.
       */
      return new InsertOperation(posA - lenB, insA.getText(), insA.getOrigin());
    } else {
      /*
       * Operation A starts in operation B. Index of A' must be the index
       * of operation B.
       */
      return new InsertOperation(posB, insA.getText(), insA.getOrigin());
    }
  }

  protected Operation transform(DeleteOperation delA, InsertOperation insB) {

    int posA = delA.getPosition();
    int lenA = delA.getTextLength();
    int posB = insB.getPosition();
    int lenB = insB.getTextLength();

    if (posB >= (posA + lenA)) {
      /*
       * Operation B is completely after operation A.
       */
      return delA;
    } else if (posB <= posA) {
      /*
       * Operation B starts before or at the same position like operation
       * A
       */
      return new DeleteOperation(posA + lenB, delA.getText());
    } else {
      /*
       * Operation B (insert) is in the range of operation A (delete).
       * Operation A' must be split up into two delete operations. (A):
       * "123456" (A'): "1" "23456"
       */
      DeleteOperation del1 = new DeleteOperation(posA, delA.getText().substring(0, posB - posA));
      DeleteOperation del2 =
          new DeleteOperation(posA + lenB, delA.getText().substring(posB - posA, lenA));
      return new SplitOperation(del1, del2);
    }
  }

  protected Operation transform(DeleteOperation delA, DeleteOperation delB) {

    int posA = delA.getPosition();
    int lenA = delA.getTextLength();
    int posB = delB.getPosition();
    int lenB = delB.getTextLength();

    if (posB >= (posA + lenA)) {
      /*
       * Operation A is completely before operation B.
       */
      return delA;
    } else if (posA >= (posB + lenB)) {
      /*
       * Operation A starts at the end or after operation B. Index of
       * operation A' must be reduced by the length of the text of
       * operation B.
       */
      return new DeleteOperation(posA - lenB, delA.getText());
    } else {
      /*
       * Operation A and operation B are overlapping.
       */
      if ((posB <= posA) && ((posA + lenA) <= (posB + lenB))) {
        /*
         * Operation B starts before or at the same position like
         * operation A and ends after or at the same position like
         * operation A.
         */
        return new NoOperation();
      } else if ((posB <= posA) && ((posA + lenA) > (posB + lenB))) {
        /*
         * Operation B starts before or at the same position like
         * operation A and ends before operation A.
         */
        return new DeleteOperation(posB, delA.getText().substring(posB + lenB - posA, lenA));
      } else if ((posB > posA) && ((posB + lenB) >= (posA + lenA))) {
        /*
         * Operation B starts after operation A and ends after or at the
         * same position like operation A.
         */
        return new DeleteOperation(posA, delA.getText().substring(0, posB - posA));
      } else {
        /*
         * Operation B is fully in operation A.
         */
        return new DeleteOperation(
            posA,
            delA.getText().substring(0, posB - posA)
                + delA.getText().substring(posB + lenB - posA, lenA));
      }
    }
  }
}
