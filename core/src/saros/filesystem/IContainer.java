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

/**
 * This interface is under development. It currently equals its Eclipse counterpart. If not
 * mentioned otherwise all offered methods are equivalent to their Eclipse counterpart.
 */
public interface IContainer extends IResource {

  public boolean exists(IPath path);

  public IResource[] members() throws IOException;

  public String getDefaultCharset() throws IOException;

  /**
   * Returns a handle for the file with the given relative path to this resource.
   *
   * @param pathString a string representation of the path relative to this resource
   * @return a handle for the file with the given relative path to this resource
   * @throws NullPointerException if the given string is <code>null</code>
   * @throws IllegalArgumentException if the given string represents an absolute path
   */
  public IFile getFile(String pathString);

  public IFile getFile(IPath path);

  /**
   * Returns a handle for the folder with the given relative path to this resource.
   *
   * @param pathString a string representation of the path relative to this resource
   * @return a handle for the folder with the given relative path to this resource
   * @throws NullPointerException if the given string is <code>null</code>
   * @throws IllegalArgumentException if the given string represents an absolute path
   */
  public IFolder getFolder(String pathString);

  public IFolder getFolder(IPath path);
}
