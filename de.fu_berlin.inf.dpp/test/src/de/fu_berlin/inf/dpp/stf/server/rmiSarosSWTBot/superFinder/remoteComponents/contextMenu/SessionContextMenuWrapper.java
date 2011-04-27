package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu;

public class SessionContextMenuWrapper extends SarosContextMenuWrapper {
    private static transient SessionContextMenuWrapper self;

    /**
     * {@link SessionContextMenuWrapper} is a singleton, but inheritance is
     * possible.
     */
    public static SessionContextMenuWrapper getInstance() {
        if (self != null)
            return self;
        self = new SessionContextMenuWrapper();

        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * contextMenus showed within session area.
     * 
     **********************************************/

}