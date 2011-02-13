package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

public class STFBotComboImp extends EclipseComponentImp implements STFBotCombo {

    private static transient STFBotComboImp self;

    private SWTBotCombo swtBotCombo;

    /**
     * {@link STFBotButtonImp} is a singleton, but inheritance is possible.
     */
    public static STFBotComboImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotComboImp();
        return self;
    }

    public void setSwtBotCombo(SWTBotCombo ccomb) {
        this.swtBotCombo = ccomb;
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

}
