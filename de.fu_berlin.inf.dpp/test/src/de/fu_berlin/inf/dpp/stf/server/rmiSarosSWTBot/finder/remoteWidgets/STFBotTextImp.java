package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

public class STFBotTextImp extends EclipseComponentImp implements STFBotText {

    private static transient STFBotTextImp textImp;

    /**
     * {@link STFBotTableImp} is a singleton, but inheritance is possible.
     */
    public static STFBotTextImp getInstance() {
        if (textImp != null)
            return textImp;
        textImp = new STFBotTextImp();
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
