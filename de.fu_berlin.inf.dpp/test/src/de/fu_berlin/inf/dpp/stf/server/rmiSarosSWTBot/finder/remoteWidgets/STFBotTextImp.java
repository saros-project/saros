package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

public class STFBotTextImp extends AbstractRmoteWidget implements STFBotText {

    private static transient STFBotTextImp textImp;

    private SWTBotText swtBotText;

    /**
     * {@link STFBotTableImp} is a singleton, but inheritance is possible.
     */
    public static STFBotTextImp getInstance() {
        if (textImp != null)
            return textImp;
        textImp = new STFBotTextImp();
        return textImp;
    }

    public void setSwtBotText(SWTBotText text) {
        this.swtBotText = text;
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

    public void setText(String text) throws RemoteException {
        swtBotText.setText(text);
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public String getText() throws RemoteException {
        return swtBotText.getText();
    }

}
