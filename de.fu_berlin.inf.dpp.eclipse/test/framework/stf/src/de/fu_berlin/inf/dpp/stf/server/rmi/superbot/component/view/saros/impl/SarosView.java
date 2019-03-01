package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.impl;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withRegex;

import de.fu_berlin.inf.dpp.editor.FollowModeManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.impl.ControlBotImpl;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.ISuperBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.IContextMenusInContactListArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.IContextMenusInSessionArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.impl.ContextMenusInContactListArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.impl.ContextMenusInSessionArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.IChatroom;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.ISarosView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;
import de.fu_berlin.inf.dpp.stf.server.util.WidgetUtil;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCTabItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.hamcrest.Matcher;
import org.jivesoftware.smack.Roster;

/**
 * This implementation of {@link ISarosView}
 *
 * @author lchen
 */
public final class SarosView extends StfRemoteObject implements ISarosView {

  private static final Logger log = Logger.getLogger(SarosView.class);

  private static final SarosView INSTANCE = new SarosView();

  private SWTBotView view;
  private SWTBotTree tree;

  public static SarosView getInstance() {
    return INSTANCE;
  }

  public ISarosView setView(SWTBotView view) {
    setViewWithTree(view);
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
      clickToolbarButtonWithTooltip(TB_DISCONNECT);
      waitUntilIsDisconnected();
    }

    clickToolbarButtonWithTooltip(TB_CONNECT);
    waitUntilIsConnected();
  }

  @Override
  public void connect() throws RemoteException {
    if (isConnected()) disconnect();

    if (getXmppAccountStore().isEmpty())
      throw new RuntimeException("unable to connect with the active account, it does not exists");

    clickToolbarButtonWithTooltip(TB_CONNECT);
    waitUntilIsConnected();
  }

  @Override
  public void disconnect() throws RemoteException {
    if (isConnected()) {
      clickToolbarButtonWithTooltip(TB_DISCONNECT);
      waitUntilIsDisconnected();
    }
  }

  /*
   * FIXME: there are some problems by clicking the toolbarDropDownButton.
   */
  @SuppressWarnings("unused")
  private void selectConnectAccount(String baseJID) throws RemoteException {
    SWTBotToolbarDropDownButton b = view.toolbarDropDownButton(TB_CONNECT);
    @SuppressWarnings("static-access")
    Matcher<MenuItem> withRegex = WidgetMatcherFactory.withRegex(Pattern.quote(baseJID) + ".*");
    b.menuItem(withRegex).click();
    try {
      b.pressShortcut(KeyStroke.getInstance("ESC"));
    } catch (ParseException e) {
      log.debug("", e);
    }
  }

  @Override
  public void addContact(JID jid) throws RemoteException {
    if (!isInContactList(jid)) {
      clickToolbarButtonWithTooltip(TB_ADD_NEW_CONTACT);
      SuperBot.getInstance().confirmShellAddContact(jid);
    }
    // wait for update of the saros session tree
    new SWTBot().sleep(500);
  }

  @Override
  public void sendFileToUser(JID jid) throws RemoteException {
    selectParticipant(jid, "you cannot send a file to youself");
    clickToolbarButtonWithTooltip(TB_SEND_A_FILE_TO_SELECTED_CONTACT);
  }

  @Override
  public void leaveSession() throws RemoteException {
    if (isInSession()) {
      if (!isHost()) {
        clickToolbarButtonWithTooltip(TB_LEAVE_SESSION);
        SWTBotShell shell = new SWTBot().shell(SHELL_CONFIRM_LEAVING_SESSION);
        shell.activate();
        shell.bot().button(YES).click();
      } else {
        boolean isLastInSession = tree.getTreeItem(NODE_SESSION).getNodes().size() == 1;
        clickToolbarButtonWithTooltip(TB_STOP_SESSION);
        if (!isLastInSession) {
          SWTBotShell shell = new SWTBot().shell(SHELL_CONFIRM_CLOSING_SESSION);
          shell.activate();
          shell.bot().button(YES).click();
        }
      }
      waitUntilIsNotInSession();
    }
  }

  /**
   * Note: {@link StfRemoteObject#TB_INCONSISTENCY_DETECTED} is not complete toolbarName, so we need
   * to use {@link IRemoteBotView#toolbarButtonWithRegex(String)} to perform this action.
   */
  @Override
  public void resolveInconsistency() throws RemoteException {
    WidgetUtil.getToolbarButtonWithRegex(view, Pattern.quote(TB_INCONSISTENCY_DETECTED) + ".*")
        .click();

    SWTBot bot;
    bot = new SWTBot();
    bot.shell(SHELL_CONFIRM_CONSISTENCY_RECOVERY).bot().button(OK).click();

    bot = new SWTBot();
    bot.sleep(1000);
    bot.waitWhile(
        Conditions.shellIsActive(SHELL_PROGRESS_INFORMATION),
        SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);
  }

  /**
   * ********************************************
   *
   * <p>Content of Saros View
   *
   * <p>********************************************
   */
  @Override
  public IContextMenusInContactListArea selectContacts() throws RemoteException {
    initContactsContextMenuWrapper(Pattern.quote((NODE_CONTACTS)));
    return ContextMenusInContactListArea.getInstance();
  }

  @Override
  public IContextMenusInContactListArea selectContact(JID jid) throws RemoteException {
    if (getNickname(jid) == null) {
      throw new RuntimeException("no contact exists with the JID: " + jid.getBase());
    }
    initContactsContextMenuWrapper(
        Pattern.quote(NODE_CONTACTS), Pattern.quote(getNickname(jid)) + ".*");
    return ContextMenusInContactListArea.getInstance();
  }

  @Override
  public IContextMenusInSessionArea selectSession() throws RemoteException {
    if (!isInSession()) throw new RuntimeException("you are not in a session");
    initSessionContextMenuWrapper(Pattern.quote((NODE_SESSION)));
    return ContextMenusInSessionArea.getInstance();
  }

  @Override
  public IContextMenusInSessionArea selectNoSessionRunning() throws RemoteException {
    if (isInSession()) throw new RuntimeException("you are in a session");
    initSessionContextMenuWrapper(Pattern.quote((NODE_NO_SESSION_RUNNING)));
    return ContextMenusInSessionArea.getInstance();
  }

  @Override
  public IContextMenusInSessionArea selectUser(final JID participantJID) throws RemoteException {
    if (!isInSession()) throw new IllegalStateException("you are not in a session");
    String participantLabel = getLabelName(participantJID);
    initSessionContextMenuWrapper(
        Pattern.quote(NODE_SESSION), ".*" + Pattern.quote(participantLabel) + ".*");
    ContextMenusInSessionArea.getInstance().setParticipantJID(participantJID);
    return ContextMenusInSessionArea.getInstance();
  }

  /**
   * ********************************************
   *
   * <p>state
   *
   * <p>********************************************
   */
  @Override
  public boolean isConnected() {
    return isToolbarButtonEnabled(TB_DISCONNECT);
  }

  public boolean isDisconnected() {
    return isToolbarButtonEnabled(TB_CONNECT);
  }

  @Override
  public String getNickname(JID jid) throws RemoteException {
    Roster roster = getConnectionService().getRoster();

    if (roster == null) throw new IllegalStateException("not connected to a xmpp server");

    if (roster.getEntry(jid.getBase()) == null) return null;
    if (roster.getEntry(jid.getBase()).getName() == null) return jid.getBase();
    else return roster.getEntry(jid.getBase()).getName();
  }

  @Override
  public boolean hasNickName(JID jid) throws RemoteException {
    try {
      if (getNickname(jid) == null) return false;
      if (!getNickname(jid).equals(jid.getBase())) return true;
      return false;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public List<String> getContacts() throws RemoteException {
    SWTBotTreeItem items = tree.getTreeItem(NODE_CONTACTS);
    List<String> contacts = new ArrayList<String>();

    for (SWTBotTreeItem item : items.getItems()) contacts.add(item.getText());

    return contacts;
  }

  @Override
  public boolean isInContactList(JID jid) throws RemoteException {
    try {
      String nickName = getNickname(jid);
      if (nickName == null) return false;

      nickName = Pattern.quote(nickName) + ".*";
      for (String label : tree.getTreeItem(NODE_CONTACTS).getNodes())
        if (label.matches(nickName)) return true;

      return false;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean existsUser(JID jid) throws RemoteException {
    try {
      String participantLabel = getLabelName(jid);

      participantLabel = ".*" + Pattern.quote(participantLabel) + ".*";
      for (String label : tree.getTreeItem(NODE_SESSION).getNodes())
        if (label.matches(participantLabel)) return true;

      return false;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean isInSession() {
    try {
      for (SWTBotToolbarButton button : view.getToolbarButtons()) {
        if ((button.getToolTipText().equals(TB_STOP_SESSION)
                || button.getToolTipText().equals(TB_LEAVE_SESSION))
            && button.isEnabled()) return true;
      }
      return false;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean isHost() throws RemoteException {
    try {
      if (!isInSession()) return false;

      List<String> participants = tree.getTreeItem(NODE_SESSION).getNodes();

      if (participants.size() == 0) return false;

      for (String participant : participants) {
        if (participant.contains(HOST_INDICATION)) {
          if (participant.contains(getLabelName(SuperBot.getInstance().getJID()))) {
            return true;
          }
        }
      }
      return false;

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean isFollowing() throws RemoteException {
    try {
      JID followee = getFollowedUser();
      if (followee == null) return false;

      return selectUser(followee).isFollowing();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public List<String> getUsers() throws RemoteException {
    return tree.getTreeItem(NODE_SESSION).getNodes();
  }

  @Override
  public JID getFollowedUser() {
    FollowModeManager followModeManager = getFollowModeManager();

    if (followModeManager == null) return null;

    User followedUser = followModeManager.getFollowedUser();

    return (followedUser != null) ? followedUser.getJID() : null;
  }

  @Override
  public IChatroom selectChatroom(String name) throws RemoteException {
    return selectChatroomWithRegex(Pattern.quote(name));
  }

  @SuppressWarnings("unchecked")
  @Override
  public IChatroom selectChatroomWithRegex(String regex) throws RemoteException {
    Chatroom.getInstance()
        .setChatTab(
            new SWTBotCTabItem(
                (CTabItem)
                    view.bot()
                        .widget(
                            allOf(widgetOfType(CTabItem.class), withRegex(regex)),
                            view.getWidget())));

    return Chatroom.getInstance();
  }

  @Override
  public void closeChatroom(String name) throws RemoteException {
    closeChatroomWithRegex(Pattern.quote(name));
  }

  @SuppressWarnings("unchecked")
  @Override
  public void closeChatroomWithRegex(String regex) throws RemoteException {
    List<? extends Widget> chatTabs =
        view.bot().widgets(allOf(widgetOfType(CTabItem.class), withRegex(regex)), view.getWidget());

    for (Widget chatTab : chatTabs) new SWTBotCTabItem((CTabItem) chatTab).close();
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean hasOpenChatrooms() throws RemoteException {
    return !view.bot()
        .getFinder()
        .findControls(view.getWidget(), allOf(widgetOfType(CTabItem.class), withRegex(".*")), true)
        .isEmpty();
  }

  /*
   * waits until
   */

  @Override
  public void waitUntilIsConnected() throws RemoteException {
    new SWTBot()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return isConnected();
              }

              @Override
              public String getFailureMessage() {
                return "unable to connect to server";
              }
            },
            SarosSWTBotPreferences.SAROS_DEFAULT_TIMEOUT);
  }

  @Override
  public void waitUntilIsDisconnected() throws RemoteException {
    new SWTBot()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return isDisconnected();
              }

              @Override
              public String getFailureMessage() {
                return "unable to disconnect from server";
              }
            },
            SarosSWTBotPreferences.SAROS_DEFAULT_TIMEOUT);
  }

  @Override
  public void waitUntilIsInSession() throws RemoteException {
    new SWTBot()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return isInSession();
              }

              @Override
              public String getFailureMessage() {
                return "joining the session failed";
              }
            });
  }

  @Override
  public void waitUntilIsInviteeInSession(ISuperBot sarosBot) throws RemoteException {
    sarosBot.views().sarosView().waitUntilIsInSession();
  }

  @Override
  public void waitUntilIsNotInSession() throws RemoteException {
    new SWTBot()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return !isInSession();
              }

              @Override
              public String getFailureMessage() {
                return "leaving the session failed";
              }
            });
  }

  @Override
  public void waitUntilAllPeersLeaveSession(final List<JID> jidsOfAllParticipants)
      throws RemoteException {

    /*
     * see STFController which manipulates the behavior of how the
     * HostAloneInSession dialog is displayed
     */

    // new SWTBot().waitUntil(new DefaultCondition() {
    // @Override
    // public boolean test() throws Exception {
    // for (JID jid : jidsOfAllParticipants) {
    // if (existsParticipant(jid))
    // return false;
    // }
    // return true;
    // }
    //
    // @Override
    // public String getFailureMessage() {
    // return "there are still users in the session";
    // }
    // });

    waitUntilIsNotInSession();
  }

  @Override
  public void waitUntilIsInconsistencyDetected() throws RemoteException {
    RemoteWorkbenchBot.getInstance()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return WidgetUtil.getToolbarButtonWithRegex(
                        view, Pattern.quote(TB_INCONSISTENCY_DETECTED) + ".*")
                    .isEnabled();
              }

              @Override
              public String getFailureMessage() {
                return "the toolbar button " + TB_INCONSISTENCY_DETECTED + " is not enabled";
              }
            });
  }

  /**
   * ************************************************************
   *
   * <p>inner functions
   *
   * <p>************************************************************
   */
  private String getLabelName(JID jid) throws RemoteException {
    String contactLabel;

    if (SuperBot.getInstance().views().sarosView().hasNickName(jid)) {

      contactLabel = SuperBot.getInstance().views().sarosView().getNickname(jid);
    } else {
      contactLabel = jid.getName();
    }
    return contactLabel;
  }

  private boolean isToolbarButtonEnabled(String tooltip) {

    try {
      for (SWTBotToolbarButton button : view.getToolbarButtons())
        if (button.getToolTipText().equals(tooltip) && button.isEnabled()) return true;

      return false;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }

  private void clickToolbarButtonWithTooltip(String tooltipText) {
    WidgetUtil.getToolbarButtonWithRegex(view, Pattern.quote(tooltipText) + ".*").click();
  }

  private void selectParticipant(JID jidOfSelectedUser, String message) throws RemoteException {
    if (SuperBot.getInstance().getJID().equals(jidOfSelectedUser)) {
      throw new RuntimeException(message);
    }
    selectUser(jidOfSelectedUser);
  }

  private void setViewWithTree(SWTBotView view) {
    this.view = view;
    this.tree = view.bot().tree();
  }

  private void initSessionContextMenuWrapper(String... treeItemNodes) {
    ContextMenusInSessionArea.getInstance().setTree(tree);
    ContextMenusInSessionArea.getInstance().setTreeItemNodes(treeItemNodes);
    ContextMenusInSessionArea.getInstance().setSarosView(this);
  }

  private void initContactsContextMenuWrapper(String... treeItemNodes) {
    ContextMenusInContactListArea.getInstance().setTree(tree);
    ContextMenusInContactListArea.getInstance().setTreeItemNodes(treeItemNodes);
    ContextMenusInContactListArea.getInstance().setSarosView(this);
  }
}
