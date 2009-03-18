/*
 * $Id: Jupiter.java 2859 2006-04-01 09:39:19Z sim $
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
package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.InclusionTransformation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.GOTOInclusionTransformation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;

/**
 * This class implements the client-side core of the Jupiter control algorithm.
 */
public class Jupiter implements Algorithm {

    /**
     * The inclusion transformation function used to transform operations.
     */
    private InclusionTransformation inclusion;

    /**
     * The vector time, representing the number of processed requests, of this
     * algorithm.
     */
    private JupiterVectorTime vectorTime;

    /**
     * Flag indicating whether this algorithm is used on the client-side. In
     * some situations, the requests from the server-side have a higher priority
     * in transformations.
     */
    private final boolean isClientSide;

    /**
     * A list that contains the requests sent to the server which are to be
     * acknowledged by the server before they can be removed. This list
     * corresponds to the 'outgoing' list in the Jupiter pseudo code
     * description.
     */
    private final List<OperationWrapper> ackRequestList;

    /**
     * Class constructor that creates a new Jupiter algorithm.
     * 
     * @param isClientSide
     *            true if the algorithm resides on the client side
     */
    public Jupiter(boolean isClientSide) {
        this.inclusion = new GOTOInclusionTransformation();
        this.vectorTime = new JupiterVectorTime(0, 0);
        this.isClientSide = isClientSide;
        this.ackRequestList = new ArrayList<OperationWrapper>();
    }

    /**
     * @see de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm#generateRequest(de.fu_berlin.inf.dpp.concurrent.jupiter.Operation)
     */
    public Request generateRequest(Operation op) {
        // send(op, myMsgs, otherMsgs);
        Request req = new RequestImpl(getSiteId(), (Timestamp) this.vectorTime
            .clone(), op);

        // add(op, myMsgs) to outgoing;
        if (op instanceof SplitOperation) {
            SplitOperation split = (SplitOperation) op;
            this.ackRequestList.add(new OperationWrapper(split.getFirst(),
                this.vectorTime.getLocalOperationCount()));
            this.ackRequestList.add(new OperationWrapper(split.getSecond(),
                this.vectorTime.getLocalOperationCount()));
        } else {
            this.ackRequestList.add(new OperationWrapper(op, this.vectorTime
                .getLocalOperationCount()));
        }

        // myMsgs = myMsgs + 1;
        this.vectorTime.incrementLocalOperationCount();

        return req;
    }

    /**
     * @see de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm#receiveRequest(de.fu_berlin.inf.dpp.concurrent.jupiter.Request)
     */
    public Operation receiveRequest(Request req) throws TransformationException {
        Timestamp timestamp = req.getTimestamp();
        if (!(timestamp instanceof JupiterVectorTime)) {
            throw new IllegalArgumentException(
                "Jupiter expects timestamps of type JupiterVectorTime");
        }
        checkPreconditions((JupiterVectorTime) timestamp);
        discardAcknowledgedOperations((JupiterVectorTime) timestamp);

        Operation newOp = transform(req.getOperation());
        this.vectorTime.incrementRemoteRequestCount();

        return newOp;
    }

    /**
     * @see de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm#acknowledge(int,
     *      de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp)
     */
    public void acknowledge(int siteId, Timestamp timestamp)
        throws TransformationException {
        discardAcknowledgedOperations((JupiterVectorTime) timestamp);
    }

    /**
     * @see de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm#transformIndices(de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp,
     *      int[])
     */
    public int[] transformIndices(Timestamp timestamp, int[] indices)
        throws TransformationException {
        checkPreconditions((JupiterVectorTime) timestamp);
        discardAcknowledgedOperations((JupiterVectorTime) timestamp);
        int[] result = new int[indices.length];
        System.arraycopy(indices, 0, result, 0, indices.length);
        for (int i = 0; i < this.ackRequestList.size(); i++) {
            OperationWrapper wrap = this.ackRequestList.get(i);
            Operation ack = wrap.getOperation();
            for (int k = 0; k < indices.length; k++) {
                result[k] = transformIndex(result[k], ack);
            }
        }
        return result;
    }

    /**
     * Transforms the given index against the operation.
     * 
     * @param index
     *            the index to be transformed
     * @param op
     *            the operation to be transformed
     * @return the transformed index
     */
    private int transformIndex(int index, Operation op) {
        if (isClientSide()) {
            return this.inclusion.transformIndex(index, op, Boolean.TRUE);
        } else {
            return this.inclusion.transformIndex(index, op, Boolean.FALSE);
        }
    }

    /**
     * Discard from the other site (client/server) acknowledged operations.
     * 
     * @param time
     *            the request to the remote operation count from
     */
    private void discardAcknowledgedOperations(JupiterVectorTime time) {
        Iterator<OperationWrapper> iter = this.ackRequestList.iterator();
        while (iter.hasNext()) {
            OperationWrapper wrap = iter.next();
            if (wrap.getLocalOperationCount() < time.getRemoteOperationCount()) {
                iter.remove();
            }
        }
        // ASSERT msg.myMsgs == otherMsgs
        assert time.getLocalOperationCount() == this.vectorTime
            .getRemoteOperationCount() : "msg.myMsgs != otherMsgs !!";
    }

    /**
     * Transforms an operation with the operations in the outgoing queue
     * {@link #ackRequestList}.
     * 
     * @param newOp
     *            the operation to be transformed
     * @return the transformed operation
     * @see #ackRequestList
     */
    private Operation transform(Operation newOp) {
        for (int ackRequestListCnt = 0; ackRequestListCnt < this.ackRequestList
            .size(); ackRequestListCnt++) {
            OperationWrapper wrap = this.ackRequestList.get(ackRequestListCnt);
            Operation existingOp = wrap.getOperation();

            Operation transformedOp;
            if (newOp instanceof SplitOperation) {
                SplitOperation split = (SplitOperation) newOp;
                if (isClientSide()) {
                    split.setFirst(this.inclusion.transform(split.getFirst(),
                        existingOp, Boolean.TRUE));
                    split.setSecond(this.inclusion.transform(split.getSecond(),
                        existingOp, Boolean.TRUE));
                    existingOp = this.inclusion.transform(existingOp, split
                        .getFirst(), Boolean.FALSE);
                    existingOp = this.inclusion.transform(existingOp, split
                        .getSecond(), Boolean.FALSE);
                } else {
                    split.setFirst(this.inclusion.transform(split.getFirst(),
                        existingOp, Boolean.FALSE));
                    split.setSecond(this.inclusion.transform(split.getSecond(),
                        existingOp, Boolean.FALSE));
                    existingOp = this.inclusion.transform(existingOp, split
                        .getFirst(), Boolean.TRUE);
                    existingOp = this.inclusion.transform(existingOp, split
                        .getSecond(), Boolean.TRUE);
                }
                transformedOp = split;
            } else {
                if (isClientSide()) {
                    transformedOp = this.inclusion.transform(newOp, existingOp,
                        Boolean.TRUE);
                    existingOp = this.inclusion.transform(existingOp, newOp,
                        Boolean.FALSE);
                } else {
                    transformedOp = this.inclusion.transform(newOp, existingOp,
                        Boolean.FALSE);
                    existingOp = this.inclusion.transform(existingOp, newOp,
                        Boolean.TRUE);
                }
            }
            this.ackRequestList.set(ackRequestListCnt, new OperationWrapper(
                existingOp, wrap.getLocalOperationCount()));

            newOp = transformedOp;
        }
        return newOp;
    }

    /**
     * Test 3 preconditions that must be fulfilled before transforming. They are
     * taken from the Jupiter paper.
     * 
     * @param time
     *            the request to be tested.
     */
    private void checkPreconditions(JupiterVectorTime time)
        throws TransformationException {
        if (!this.ackRequestList.isEmpty()
            && (time.getRemoteOperationCount() < this.ackRequestList.get(0)
                .getLocalOperationCount())) {
            throw new TransformationException("Precondition #1 violated.");
        } else if (time.getRemoteOperationCount() > this.vectorTime
            .getLocalOperationCount()) {
            throw new TransformationException(
                "precondition #2 violated (Remote vector time is greater than local vector time).");
        } else if (time.getLocalOperationCount() != this.vectorTime
            .getRemoteOperationCount()) {
            throw new TransformationException(
                "Precondition #3 violated (Vector time does not match): "
                    + time + " , " + this.vectorTime);
        }
    }

    /**
     * This is a simple helper class used in the implementation of the Jupiter
     * algorithm. A OperationWrapper instance is created with an operation and
     * the current local operation count and inserted into the outgoing queue
     * (see {@link Jupiter#ackRequestList}).
     * 
     * @see Jupiter#generateRequest(Operation)
     * @see Jupiter#receiveRequest(Request)
     */
    private static class OperationWrapper {

        private final Operation op;

        private final int count;

        OperationWrapper(Operation op, int count) {
            this.op = op;
            this.count = count;
        }

        Operation getOperation() {
            return this.op;
        }

        int getLocalOperationCount() {
            return this.count;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return ("OperationWrapper(" + this.op + ", " + this.count + ")");
        }
    }

    /**
     * Throws a CannotUndoException because undo is not supported by this
     * implementation.
     * 
     * @see de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm#undo()
     */
    public Request undo() {
        throw new CannotUndoException();
    }

    /**
     * Throws a CannotRedoException because undo is not supported by this
     * implementation.
     * 
     * @see de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm#redo()
     */
    public Request redo() {
        throw new CannotRedoException();
    }

    /**
     * Set an inclusion transformation function.
     * 
     * @param it
     *            the inclusion transformation function to set.
     */
    public void setInclusionTransformation(InclusionTransformation it) {
        this.inclusion = it;
    }

    /**
     * @return the algorithms inclusion transformation
     */
    public InclusionTransformation getInclusionTransformation() {
        return this.inclusion;
    }

    /**
     * @see de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm#getSiteId()
     */
    public int getSiteId() {
        return isClientSide() ? 1 : 0;
    }

    /**
     * @see de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm#getTimestamp()
     */
    public synchronized Timestamp getTimestamp() {
        return (Timestamp) this.vectorTime.clone();
    }

    /**
     * Checks if this algorithm locates client side.
     * 
     * @return true if this algorithm locates client side
     */
    public boolean isClientSide() {
        return this.isClientSide;
    }

    public void updateVectorTime(Timestamp timestamp)
        throws TransformationException {
        if (this.ackRequestList.size() > 0) {
            throw new TransformationException(
                "ackRequestList have entries. Update Vector time failed.");
        }
        int local = timestamp.getComponents()[0];
        int remote = timestamp.getComponents()[1];
        this.vectorTime = new JupiterVectorTime(local, remote);

    }

}
