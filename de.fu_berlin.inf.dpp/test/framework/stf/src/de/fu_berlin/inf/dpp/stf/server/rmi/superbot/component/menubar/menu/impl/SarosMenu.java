package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.impl;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.ISarosMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.submenu.ISarosPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.submenu.impl.SarosPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;
import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;

public final class SarosMenu extends StfRemoteObject implements ISarosMenu {

  private static final SarosMenu INSTANCE = new SarosMenu();

  private SWTBotMenu menu;

  public static SarosMenu getInstance() {
    return INSTANCE;
  }

  public ISarosMenu setMenu(SWTBotMenu menu) {
    this.menu = menu;
    return this;
  }

  @Override
  public void createAccount(JID jid, String password) throws RemoteException {
    RemoteWorkbenchBot.getInstance().activateWorkbench();
    menu.menu(MENU_CREATE_ACCOUNT).click();
    SuperBot.getInstance().confirmShellCreateNewXMPPAccount(jid, password);
  }

  @Override
  public void addContact(JID jid) throws RemoteException {
    RemoteWorkbenchBot.getInstance().activateWorkbench();
    menu.menu(MENU_ADD_CONTACT).click();
    SuperBot.getInstance().confirmShellAddContact(jid);
  }

  @Override
  public void addContactsToSession(String... jidOfInvitees) throws RemoteException {
    RemoteWorkbenchBot.getInstance().activateWorkbench();
    menu.menu(MENU_ADD_CONTACTS_TO_SESSION).click();
    SuperBot.getInstance().confirmShellAddContactsToSession(jidOfInvitees);
  }

  @Override
  public void shareProjects(String projectName, JID... jids) throws RemoteException {
    RemoteWorkbenchBot.getInstance().activateWorkbench();
    menu.menu(SHARE_PROJECTS).click();
    SuperBot.getInstance().confirmShellShareProjects(projectName, jids);
  }

  @Override
  public void shareProjectFiles(String project, String[] files, JID... jids)
      throws RemoteException {
    RemoteWorkbenchBot.getInstance().activateWorkbench();
    menu.menu(SHARE_PROJECTS).click();
    SuperBot.getInstance().confirmShellShareProjectFiles(project, files, jids);
  }

  @Override
  public void shareProjects(String[] projectNames, JID... jids) throws RemoteException {
    RemoteWorkbenchBot.getInstance().activateWorkbench();
    menu.menu(SHARE_PROJECTS).click();
    SuperBot.getInstance().confirmShellShareProjects(projectNames, jids);
  }

  @Override
  public void addProject(String project, String[] files) throws RemoteException {
    RemoteWorkbenchBot.getInstance().activateWorkbench();
    menu.menu(ADD_PROJECTS).click();
    SuperBot.getInstance().confirmShellAddProjectToSession(project, files);
  }

  @Override
  public void addProjects(String... projectNames) throws RemoteException {
    RemoteWorkbenchBot.getInstance().activateWorkbench();
    menu.menu(ADD_PROJECTS).click();
    SuperBot.getInstance().confirmShellAddProjectsToSession(projectNames);
  }

  @Override
  public void stopSession() throws RemoteException {
    RemoteWorkbenchBot.getInstance().activateWorkbench();
    menu.menu(CM_STOP_SAROS_SESSION).click();
    SuperBot.getInstance().confirmShellLeavingClosingSession();
  }

  @Override
  public ISarosPreferences preferences() throws RemoteException {
    return SarosPreferences.getInstance();
  }
}
