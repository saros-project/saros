package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets.SarosSWTBotChatInput;

public class ChatView extends Component implements IChatView {

    private static transient ChatView self;
    private IRemoteBotView view;

    /**
     * {@link ChatView} is a singleton, but inheritance is possible.
     */
    public static ChatView getInstance() {
        if (self != null)
            return self;
        self = new ChatView();
        return self;
    }

    public IChatView setView(IRemoteBotView view) {
        this.view = view;
        return this;
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

    public void sendChatMessage(String message) throws RemoteException {
        precondition();
        SarosSWTBotChatInput chatInput = bot.chatInput();
        chatInput.setText(message);
        bot.text();
        log.debug("inerted message in chat view: " + chatInput.getText());
        // chatInput.pressShortcut(Keystrokes.LF);
        chatInput.pressEnterKey();
    }

    public boolean compareChatMessage(String jid, String message)
        throws RemoteException {
        precondition();
        log.debug("chatLine: " + bot.lastChatLine());
        log.debug("text of the lastChatLine: " + bot.lastChatLine().getText());
        String text = bot.lastChatLine().getText();
        return text.equals(message);
    }

    /**********************************************
     * 
     * States
     * 
     **********************************************/
    public boolean isChatViewOpen() throws RemoteException {
        return bot().isViewOpen(VIEW_SAROS_CHAT);
    }

    public String getUserNameOnChatLinePartnerChangeSeparator()
        throws RemoteException {
        precondition();
        log.debug("user name of the first chat line partner change separator: "
            + bot.chatLinePartnerChangeSeparator().getPlainID());
        return bot.chatLinePartnerChangeSeparator().getPlainID();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator(int index)
        throws RemoteException {
        precondition();
        log.debug("user name of the chat line partner change separator with the index"
            + index
            + ": "
            + bot.chatLinePartnerChangeSeparator(index).getPlainID());
        return bot.chatLinePartnerChangeSeparator(index).getPlainID();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator(String plainID)
        throws RemoteException {
        precondition();
        log.debug("user name of the chat line partner change separator with the plainID "
            + plainID
            + ": "
            + bot.chatLinePartnerChangeSeparator(plainID).getPlainID());
        return bot.chatLinePartnerChangeSeparator(plainID).getPlainID();
    }

    public String getTextOfChatLine() throws RemoteException {
        precondition();
        log.debug("text of the first chat line: " + bot.chatLine().getText());
        return bot.chatLine().getText();
    }

    public String getTextOfChatLine(int index) throws RemoteException {
        precondition();
        log.debug("text of the chat line with the index " + index + ": "
            + bot.chatLine(index).getText());
        return bot.chatLine(index).getText();
    }

    public String getTextOfLastChatLine() throws RemoteException {
        precondition();
        log.debug("text of the last chat line: " + bot.lastChatLine().getText());
        return bot.lastChatLine().getText();
    }

    public String getTextOfChatLine(String regex) throws RemoteException {
        precondition();
        log.debug("text of the chat line with the specifed regex: "
            + bot.chatLine(regex).getText());
        return bot.chatLine(regex).getText();
    }

    /**********************************************
     * 
     * Waits until
     * 
     **********************************************/
    public void waitUntilGetChatMessage(String jid, String message)
        throws RemoteException {
        bot().waitUntil(SarosConditions.isChatMessageExist(this, jid, message));
    }

    /**********************************************
     * 
     * inner functions
     * 
     **********************************************/
    private void precondition() throws RemoteException {
        bot().activateWorkbench();
        bot().openViewById(VIEW_SAROS_CHAT_ID);
        bot().view(VIEW_SAROS_CHAT).show();

    }
}
