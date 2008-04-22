/*
 * $Id: VectorTime.java 982 2005-11-07 11:23:11Z sim $
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

/**
 * Vector time is a concept used in almost all OT algorithms to determine
 * causality relations of operations.
 */
public interface VectorTime extends Timestamp {
	
	/**
	 * Gets the length of the vector.
	 * 
	 * @return the length of the vector time
	 */
	int getLength();
	
	/**
	 * Gets the value at the given index.
	 * 
	 * @param index the index into the vector
	 * @return the value at the given index
	 * @throws IndexOutOfBoundsException if index is invalid
	 */
	int getAt(int index);
	
}
