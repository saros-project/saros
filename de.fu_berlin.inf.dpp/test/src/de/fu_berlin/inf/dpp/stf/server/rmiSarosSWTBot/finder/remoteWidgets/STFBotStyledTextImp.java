package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

public class STFBotStyledTextImp extends EclipseComponentImp implements
    STFBotStyledText {

    private static transient STFBotStyledTextImp self;

    private SWTBotStyledText swtBotStyledText;

    /**
     * {@link STFBotTableImp} is a singleton, but inheritance is possible.
     */
    public static STFBotStyledTextImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotStyledTextImp();
        return self;
    }

    public void setSwtBotStyledText(SWTBotStyledText styledText) {
        this.swtBotStyledText = styledText;
    }

    public String getText() throws RemoteException {
        return swtBotStyledText.getText();
    }

}
