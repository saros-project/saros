package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.impl;

import static de.fu_berlin.inf.dpp.ui.View.ADD_CONTACT;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.impl.ControlBotImpl;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.IHTMLBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.ISuperBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.IContextMenusInContactListArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.IContextMenusInSessionArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.IChatroom;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.ISarosView;
import de.fu_berlin.inf.dpp.ui.View;
import java.rmi.RemoteException;
import java.util.List;
import org.apache.log4j.Logger;

/** The implementation of {@link ISarosView} to access the HTML-GUI */
public class SarosHTMLView extends StfRemoteObject implements ISarosView {

  private static final Logger log = Logger.getLogger(SarosHTMLView.class);

  private static final SarosHTMLView INSTANCE = new SarosHTMLView();

  private IHTMLBot htmlBot;

  private boolean connectedIndicator = false;

  public static SarosHTMLView getInstance() {
    return INSTANCE;
  }

  public ISarosView setBot(IHTMLBot htmlBot) {
    this.htmlBot = htmlBot;
    return this;
  }

  @Override
  public void connectWith(JID jid, String password, boolean forceReconnect) throws RemoteException {
    ControlBotImpl.getInstance()
        .getAccountManipulator()
        .addAccount(jid.getName(), password, jid.getDomain());

    boolean activated =
        ControlBotImpl.getInstance()
            .getAccountManipulator()
            .activateAccount(jid.getName(), jid.getDomain());

    // already connected with the given account
    if (!forceReconnect && isConnected() && !activated) return;

    if (isConnected()) {
      disconnect();
    }

    htmlBot.view(View.MAIN_VIEW).open();
    htmlBot.view(View.MAIN_VIEW).button(jid.getName()).click();

    connectedIndicator = true;
  }

  @Override
  public void connect() throws RemoteException {
    if (isConnected()) disconnect();

    if (getXmppAccountStore().isEmpty())
      throw new RuntimeException("unable to connect with the active account, it does not exists");

    htmlBot.view(View.MAIN_VIEW).open();
    String activeAccount = htmlBot.view(View.MAIN_VIEW).textElement("active-account").getText();
    htmlBot.view(View.MAIN_VIEW).button(activeAccount).click();

    connectedIndicator = true;
  }

  @Override
  public void disconnect() throws RemoteException {
    connectedIndicator = false;
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public void addContact(JID jid) throws RemoteException {
    htmlBot.view(ADD_CONTACT).open();
    htmlBot.view(ADD_CONTACT).inputField("jid").enter(jid.getBase());
    htmlBot.view(ADD_CONTACT).button("add-contact").click();
  }

  @Override
  public IContextMenusInContactListArea selectContact(JID jid) throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public IContextMenusInContactListArea selectContacts() throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public IContextMenusInSessionArea selectSession() throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public IContextMenusInSessionArea selectNoSessionRunning() throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public IContextMenusInSessionArea selectUser(JID jid) throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public IChatroom selectChatroom(String name) throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public IChatroom selectChatroomWithRegex(String regex) throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public void closeChatroom(String name) throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public void closeChatroomWithRegex(String regex) throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public boolean hasOpenChatrooms() throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public boolean isConnected() throws RemoteException {
    return connectedIndicator;
  }

  @Override
  public boolean isInContactList(JID jid) throws RemoteException {
    return htmlBot.getContactList(View.MAIN_VIEW).contains(jid.getBase());
  }

  @Override
  public String getNickname(JID jid) throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public boolean hasNickName(JID jid) throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public List<String> getContacts() throws RemoteException {
    return htmlBot.getContactList(View.MAIN_VIEW);
  }

  @Override
  public void waitUntilIsConnected() throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public void waitUntilIsDisconnected() throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public void sendFileToUser(JID jid) throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public void leaveSession() throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public void resolveInconsistency() throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public boolean isInSession() throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public boolean existsUser(JID jid) throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public boolean isHost() throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public boolean isFollowing() throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public List<String> getUsers() throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public JID getFollowedUser() throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public void waitUntilIsInconsistencyDetected() throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public void waitUntilIsInSession() throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public void waitUntilIsInviteeInSession(ISuperBot superBot) throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public void waitUntilIsNotInSession() throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }

  @Override
  public void waitUntilAllPeersLeaveSession(List<JID> jidsOfAllParticipants)
      throws RemoteException {
    throw new UnsupportedOperationException("Method is not yet implemented");
  }
}
