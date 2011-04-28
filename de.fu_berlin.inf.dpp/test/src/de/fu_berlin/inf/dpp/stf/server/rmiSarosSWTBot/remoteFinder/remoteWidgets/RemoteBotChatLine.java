package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets.SarosSWTBotChatLine;

public class RemoteBotChatLine extends AbstractRmoteWidget implements
    IRemoteBotChatLine {

    private static transient RemoteBotChatLine self;

    private SarosSWTBotChatLine widget;

    /**
     * {@link RemoteBotChatLine} is a singleton, but inheritance is possible.
     */
    public static RemoteBotChatLine getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotChatLine();
        return self;
    }

    public IRemoteBotChatLine setWidget(SarosSWTBotChatLine ccomb) {
        this.widget = ccomb;
        return this;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * finders
     * 
     **********************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public String getText() throws RemoteException {
        return widget.getText();
    }

}
