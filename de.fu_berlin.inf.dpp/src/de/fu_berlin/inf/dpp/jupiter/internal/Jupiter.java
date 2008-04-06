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
package de.fu_berlin.inf.dpp.jupiter.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.jupiter.*;
import de.fu_berlin.inf.dpp.jupiter.internal.text.*;

/**
 * This class implements the client-side core of the Jupiter control algorithm.
 */
public class Jupiter implements Algorithm {
	
	private static Logger logger = Logger.getLogger(Jupiter.class);
	
	/**
	 * The inclusion transformation function used to transform operations.
	 */
	private InclusionTransformation inclusion;

	/**
	 * The vector time, representing the number of processed requests, of
	 * this algorithm.
	 */
	private JupiterVectorTime vectorTime;

	/**
	 * Flag indicating whether this algorithm is used on the client-side.
	 * In some situations, the requests from the server-side have a higher
	 * priority in transformations.
	 */
	private boolean isClientSide;

	/**
	 * A list that contains the requests sent to the server which are to be
	 * acknowledged by the server before they can be removed. This list
	 * corresponds to the 'outgoing' list in the Jupiter pseudo code
	 * description.
	 */
	private List<OperationWrapper> ackRequestList;

	/**
	 * Class constructor that creates a new Jupiter algorithm.
	 * @param isClientSide
	 *            true if the algorithm resides on the client side
	 */
	public Jupiter(boolean isClientSide) {
		this.inclusion = new GOTOInclusionTransformation();
		this.vectorTime = new JupiterVectorTime(0, 0);
		this.isClientSide = isClientSide;
		ackRequestList = new ArrayList<OperationWrapper>();
	}
	
	/**
	 * @see  de.fu_berlin.inf.dpp.jupiter.Algorithm#generateRequest( de.fu_berlin.inf.dpp.jupiter.Operation)
	 */
	public Request generateRequest(Operation op) {
		// send(op, myMsgs, otherMsgs);
		Request req = new RequestImpl(
						getSiteId(), 
						(Timestamp) vectorTime.clone(), 
						op);

		// add(op, myMsgs) to outgoing;
		if (op instanceof SplitOperation) {
			SplitOperation split = (SplitOperation) op;
			ackRequestList.add(new OperationWrapper(split.getFirst(),
							vectorTime.getLocalOperationCount()));
			ackRequestList.add(new OperationWrapper(split.getSecond(),
							vectorTime.getLocalOperationCount()));
		} else {
			ackRequestList.add(new OperationWrapper(op, vectorTime
							.getLocalOperationCount()));
		}

		// myMsgs = myMsgs + 1;
		vectorTime.incrementLocalOperationCount();

		return req;
	}
	
	/**
	 * @see de.fu_berlin.inf.dpp.jupiter.Algorithm#receiveRequest(de.fu_berlin.inf.dpp.jupiter.Request)
	 */
	public Operation receiveRequest(Request req) throws TransformationException {
		Timestamp timestamp = req.getTimestamp();
		if (!(timestamp instanceof JupiterVectorTime)) {
			throw new IllegalArgumentException("Jupiter expects timestamps of type JupiterVectorTime");
		}
		//TODO: check preconditions
		checkPreconditions((JupiterVectorTime) timestamp);
		discardAcknowledgedOperations((JupiterVectorTime) timestamp);

		Operation newOp = transform(req.getOperation());
		vectorTime.incrementRemoteRequestCount();
		
		return newOp;
	}
	
	public Operation receiveTransformedRequest(Request req) throws TransformationException{
		Timestamp timestamp = req.getTimestamp();
		if (!(timestamp instanceof JupiterVectorTime)) {
			throw new IllegalArgumentException("Jupiter expects timestamps of type JupiterVectorTime");
		}
		//TODO: check preconditions
		checkPreconditions((JupiterVectorTime) timestamp);
		discardAcknowledgedOperations((JupiterVectorTime) timestamp);
		vectorTime.incrementRemoteRequestCount();
		return req.getOperation();
	}
	
	/**
	 * @see de.fu_berlin.inf.dpp.jupiter.Algorithm#acknowledge(int, de.fu_berlin.inf.dpp.jupiter.Timestamp)
	 */
	public void acknowledge(int siteId, Timestamp timestamp) throws TransformationException {
		discardAcknowledgedOperations((JupiterVectorTime) timestamp);
	}
	
	/**
	 * @see de.fu_berlin.inf.dpp.jupiter.Algorithm#transformIndices(de.fu_berlin.inf.dpp.jupiter.Timestamp, int[])
	 */
	public int[] transformIndices(Timestamp timestamp, int[] indices) throws TransformationException {
		checkPreconditions((JupiterVectorTime) timestamp);
		discardAcknowledgedOperations((JupiterVectorTime) timestamp);
		int[] result = new int[indices.length]; 
		System.arraycopy(indices, 0, result, 0, indices.length);
		for (int i = 0; i < ackRequestList.size(); i++) {
			OperationWrapper wrap = (OperationWrapper) ackRequestList.get(i);
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
	 * @param index the index to be transformed
	 * @param op the operation to be transformed
	 * @return the transformed index
	 */
	private int transformIndex(int index, Operation op) {
		if (isClientSide()) {
			return inclusion.transformIndex(index, op, Boolean.TRUE);
		} else {
			return inclusion.transformIndex(index, op, Boolean.FALSE);
		}
	}

	/**
	 * Discard from the other site (client/server) acknowledged operations.
	 * 
	 * @param time the request to the remote operation count from
	 */
	private void discardAcknowledgedOperations(JupiterVectorTime time) {
		Iterator iter = ackRequestList.iterator();
		while (iter.hasNext()) {
			OperationWrapper wrap = (OperationWrapper) iter.next();
			if (wrap.getLocalOperationCount() < time.getRemoteOperationCount()) {
				iter.remove();
			}
		}
		// ASSERT msg.myMsgs == otherMsgs
		assert time.getLocalOperationCount() == vectorTime
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
		for (int ackRequestListCnt = 0; ackRequestListCnt < ackRequestList
						.size(); ackRequestListCnt++) {
			OperationWrapper wrap = (OperationWrapper) ackRequestList
							.get(ackRequestListCnt);
			Operation existingOp = wrap.getOperation();

			Operation transformedOp;
			if (newOp instanceof SplitOperation) {
				SplitOperation split = (SplitOperation) newOp;
				if (isClientSide()) {
					split.setFirst(inclusion.transform(split.getFirst(),
									existingOp, Boolean.TRUE));
					split.setSecond(inclusion.transform(split.getSecond(),
									existingOp, Boolean.TRUE));
					existingOp = inclusion.transform(existingOp, split
									.getFirst(), Boolean.FALSE);
					existingOp = inclusion.transform(existingOp, split
									.getSecond(), Boolean.FALSE);
				} else {
					split.setFirst(inclusion.transform(split.getFirst(),
									existingOp, Boolean.FALSE));
					split.setSecond(inclusion.transform(split.getSecond(),
									existingOp, Boolean.FALSE));
					existingOp = inclusion.transform(existingOp, split
									.getFirst(), Boolean.TRUE);
					existingOp = inclusion.transform(existingOp, split
									.getSecond(), Boolean.TRUE);
				}
				transformedOp = split;
			} else {
				if (isClientSide()) {
					transformedOp = inclusion.transform(newOp, existingOp,
									Boolean.TRUE);
					existingOp = inclusion.transform(existingOp, newOp,
									Boolean.FALSE);
				} else {
					transformedOp = inclusion.transform(newOp, existingOp,
									Boolean.FALSE);
					existingOp = inclusion.transform(existingOp, newOp,
									Boolean.TRUE);
				}
			}
			ackRequestList.set(ackRequestListCnt, new OperationWrapper(
							existingOp, wrap.getLocalOperationCount()));

			newOp = transformedOp;
		}
		return newOp;
	}

	/**
	 * Test 3 preconditions that must be fulfilled before transforming. They are
	 * taken from the Jupiter paper.
	 * 
	 * @param time the request to be tested.
	 */
	private void checkPreconditions(JupiterVectorTime time) throws TransformationException {
		if (!ackRequestList.isEmpty()
						&& time.getRemoteOperationCount() < ((OperationWrapper) ackRequestList
							   .get(0)).getLocalOperationCount()) {
			throw new TransformationException("precondition #1 violated.");
		} else if (time.getRemoteOperationCount() > vectorTime
						.getLocalOperationCount()) {
			throw new TransformationException("precondition #2 violated.");
		} else if (time.getLocalOperationCount() != vectorTime
						.getRemoteOperationCount()) {
			throw new TransformationException("precondition #3 violated: " + time + " , " + vectorTime);
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

		private Operation op;

		private int count;

		OperationWrapper(Operation op, int count) {
			this.op = op;
			this.count = count;
		}

		Operation getOperation() {
			return op;
		}

		int getLocalOperationCount() {
			return count;
		}

		/**
		 * {@inheritDoc}
		 */
		public String toString() {
			return ("OperationWrapper(" + op + ", " + count + ")");
		}
	}

	/**
	 * Throws a CannotUndoException because undo is not supported by this
	 * implementation.
	 * 
	 * @see de.fu_berlin.inf.dpp.jupiter.Algorithm#undo()
	 */
	public Request undo() {
		throw new CannotUndoException();
	}

	/**
	 * Throws a CannotRedoException because undo is not supported by this
	 * implementation.
	 * 
	 * @see de.fu_berlin.inf.dpp.jupiter.Algorithm#redo()
	 */
	public Request redo() {
		throw new CannotRedoException();
	}

	/**
	 * Set an inclusion transformation function.
	 * 
	 * @param it the inclusion transformation function to set.
	 */
	public void setInclusionTransformation(InclusionTransformation it) {
		this.inclusion = it;
	}

	/**
	 * @return the algorithms inclusion transformation
	 */
	public InclusionTransformation getInclusionTransformation() {
		return inclusion;
	}

	/**
	 * @see  de.fu_berlin.inf.dpp.jupiter.Algorithm#getSiteId()
	 */
	public int getSiteId() {
		return isClientSide() ? 1 : 0;
	}
	
	/**
	 * @see de.fu_berlin.inf.dpp.jupiter.Algorithm#getTimestamp()
	 */
	public synchronized Timestamp getTimestamp() {
		return (Timestamp) vectorTime.clone();
	}
	
	/**
	 * Checks if this algorithm locates client side.
	 * 
	 * @return true if this algorithm locates client side
	 */
	public boolean isClientSide() {
		return isClientSide;
	}
	
}
