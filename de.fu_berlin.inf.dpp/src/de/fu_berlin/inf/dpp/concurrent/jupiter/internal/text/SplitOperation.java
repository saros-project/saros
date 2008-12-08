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

import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;

/**
 * The SplitOperation contains two operations. It is used when an operation
 * needs to be split up under certain transformation conditions.
 * 
 * @see ch.iserver.ace.algorithm.Operation
 */
public class SplitOperation implements Operation {

    /**
     * The first operation.
     */
    private Operation op1;

    /**
     * The second operation.
     */
    private Operation op2;

    /**
     * Class constructor.
     */
    public SplitOperation() {
    }

    /**
     * Class constructor.
     * 
     * @param op1
     *            the first operation
     * @param op2
     *            the second operation
     */
    public SplitOperation(Operation op1, Operation op2) {
        this.op1 = op1;
        this.op2 = op2;
    }

    /**
     * Returns the first operation.
     * 
     * @return the first operation
     */
    public Operation getFirst() {
        return this.op1;
    }

    /**
     * Sets the first operation.
     * 
     * @param op1
     *            the first operation
     */
    public void setFirst(Operation op1) {
        this.op1 = op1;
    }

    /**
     * Returns the second operation.
     * 
     * @return the second operation
     */
    public Operation getSecond() {
        return this.op2;
    }

    /**
     * Sets the second operation.
     * 
     * @param op2
     *            the second operation
     */
    public void setSecond(Operation op2) {
        this.op2 = op2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Split(" + this.op1 + ", " + this.op2 + ")";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj.getClass().equals(getClass())) {
            SplitOperation op = (SplitOperation) obj;
            return op.getFirst().equals(this.op1)
                    && op.getSecond().equals(this.op2);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hashcode = this.op1.hashCode();
        hashcode += 17 * this.op2.hashCode();
        return hashcode;
    }

}
