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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * The SplitOperation contains two operations. It is used when an operation
 * needs to be split up under certain transformation conditions.
 * 
 * @see Operation
 */
public class SplitOperation implements Operation {

    private static final Logger log = Logger.getLogger(SplitOperation.class
        .getName());

    /**
     * The first operation.
     */
    private Operation op1;

    /**
     * The second operation.
     */
    private Operation op2;

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

    // TODO review for nested SplitOperations
    public List<TextEditActivity> toTextEdit(IPath path, String source) {

        try {
            List<TextEditActivity> first = getFirst().toTextEdit(path, source);
            List<TextEditActivity> second = getSecond()
                .toTextEdit(path, source);

            List<TextEditActivity> result = new ArrayList<TextEditActivity>(
                first.size() + second.size());
            result.addAll(first);
            result.addAll(second);

            // FIXME is this really necessary?
            if (result.size() <= 1)
                return result;

            if (result.size() == 2) {
                // TODO Somehow delete operations need to be shifted, don't know
                // why

                TextEditActivity op1 = result.get(0);
                TextEditActivity op2 = result.get(1);

                if ((op1.replacedText.length() > 0) && (op1.text.length() == 0)
                    && (op2.replacedText.length() > 0)
                    && (op2.text.length() == 0)) {

                    log.warn("Split operation shifts second delete operation:"
                        + this);
                    Saros.getDefault().getLog().log(
                        new Status(IStatus.WARNING, Saros.SAROS, IStatus.OK,
                            "Split operation shifts second delete operation:"
                                + this, new StackTrace()));

                    result.set(1,
                        new TextEditActivity(source, op2.offset
                            - op1.replacedText.length(), "", op2.replacedText,
                            path));
                }
                return result;
            }

            if (result.size() > 2) {
                log.warn("SplitOperation larger than expected: " + this,
                    new StackTrace());
                Saros.getDefault().getLog().log(
                    new Status(IStatus.WARNING, Saros.SAROS, IStatus.OK,
                        "SplitOperation larger than expected: " + this,
                        new StackTrace()));
            }
            return result;
        } catch (RuntimeException e) {
            log.error("Internal error in SplitOperation: " + this, e);
            throw e;
        }
    }
}
