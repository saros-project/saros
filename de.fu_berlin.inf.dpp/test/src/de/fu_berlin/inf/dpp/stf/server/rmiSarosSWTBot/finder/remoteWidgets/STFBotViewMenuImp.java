package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotViewMenu;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

public class STFBotViewMenuImp extends EclipseComponentImp implements
    STFBotViewMenu {

    private static transient STFBotViewMenuImp self;

    private SWTBotViewMenu viewMenu;

    /**
     * {@link STFBotButtonImp} is a singleton, but inheritance is possible.
     */
    public static STFBotViewMenuImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotViewMenuImp();
        return self;
    }

    public void setSwtBotViewMenu(SWTBotViewMenu viewMenu) {
        this.viewMenu = viewMenu;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
}
