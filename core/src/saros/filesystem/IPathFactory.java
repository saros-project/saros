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

/**
 * An interface for implementing a factory that is able to convert {@link saros.filesystem.IPath
 * path} objects to their string representation and vice versa. Implementations must throw the given
 * {@link RuntimeException runtime exceptions} as declared in the method signatures.
 */
public interface IPathFactory {

  /**
   * Converts a path to its string representation
   *
   * @param path the path to convert
   * @return the string representation of the path
   * @throws NullPointerException if path is <code>null</code>
   * @throws IllegalArgumentException if the path is not relative (e.g it presents a full path like
   *     <code>/etc/init.d/</code>)
   */
  public String fromPath(IPath path);

  /**
   * Converts a string to a path object
   *
   * @param name the name of the path to convert
   * @return a path object representing the path of the given name
   * @throws NullPointerException if name is <code>null</code>
   * @throws IllegalArgumentException if the resulting path object is not a relative path
   */
  public IPath fromString(String name);
}
