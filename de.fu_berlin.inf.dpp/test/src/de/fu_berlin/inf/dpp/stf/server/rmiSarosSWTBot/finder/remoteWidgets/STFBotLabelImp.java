package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

public class STFBotLabelImp extends EclipseComponentImp implements STFBotLabel {

    private static transient STFBotLabelImp labelImp;

    private SWTBotLabel swtBotLabel;

    /**
     * {@link STFBotTableImp} is a singleton, but inheritance is possible.
     */
    public static STFBotLabelImp getInstance() {
        if (labelImp != null)
            return labelImp;
        labelImp = new STFBotLabelImp();
        return labelImp;
    }

    public void setSwtBotLabel(SWTBotLabel label) {
        this.swtBotLabel = label;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public String getText() throws RemoteException {
        return bot.label().getText();
    }

    public String getTextOfLabelInGroup(String inGroup) throws RemoteException {
        return bot.labelInGroup(inGroup).getText();
    }

    public boolean existsLabel(String mnemonicText) throws RemoteException {
        try {
            bot.label(mnemonicText);

            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public boolean existsLabelInView(String viewTitle) throws RemoteException {
        try {
            bot().view(viewTitle).bot_().label();
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }
}
