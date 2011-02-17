package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarToggleButton;

public class STFBotToolbarToggleButtonImp extends AbstractRmoteWidget implements
    STFBotToolbarToggleButton {

    private static transient STFBotToolbarToggleButtonImp self;

    private SWTBotToolbarToggleButton toolbarToggleButton;

    /**
     * {@link STFBotButtonImp} is a singleton, but inheritance is possible.
     */
    public static STFBotToolbarToggleButtonImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotToolbarToggleButtonImp();
        return self;
    }

    public void setSwtBotToolbarToggleButton(
        SWTBotToolbarToggleButton toolbarToggleButton) {
        this.toolbarToggleButton = toolbarToggleButton;
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
