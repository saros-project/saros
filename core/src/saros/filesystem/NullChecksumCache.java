/*
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

package de.fu_berlin.inf.dpp.filesystem;

/**
 * A checksum cache that always makes callers recalculate checksums.
 *
 * <p>Useful if your implementation has difficulties using the {@link FileSystemChecksumCache}.
 */
public class NullChecksumCache implements IChecksumCache {
  @Override
  public Long getChecksum(IFile file) {
    return null;
  }

  @Override
  public boolean addChecksum(IFile file, long checksum) {
    return false;
  }
}
