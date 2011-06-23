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

    public void sendChatMessage(String message) throws RemoteException {

        RemoteWorkbenchBot.getInstance().activateWorkbench();
        SarosSWTBotChatInput chatInput = SarosSWTBot.getInstance().chatInput();
        chatInput.setText(message);
        log.debug("inserted message in chat view: " + chatInput.getText());
        // chatInput.pressShortcut(Keystrokes.LF);
        chatInput.pressEnterKey();
    }

    public boolean compareChatMessage(String jid, String message)
        throws RemoteException {
        log.debug("chatLine: "
            + RemoteWorkbenchBot.getInstance().lastChatLine());
        log.debug("text of the lastChatLine: "
            + SarosSWTBot.getInstance().lastChatLine().getText());
        String text = SarosSWTBot.getInstance().lastChatLine().getText();
        return text.equals(message);
    }

    public String getUserNameOnChatLinePartnerChangeSeparator()
        throws RemoteException {
        log.debug("user name of the first chat line partner change separator: "
            + SarosSWTBot.getInstance().chatLinePartnerChangeSeparator()
                .getPlainID());
        return SarosSWTBot.getInstance().chatLinePartnerChangeSeparator()
            .getPlainID();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator(int index)
        throws RemoteException {
        log.debug("user name of the chat line partner change separator with the index"
            + index
            + ": "
            + SarosSWTBot.getInstance().chatLinePartnerChangeSeparator(index)
                .getPlainID());
        return SarosSWTBot.getInstance().chatLinePartnerChangeSeparator(index)
            .getPlainID();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator(String plainID)
        throws RemoteException {
        log.debug("user name of the chat line partner change separator with the plainID "
            + plainID
            + ": "
            + SarosSWTBot.getInstance().chatLinePartnerChangeSeparator(plainID)
                .getPlainID());
        return SarosSWTBot.getInstance()
            .chatLinePartnerChangeSeparator(plainID).getPlainID();
    }

    public String getTextOfChatLine() throws RemoteException {
        log.debug("text of the first chat line: "
            + SarosSWTBot.getInstance().chatLine().getText());
        return SarosSWTBot.getInstance().chatLine().getText();
    }

    public String getTextOfChatLine(int index) throws RemoteException {
        log.debug("text of the chat line with the index " + index + ": "
            + SarosSWTBot.getInstance().chatLine(index).getText());
        return SarosSWTBot.getInstance().chatLine(index).getText();
    }

    public String getTextOfLastChatLine() throws RemoteException {
        log.debug("text of the last chat line: "
            + SarosSWTBot.getInstance().lastChatLine().getText());
        return SarosSWTBot.getInstance().lastChatLine().getText();
    }

    public String getTextOfChatLine(String regex) throws RemoteException {
        log.debug("text of the chat line with the specifed regex: "
            + SarosSWTBot.getInstance().chatLine(regex).getText());
        return SarosSWTBot.getInstance().chatLine(regex).getText();
    }

    public void waitUntilGetChatMessage(String jid, String message)
        throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(
            SarosConditions.isChatMessageExist(this, jid, message));
    }
}
