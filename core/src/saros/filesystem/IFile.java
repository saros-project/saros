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
import java.io.UnsupportedEncodingException;
import java.nio.charset.IllegalCharsetNameException;

/**
 * This interface is under development. It currently equals its Eclipse counterpart. If not
 * mentioned otherwise all offered methods are equivalent to their Eclipse counterpart.
 */
public interface IFile extends IResource {
  public String getCharset() throws IOException;

  /**
   * Sets the given character set for this file.
   *
   * <p>Does nothing if the passed character set is <code>null</code>.
   *
   * @param charset the character set to set
   * @throws IOException if the character set could not be set
   * @throws IllegalCharsetNameException if the given character set name is not valid
   * @throws UnsupportedEncodingException if the given character set is not supported by the local
   *     JVM
   */
  void setCharset(String charset) throws IOException;

  public InputStream getContents() throws IOException;

  /**
   * Writes the content of the given input stream into the file. Any existing file content is
   * overwritten.
   *
   * @param input the input stream to write into the file
   * @throws IOException if the content could not be written to the file
   */
  public void setContents(InputStream input) throws IOException;

  /**
   * Creates the this file with the given content.
   *
   * @throws IOException if the file already exists, could not be created, or the content of the
   *     newly created file could not be set
   */
  public void create(InputStream input) throws IOException;

  /**
   * Returns the size of the file.
   *
   * @return the size of the file in bytes
   * @throws IOException if an I/O error occurred
   */
  public long getSize() throws IOException;

  default Type getType() {
    return Type.FILE;
  }
}
