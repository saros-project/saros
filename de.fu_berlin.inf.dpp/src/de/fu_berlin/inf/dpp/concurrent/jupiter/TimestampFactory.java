/*
 * $Id: TimestampFactory.java 749 2005-10-21 13:51:56Z sim $
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
 * TimestampFactory is a factory that can create Timestamp objects from an
 * external representation of timestamps in the form of an int array.
 */
public interface TimestampFactory {

    /**
     * Creates a Timestamp from the components in the int array.
     * 
     * @param components
     *            the components of the timestamp as an int array
     * @return a Timestamp instance created from the component array
     * @throws IllegalArgumentException
     *             if the component array does not satisfy the expectations of
     *             the TimestampFactory
     */
    Timestamp createTimestamp(int[] components);

}
