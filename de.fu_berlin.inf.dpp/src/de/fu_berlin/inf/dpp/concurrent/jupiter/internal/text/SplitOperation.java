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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;

/**
 * The SplitOperation contains two operations to be performed after each other.
 * It is used when an operation needs to be split up under certain
 * transformation conditions.
 * 
 * @see Operation
 */
@XStreamAlias("splitOp")
public class SplitOperation implements Operation {

    /**
     * The first operation.
     */
    protected Operation op1;

    /**
     * The second operation.
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Split(" + this.op1 + ", " + this.op2 + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SplitOperation other = (SplitOperation) obj;
        if (op1 == null) {
            if (other.op1 != null)
                return false;
        } else if (!op1.equals(other.op1))
            return false;
        if (op2 == null) {
            if (other.op2 != null)
                return false;
        } else if (!op2.equals(other.op2))
            return false;
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

    public List<TextEditActivity> toTextEdit(IPath path, String source) {

        List<TextEditActivity> result = new ArrayList<TextEditActivity>();

        result.addAll(getFirst().toTextEdit(path, source));
        result.addAll(getSecond().toTextEdit(path, source));

        return result;
    }
}
