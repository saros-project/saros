/*
 * $Id: Timestamp.java 747 2005-10-21 13:31:38Z sim $
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
 * This interface represents the concept of a timestamp. Timestamps is a general concept, so this
 * interface remains empty. Timestamps are immutable.
 *
 * @see VectorTime
 */
public interface Timestamp {

  /**
   * Retrieves the components of the timestamp as an int array. The exact representation is up to
   * the concrete Timestamp implementation.
   *
   * @return the components of the Timestamp implementation
   */
  int[] getComponents();
}
