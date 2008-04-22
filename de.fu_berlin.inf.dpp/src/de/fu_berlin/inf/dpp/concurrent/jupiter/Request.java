/*
 * $Id: Request.java 2430 2005-12-11 15:17:11Z sim $
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

package de.fu_berlin.inf.dpp.concurrent.jupiter;

import java.io.Serializable;

import de.fu_berlin.inf.dpp.net.JID;


/**
 * This interface represents a request. Requests are typically sent over the
 * network to other sites. A request consists at least of the identifier of the
 * sending site, an operation and a timestamp that specifies the definition
 * context of the operation.
 * 
 * @see ch.iserver.ace.algorithm.Operation
 * @see ch.iserver.ace.algorithm.Timestamp
 */
public interface Request extends Serializable {

	/**
	 * Gets the identifier of the sending site.
	 * 
	 * @return the identifier of the sending site
	 */
	public int getSiteId();

	/**
	 * Gets the operation to be propagated.
	 * 
	 * @return the operation
	 */
	public Operation getOperation();

	/**
	 * Gets the timestamp that specifies the definition context of the enclosed
	 * operation.
	 * 
	 * @return the timestamp of the definition context
	 */
	public Timestamp getTimestamp();
	
	/**
	 * Gets the jid of the appropriate client site.
	 * @return the jid of the client
	 */
	public JID getJID();

}
