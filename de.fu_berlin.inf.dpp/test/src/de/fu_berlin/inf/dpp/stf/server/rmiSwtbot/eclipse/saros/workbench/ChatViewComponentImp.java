package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.sarosSWTBot.widgets.SarosSWTBotChatInput;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

public class ChatViewComponentImp extends EclipseComponent implements
    ChatViewComponent {

    private static transient ChatViewComponentImp self;

    /*
     * View infos
     */
    private final static String VIEWNAME = "Chat View";
    private final static String VIEWID = "de.fu_berlin.inf.dpp.ui.chat.ChatView";

    /**
     * {@link ChatViewComponentImp} is a singleton, but inheritance is possible.
     */
    public static ChatViewComponentImp getInstance() {
        if (self != null)
            return self;
        self = new ChatViewComponentImp();
        return self;
    }

    public void activateChatView() throws RemoteException {
        basicC.setFocusOnViewByTitle(VIEWNAME);
    }

    public void openChatView() throws RemoteException {
        if (!isChatViewOpen())
            basicC.openViewById(VIEWID);
    }

    public void closeChatView() throws RemoteException {
        basicC.closeViewById(VIEWID);
    }

    public boolean isChatViewOpen() throws RemoteException {
        return basicC.isViewOpen(VIEWNAME);
    }

    public void waitUntilGetChatMessage(String jid, String message)
        throws RemoteException {
        waitUntil(SarosConditions.isChatMessageExist(this, jid, message));
    }

    public void sendChatMessage(String message) throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        SarosSWTBotChatInput chatInput = bot.chatInput();
        chatInput.setText(message);
        bot.text();
        log.debug("inerted message in chat view: " + chatInput.getText());
        // chatInput.pressShortcut(Keystrokes.LF);
        chatInput.pressEnterKey();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator()
        throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("user name of the first chat line partner change separator: "
            + bot.chatLinePartnerChangeSeparator().getPlainID());
        return bot.chatLinePartnerChangeSeparator().getPlainID();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator(int index)
        throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("user name of the chat line partner change separator with the index"
            + index
            + ": "
            + bot.chatLinePartnerChangeSeparator(index).getPlainID());
        return bot.chatLinePartnerChangeSeparator(index).getPlainID();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator(String plainID)
        throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("user name of the chat line partner change separator with the plainID "
            + plainID
            + ": "
            + bot.chatLinePartnerChangeSeparator(plainID).getPlainID());
        return bot.chatLinePartnerChangeSeparator(plainID).getPlainID();
    }

    public String getTextOfChatLine() throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("text of the first chat line: " + bot.chatLine().getText());
        return bot.chatLine().getText();
    }

    public String getTextOfChatLine(int index) throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("text of the chat line with the index " + index + ": "
            + bot.chatLine(index).getText());
        return bot.chatLine(index).getText();
    }

    public String getTextOfLastChatLine() throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("text of the last chat line: " + bot.lastChatLine().getText());
        return bot.lastChatLine().getText();
    }

    public String getTextOfChatLine(String regex) throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("text of the chat line with the specifed regex: "
            + bot.chatLine(regex).getText());
        return bot.chatLine(regex).getText();
    }

    public boolean compareChatMessage(String jid, String message)
        throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("chatLine: " + bot.lastChatLine());
        log.debug("text of the lastChatLine: " + bot.lastChatLine().getText());
        String text = bot.lastChatLine().getText();
        return text.equals(message);
    }

}
