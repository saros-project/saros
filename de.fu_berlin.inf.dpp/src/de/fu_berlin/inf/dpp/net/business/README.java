package de.fu_berlin.inf.dpp.net.business;

import org.eclipse.core.resources.ResourcesPlugin;

/**
 * All classes found in the net.business package are misplaced. They belong
 * either to the session negotiation management (SessionManager/Invitation) or
 * to a Saros session. These classes must not be put into the net.* package !
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