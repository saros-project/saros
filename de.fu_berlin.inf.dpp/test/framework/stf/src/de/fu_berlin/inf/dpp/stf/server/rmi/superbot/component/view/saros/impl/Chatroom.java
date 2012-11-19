package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.bot.condition.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.SarosSWTBotChatInput;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.IChatroom;

public final class Chatroom extends StfRemoteObject implements IChatroom {

    private static final Logger log = Logger.getLogger(Chatroom.class);

    private static final Chatroom INSTANCE = new Chatroom();

    public static Chatroom getInstance() {
        return INSTANCE;
    }

    public boolean compareChatMessage(String jid, String message)
        throws RemoteException {
        try {
            SarosSWTBot bot = new SarosSWTBot();
            log.debug("chatLine: "
                + RemoteWorkbenchBot.getInstance().lastChatLine());
            log.debug("text of the lastChatLine: "
                + bot.lastChatLine().getText());
            String text = bot.lastChatLine().getText();
            return text.equals(message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void enterChatMessage(String message) throws RemoteException {
        RemoteWorkbenchBot.getInstance().activateWorkbench();
        SarosSWTBotChatInput chatInput = new SarosSWTBot().chatInput();
        chatInput.typeText(message);
    }

    @Override
    public void clearChatMessage() throws RemoteException {
        RemoteWorkbenchBot.getInstance().activateWorkbench();
        SarosSWTBotChatInput chatInput = new SarosSWTBot().chatInput();
        chatInput.setText("");
    }

    public void sendChatMessage() throws RemoteException {
        RemoteWorkbenchBot.getInstance().activateWorkbench();
        SarosSWTBotChatInput chatInput = new SarosSWTBot().chatInput();
        chatInput.pressEnterKey();
    }

    public void sendChatMessage(String message) throws RemoteException {
        RemoteWorkbenchBot.getInstance().activateWorkbench();
        SarosSWTBotChatInput chatInput = new SarosSWTBot().chatInput();
        chatInput.setText(message);
        // chatInput.pressShortcut(Keystrokes.LF);
        chatInput.pressEnterKey();
    }

    public String getChatMessage() throws RemoteException {
        RemoteWorkbenchBot.getInstance().activateWorkbench();
        SarosSWTBotChatInput chatInput = new SarosSWTBot().chatInput();
        return chatInput.getText();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator()
        throws RemoteException {
        SarosSWTBot bot = new SarosSWTBot();
        log.debug("user name of the first chat line partner change separator: "
            + bot.chatLinePartnerChangeSeparator().getPlainID());
        return bot.chatLinePartnerChangeSeparator().getPlainID();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator(int index)
        throws RemoteException {

        SarosSWTBot bot = new SarosSWTBot();

        log.debug("user name of the chat line partner change separator with the index"
            + index
            + ": "
            + bot.chatLinePartnerChangeSeparator(index).getPlainID());
        return bot.chatLinePartnerChangeSeparator(index).getPlainID();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator(String plainID)
        throws RemoteException {

        SarosSWTBot bot = new SarosSWTBot();

        log.debug("user name of the chat line partner change separator with the plainID "
            + plainID
            + ": "
            + bot.chatLinePartnerChangeSeparator(plainID).getPlainID());
        return bot.chatLinePartnerChangeSeparator(plainID).getPlainID();
    }

    public String getTextOfFirstChatLine() throws RemoteException {

        SarosSWTBot bot = new SarosSWTBot();

        log.debug("text of the first chat line: " + bot.chatLine().getText());
        return bot.chatLine().getText();
    }

    public String getTextOfChatLine(int index) throws RemoteException {

        SarosSWTBot bot = new SarosSWTBot();

        log.debug("text of the chat line with the index " + index + ": "
            + bot.chatLine(index).getText());
        return bot.chatLine(index).getText();
    }

    public String getTextOfLastChatLine() throws RemoteException {

        SarosSWTBot bot = new SarosSWTBot();

        log.debug("text of the last chat line: " + bot.lastChatLine().getText());
        return bot.lastChatLine().getText();
    }

    public String getTextOfChatLine(String regex) throws RemoteException {

        SarosSWTBot bot = new SarosSWTBot();

        log.debug("text of the chat line with the specifed regex: "
            + bot.chatLine(regex).getText());
        return bot.chatLine(regex).getText();
    }

    public void waitUntilGetChatMessage(String jid, String message)
        throws RemoteException {
        new SarosSWTBot().waitUntil(SarosConditions.isChatMessageExist(this,
            jid, message));
    }
}
