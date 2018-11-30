package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.impl;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.IContextMenusInSessionArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;
import java.rmi.RemoteException;
import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public final class ContextMenusInSessionArea extends ContextMenusInSarosView
    implements IContextMenusInSessionArea {

  private static final Logger log = Logger.getLogger(ContextMenusInSessionArea.class);

  protected JID participantJID;

  private static final ContextMenusInSessionArea INSTANCE = new ContextMenusInSessionArea();

  public static ContextMenusInSessionArea getInstance() {
    return INSTANCE;
  }

  public void setParticipantJID(JID jid) {
    this.participantJID = jid;
  }

  @Override
  public void grantWriteAccess() throws RemoteException {
    log.trace("granting write access to: " + participantJID.getBase());

    if (hasWriteAccess()) {
      throw new RuntimeException(
          "user \"" + participantJID.getBase() + "\" already has write access!.");
    }

    log.trace("clicking on context menu item: " + CM_GRANT_WRITE_ACCESS);

    SWTBotTreeItem treeItem = getTreeItem();

    try {
      treeItem.select();
      ContextMenuHelper.clickContextMenu(tree, CM_GRANT_WRITE_ACCESS);
    } catch (RuntimeException e) {
      logError(log, e, tree, treeItem);
      throw e;
    }
    waitUntilHasWriteAccess();
  }

  @Override
  public void restrictToReadOnlyAccess() throws RemoteException {
    log.trace("revoking write access from: " + participantJID.getBase());

    if (!hasWriteAccess()) {
      throw new RuntimeException(
          "user \"" + participantJID.getBase() + "\" already has read-only access!");
    }

    log.trace("clicking on context menu item: " + CM_RESTRICT_TO_READ_ONLY_ACCESS);

    SWTBotTreeItem treeItem = getTreeItem();

    try {
      treeItem.select();
      ContextMenuHelper.clickContextMenu(tree, CM_RESTRICT_TO_READ_ONLY_ACCESS);
    } catch (RuntimeException e) {
      logError(log, e, tree, treeItem);
      throw e;
    }

    waitUntilHasReadOnlyAccess();
  }

  @Override
  public void followParticipant() throws RemoteException {
    log.trace("start following participant: " + participantJID.getBase());

    if (isFollowing()) {
      log.warn("you are already following participant: " + participantJID.getBase());
      return;
    }

    if (SuperBot.getInstance().getJID().equals(participantJID)) {
      throw new RuntimeException("you can't follow yourself");
    }

    log.trace("clicking on context menu item: " + CM_FOLLOW_PARTICIPANT);

    SWTBotTreeItem treeItem = getTreeItem();

    try {
      treeItem.select();
      ContextMenuHelper.clickContextMenu(tree, CM_FOLLOW_PARTICIPANT);
    } catch (RuntimeException e) {
      logError(log, e, tree, treeItem);
      throw e;
    }

    waitUntilIsFollowing();
  }

  @Override
  public void stopFollowing() throws RemoteException {
    log.trace("stop following user: " + participantJID.getBase());

    if (!isFollowing()) {
      log.warn("you are not following participant: " + participantJID.getBase());
      return;
    }

    SWTBotTreeItem treeItem = getTreeItem();

    log.trace("clicking on context menu item: " + CM_STOP_FOLLOWING);

    try {
      treeItem.select();
      ContextMenuHelper.clickContextMenu(tree, CM_STOP_FOLLOWING);
    } catch (RuntimeException e) {
      logError(log, e, tree, treeItem);
      throw e;
    }

    waitUntilIsNotFollowing();
  }

  @Override
  public void jumpToPositionOfSelectedParticipant() throws RemoteException {
    if (SuperBot.getInstance().getJID().equals(participantJID)) {
      throw new RuntimeException("you can't jump to the position of yourself");
    }

    SWTBotTreeItem treeItem = getTreeItem();

    log.trace("clicking on context menu item: " + CM_JUMP_TO_POSITION_OF_PARTICIPANT);

    try {
      treeItem.select();
      ContextMenuHelper.clickContextMenu(tree, CM_JUMP_TO_POSITION_OF_PARTICIPANT);
    } catch (RuntimeException e) {
      logError(log, e, tree, treeItem);
      throw e;
    }
  }

  @Override
  public void addProjects(String... projectNames) throws RemoteException {

    SWTBotTreeItem treeItem = getTreeItem();

    log.trace("clicking on context menu item: " + ADD_PROJECTS);

    try {
      treeItem.select();
      ContextMenuHelper.clickContextMenu(tree, ADD_PROJECTS);
    } catch (RuntimeException e) {
      logError(log, e, tree, treeItem);
      throw e;
    }

    SuperBot.getInstance().confirmShellAddProjectsToSession(projectNames);
  }

  @Override
  public void addContactsToSession(String... jidOfInvitees) throws RemoteException {
    SWTBotTreeItem treeItem = getTreeItem();

    log.trace("clicking on context menu item: " + CM_ADD_CONTACTS_TO_SESSION);

    try {
      treeItem.select();
      ContextMenuHelper.clickContextMenu(tree, CM_ADD_CONTACTS_TO_SESSION);
    } catch (RuntimeException e) {
      logError(log, e, tree, treeItem);
      throw e;
    }

    SuperBot.getInstance().confirmShellAddContactsToSession(jidOfInvitees);
  }

  @Override
  public void shareProjects(String projectName, JID... jids) throws RemoteException {

    SWTBotTreeItem treeItem = getTreeItem();

    log.trace("clicking on context menu item: " + SHARE_PROJECTS);

    try {
      treeItem.select();
      ContextMenuHelper.clickContextMenu(tree, SHARE_PROJECTS);
    } catch (RuntimeException e) {
      logError(log, e, tree, treeItem);
      throw e;
    }

    SuperBot.getInstance().confirmShellShareProjects(projectName, jids);
  }

  @Override
  public boolean hasWriteAccess() throws RemoteException {
    log.trace("checking if participant '" + participantJID.getBase() + "' has write access");

    SWTBotTreeItem treeItem = null;
    try {
      return !getTreeItem().getText().contains(READ_ONLY_ACCESS);
    } catch (RuntimeException e) {
      logError(log, e, tree, treeItem);
      return false;
    }
  }

  @Override
  public boolean hasReadOnlyAccess() throws RemoteException {
    log.trace("checking if participant '" + participantJID.getBase() + "' has read only access");
    SWTBotTreeItem treeItem = null;
    try {
      return getTreeItem().getText().contains(READ_ONLY_ACCESS);
    } catch (RuntimeException e) {
      logError(log, e, tree, treeItem);
      return false;
    }
  }

  @Override
  public boolean isFollowing() throws RemoteException {
    log.trace("checking if local user is following participant: " + participantJID.getBase());

    SWTBotTreeItem treeItem = null;
    try {
      return getTreeItem().getText().contains(FOLLOW_MODE_ENABLED)
          || getTreeItem().getText().contains(FOLLOW_MODE_PAUSED);
    } catch (RuntimeException e) {
      logError(log, e, tree, treeItem);
      return false;
    }
  }

  @Override
  public void waitUntilHasWriteAccess() throws RemoteException {
    log.trace("waiting for participant '" + participantJID.getBase() + "' to gain write access");
    new SWTBot()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return hasWriteAccess();
              }

              @Override
              public String getFailureMessage() {
                return "unable to grant write access to " + participantJID.getBase();
              }
            });
  }

  @Override
  public void waitUntilHasReadOnlyAccess() throws RemoteException {
    log.trace(
        "waiting for participant '" + participantJID.getBase() + "' to gain read only access");
    new SWTBot()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return hasReadOnlyAccess();
              }

              @Override
              public String getFailureMessage() {
                return "unable to restrict " + participantJID.getBase() + " to read-only access";
              }
            });
  }

  @Override
  public void waitUntilIsFollowing() throws RemoteException {
    log.trace("waiting to follow participant: " + participantJID.getBase());
    new SWTBot()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return isFollowing();
              }

              @Override
              public String getFailureMessage() {
                return "unable to follow " + participantJID.getBase();
              }
            });
  }

  @Override
  public void waitUntilIsNotFollowing() throws RemoteException {
    log.trace("waiting to stop following participant: " + participantJID.getBase());
    new SWTBot()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return !isFollowing();
              }

              @Override
              public String getFailureMessage() {
                return "unable to stop following mode on user" + participantJID.getBase();
              }
            });
  }
}
