package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;

public class STFBotToolbarDropDownButtonImp extends AbstractRmoteWidget
    implements STFBotToolbarDropDownButton {
    private static transient STFBotToolbarDropDownButtonImp self;

    private SWTBotToolbarDropDownButton toolbarDropDownButton;

    /**
     * {@link STFBotButtonImp} is a singleton, but inheritance is possible.
     */
    public static STFBotToolbarDropDownButtonImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotToolbarDropDownButtonImp();
        return self;
    }

    public void setSwtBotToolbarDropDownButton(
        SWTBotToolbarDropDownButton toolbarDropDownButton) {
        this.toolbarDropDownButton = toolbarDropDownButton;
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
