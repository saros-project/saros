package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotCTabItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets.SarosSWTBotChatInput;

public class Chatroom extends Component implements IChatroom {

    private static transient Chatroom self;

    /**
     * {@link Chatroom} is a singleton, but inheritance is possible.
     */
    public static Chatroom getInstance() {
        if (self != null)
            return self;
        self = new Chatroom();

        return self;
    }

    protected IRemoteBotCTabItem cTabItem;

    public void setCTabItem(IRemoteBotCTabItem cTabItem) {
        this.cTabItem = cTabItem;
    }

    public void sendChatMessage(String message) throws RemoteException {

        remoteBot().activateWorkbench();
        SarosSWTBotChatInput chatInput = bot().chatInput();
        chatInput.setText(message);
        log.debug("inerted message in chat view: " + chatInput.getText());
        // chatInput.pressShortcut(Keystrokes.LF);
        chatInput.pressEnterKey();
    }

    public boolean compareChatMessage(String jid, String message)
        throws RemoteException {
        log.debug("chatLine: " + remoteBot().lastChatLine());
        log.debug("text of the lastChatLine: " + bot().lastChatLine().getText());
        String text = bot().lastChatLine().getText();
        return text.equals(message);
    }

    public String getUserNameOnChatLinePartnerChangeSeparator()
        throws RemoteException {
        log.debug("user name of the first chat line partner change separator: "
            + bot().chatLinePartnerChangeSeparator().getPlainID());
        return bot().chatLinePartnerChangeSeparator().getPlainID();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator(int index)
        throws RemoteException {
        log.debug("user name of the chat line partner change separator with the index"
            + index
            + ": "
            + bot().chatLinePartnerChangeSeparator(index).getPlainID());
        return bot().chatLinePartnerChangeSeparator(index).getPlainID();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator(String plainID)
        throws RemoteException {
        log.debug("user name of the chat line partner change separator with the plainID "
            + plainID
            + ": "
            + bot().chatLinePartnerChangeSeparator(plainID).getPlainID());
        return bot().chatLinePartnerChangeSeparator(plainID).getPlainID();
    }

    public String getTextOfChatLine() throws RemoteException {
        log.debug("text of the first chat line: " + bot().chatLine().getText());
        return bot().chatLine().getText();
    }

    public String getTextOfChatLine(int index) throws RemoteException {
        log.debug("text of the chat line with the index " + index + ": "
            + bot().chatLine(index).getText());
        return bot().chatLine(index).getText();
    }

    public String getTextOfLastChatLine() throws RemoteException {
        log.debug("text of the last chat line: "
            + bot().lastChatLine().getText());
        return bot().lastChatLine().getText();
    }

    public String getTextOfChatLine(String regex) throws RemoteException {
        log.debug("text of the chat line with the specifed regex: "
            + bot().chatLine(regex).getText());
        return bot().chatLine(regex).getText();
    }

    public void waitUntilGetChatMessage(String jid, String message)
        throws RemoteException {
        remoteBot().waitUntil(
            SarosConditions.isChatMessageExist(this, jid, message));
    }
}
