/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
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

package de.fu_berlin.inf.dpp.core.zip;

/**
 * Interface for reporting progress of Zip operations.
 *
 * @author Stefan Rossbach
 */
public interface ZipListener {

    /**
     * Gets called when a new Zip entry is created.
     *
     * @param filename the name of the file including its path that will be
     *                 compressed now
     * @return <code>true</true> if the Zip progress should be aborted, <code>false</code>
     * otherwise
     */
    public boolean update(String filename);

    /**
     * Gets called when a chunk of data has been deflated. This should be called
     * in a moderate amount (e.g after every x bytes).
     *
     * @param totalRead the amount of bytes that has already been read
     * @param totalSize the total size in bytes that will be read when the operation
     *                  has finished or <code>-1</code> if the size is unknown
     * @return <code>true</true> if the Zip progress should be aborted, <code>false</code>
     * otherwise
     */
    public boolean update(long totalRead, long totalSize);
}
