/*
 * $Id: InclusionTransformation.java 2430 2005-12-11 15:17:11Z sim $
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

/** Interface for inclusion transformation functions. */
public interface InclusionTransformation {

  /**
   * Include operation <var>op2</var> into the context of operation <var>op1</var>. The transformed
   * operation <var>op1'</var> is returned.
   *
   * @param op1 the operation into which another is to be contextually included.
   * @param op2 the operation to be included.
   * @param param an additional parameter depending on the implementation.
   * @return the transformed operation <var>op1'</var>
   */
  public Operation transform(Operation op1, Operation op2, Object param);

  /**
   * Transforms an index against the given operation.
   *
   * @param index the index to be transformed
   * @param op the Operation to be transformed
   * @param param an additional implementation dependent parameter
   * @return the transformed index
   */
  public int transformIndex(int index, Operation op, Object param);
}
