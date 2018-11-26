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
 */

package de.fu_berlin.inf.dpp.context;

import org.picocontainer.MutablePicoContainer;

/**
 * Interface for implementing context factories depending on the current platform Saros is running
 * on.
 */
public interface IContextFactory {

  /**
   * Creates the runtime components for the Saros application. It is up to the implementor to ensure
   * to create all necessary components that are needed during runtime on the given platform.
   *
   * @param container the container to insert the components to
   */
  public void createComponents(MutablePicoContainer container);
}
