package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.eclipse.core.resources.ResourcesPlugin;

/**
 * All classes found in the net.internal.extensions package are misplaced. These
 * packet extensions are used by Saros, not by the network layer itself which
 * only offers methods to send these extensions. Do not put any class of this
 * package into the net.* package.
 */
public class README {

    /*
     * the only purpose this class serves it to create compile errors when moved
     * into the core ... delete the class when the issues are solved
     */

    static {
        System.err.println(ResourcesPlugin.getWorkspace() == null);
    }
}