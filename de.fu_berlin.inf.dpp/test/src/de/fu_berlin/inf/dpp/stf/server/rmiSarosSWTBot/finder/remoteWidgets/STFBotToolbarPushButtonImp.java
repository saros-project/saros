package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarPushButton;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

public class STFBotToolbarPushButtonImp extends EclipseComponentImp implements
    STFBotToolbarPushButton {

    private static transient STFBotToolbarPushButtonImp self;

    private SWTBotToolbarPushButton toolbarPushButton;

    /**
     * {@link STFBotButtonImp} is a singleton, but inheritance is possible.
     */
    public static STFBotToolbarPushButtonImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotToolbarPushButtonImp();
        return self;
    }

    public void setSwtBotToolbarPushButton(
        SWTBotToolbarPushButton toolbarPushButton) {
        this.toolbarPushButton = toolbarPushButton;
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
