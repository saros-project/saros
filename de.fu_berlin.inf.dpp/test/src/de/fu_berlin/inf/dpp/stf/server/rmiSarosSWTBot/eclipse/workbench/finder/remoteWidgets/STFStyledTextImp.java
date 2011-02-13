package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.EclipseComponentImp;

public class STFStyledTextImp extends EclipseComponentImp implements
    STFStyledText {

    private static transient STFStyledTextImp self;

    private SWTBotStyledText swtBotStyledText;

    /**
     * {@link STFTableImp} is a singleton, but inheritance is possible.
     */
    public static STFStyledTextImp getInstance() {
        if (self != null)
            return self;
        self = new STFStyledTextImp();
        return self;
    }

    public void setSwtBotStyledText(SWTBotStyledText styledText) {
        this.swtBotStyledText = styledText;
    }

    public String getText() throws RemoteException {
        return swtBotStyledText.getText();
    }

}
