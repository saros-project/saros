/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package saros.filesystem;

import java.io.IOException;
import java.io.InputStream;

/**
 * This interface is under development. It currently equals its Eclipse counterpart. If not
 * mentioned otherwise all offered methods are equivalent to their Eclipse counterpart.
 */
public interface IFile extends IResource {
  public String getCharset() throws IOException;

  public InputStream getContents() throws IOException;

  /**
   * Equivalent to the Eclipse call <code>IFile#setContents(input, force, keepHistory, null)</code>
   */
  public void setContents(InputStream input, boolean force, boolean keepHistory) throws IOException;

  /** Equivalent to the Eclipse call <code>IFile#create(input, force, null)</code> */
  public void create(InputStream input, boolean force) throws IOException;

  /**
   * Returns the size of the file.
   *
   * @return the size of the file in bytes
   * @throws IOException if an I/O error occurred
   */
  public long getSize() throws IOException;
}
