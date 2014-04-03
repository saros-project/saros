/*
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
 */

package de.fu_berlin.inf.dpp;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

public interface ISarosContext {

    /**
     * Injects dependencies into the annotated fields of the given object. This
     * method should be used for objects that were created by Eclipse, which
     * have a different life cycle than the Saros plug-in.
     * 
     * @deprecated using annotated field injection inside the business logic is
     *             a bad design choice
     */
    @Deprecated
    void initComponent(Object object);

    /**
     * This should only be used by SarosSession code. Make sure to release the
     * child container to prevent a memory leak
     * 
     * @return Create a new child container
     */
    MutablePicoContainer createSimpleChildContainer();

    /**
     * Remove the given child from this contexts container.
     * 
     * @param picoContainer
     * @return
     */
    boolean removeChildContainer(PicoContainer picoContainer);

    /**
     * Retrieve a component keyed by the component type.
     * 
     * @param componentType
     *            the type of the component
     * @return the typed resulting object instance or <code>null</code> if the
     *         object does not exist.
     */
    <T> T getComponent(Class<T> componentType);
}
