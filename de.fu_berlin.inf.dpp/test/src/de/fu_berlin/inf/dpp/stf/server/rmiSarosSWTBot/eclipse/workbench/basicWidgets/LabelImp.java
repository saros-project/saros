package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

public class LabelImp extends EclipseComponentImp implements Label {

    private static transient LabelImp labelImp;

    /**
     * {@link TableImp} is a singleton, but inheritance is possible.
     */
    public static LabelImp getInstance() {
        if (labelImp != null)
            return labelImp;
        labelImp = new LabelImp();
        return labelImp;
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
    public String getTextOfLabel() throws RemoteException {
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
            view(viewTitle).bot().label();
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }
}
