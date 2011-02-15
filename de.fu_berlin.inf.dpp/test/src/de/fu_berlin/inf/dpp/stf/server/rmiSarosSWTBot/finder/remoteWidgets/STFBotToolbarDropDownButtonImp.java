package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

public class STFBotToolbarDropDownButtonImp extends EclipseComponentImp
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

    public void setSwtBotToolbarDropDownButton(SWTBotToolbarDropDownButton toolbarDropDownButton) {
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
