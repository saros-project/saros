/*
 * $Id$
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

import de.fu_berlin.inf.dpp.jupiter.Operation;
import de.fu_berlin.inf.dpp.jupiter.Request;
import de.fu_berlin.inf.dpp.jupiter.Timestamp;


/**
 * Default implementation of the Request interface.
 */
public class RequestImpl implements Request {
	
	/**
	 * The site id of the request.
	 */
	private final int siteId;
	
	/**
	 * The timestamp of the request.
	 */
	private final Timestamp timestamp;
	
	/**
	 * The operation of the request.
	 */
	private final Operation operation;
	
	/**
	 * Creates a new instance of the RequestImpl class.
	 * 
	 * @param siteId the site id
	 * @param timestamp the timestamp
	 * @param operation the operation
	 */
	public RequestImpl(int siteId, Timestamp timestamp, Operation operation) {
		this.siteId = siteId;
		this.timestamp = timestamp;
		this.operation = operation;
	}
	
	/**
	 * @see ch.iserver.ace.algorithm.Request#getSiteId()
	 */
	public int getSiteId() {
		return siteId;
	}

	/**
	 * @see ch.iserver.ace.algorithm.Request#getOperation()
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * @see ch.iserver.ace.algorithm.Request#getTimestamp()
	 */
	public Timestamp getTimestamp() {
		return timestamp;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Request) {
			Request request = (Request) obj;
			return siteId == request.getSiteId()
			       && nullSafeEquals(timestamp, request.getTimestamp())
			       && nullSafeEquals(operation, request.getOperation());
		} else {
			return false;
		}
	}
	
	private boolean nullSafeEquals(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		} else if (o1 == null || o2 == null) {
			return false;
		} else {
			return o1.equals(o2);
		}
	}
		
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int hashCode = 13 * siteId;
		hashCode += (timestamp != null) ? 17 * timestamp.hashCode() : 0;
		hashCode += (operation != null) ? 29 * operation.hashCode() : 0;
		return hashCode;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("request(");
		buffer.append(siteId);
		buffer.append(",");
		buffer.append(timestamp);
		buffer.append(",");
		buffer.append(operation);
		buffer.append(")");
		return buffer.toString();
	}

}
