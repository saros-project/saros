package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.EclipseComponentImp;

public class STFTextImp extends EclipseComponentImp implements STFText {

    private static transient STFTextImp textImp;

    /**
     * {@link STFTableImp} is a singleton, but inheritance is possible.
     */
    public static STFTextImp getInstance() {
        if (textImp != null)
            return textImp;
        textImp = new STFTextImp();
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
