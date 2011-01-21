package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;

public class TextImp extends EclipsePart implements Text {

    private static transient TextImp textImp;

    /**
     * {@link TableImp} is a singleton, but inheritance is possible.
     */
    public static TextImp getInstance() {
        if (textImp != null)
            return textImp;
        textImp = new TextImp();
        return textImp;
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

    public void setTextInTextWithLabel(String text, String label)
        throws RemoteException {
        bot.textWithLabel(label).setText(text);
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public String getTextInTextWithLabel(String label) throws RemoteException {
        return bot.textWithLabel(label).getText();
    }

}
