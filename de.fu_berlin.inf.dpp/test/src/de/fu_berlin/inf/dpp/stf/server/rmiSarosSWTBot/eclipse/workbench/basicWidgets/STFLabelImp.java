package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

public class STFLabelImp extends EclipseComponentImp implements STFLabel {

    private static transient STFLabelImp labelImp;

    private SWTBotLabel swtBotLabel;

    /**
     * {@link STFTableImp} is a singleton, but inheritance is possible.
     */
    public static STFLabelImp getInstance() {
        if (labelImp != null)
            return labelImp;
        labelImp = new STFLabelImp();
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
