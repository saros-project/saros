package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.basicWidgets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotView;
import de.fu_berlin.inf.dpp.ui.preferencePages.GeneralPreferencePage;

public class TestBasicWidgetTree extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        deleteAllProjectsByActiveTesters();
    }

    @Test
    public void existsTreeItemInShell() throws RemoteException {
        alice.remoteBot().menu(MENU_WINDOW).menu(MENU_SHOW_VIEW).menu(MENU_OTHER)
            .click();
        alice.remoteBot().waitUntilShellIsOpen(SHELL_SHOW_VIEW);
        IRemoteBotShell shell = alice.remoteBot().shell(SHELL_SHOW_VIEW);
        shell.activate();
        assertTrue(shell.bot().tree().selectTreeItem(NODE_GENERAL)
            .existsSubItem(NODE_CONSOLE));

    }

    @Test
    public void existsTreeItemInShell2() throws RemoteException {
        alice.remoteBot().menu(MENU_SAROS).menu(MENU_PREFERENCES).click();
        alice.remoteBot().waitUntilShellIsOpen(SHELL_PREFERNCES);
        IRemoteBotShell shell = alice.remoteBot().shell(SHELL_PREFERNCES);
        shell.activate();
        assertTrue(shell.bot().tree()
            .selectTreeItem(NODE_GENERAL, NODE_EDITORS, NODE_TEXT_EDITORS)
            .existsSubItem(NODE_ANNOTATIONS));
    }

    @Test
    public void existsTreeItemWithRegexsInShell() throws RemoteException {
        alice.remoteBot().menu(MENU_SAROS).menu(MENU_PREFERENCES).click();

        alice.remoteBot().waitUntilShellIsOpen(SHELL_PREFERNCES);
        IRemoteBotShell shell = alice.remoteBot().shell(SHELL_PREFERNCES);
        shell.activate();
        assertTrue(shell.bot().tree()
            .selectTreeItem(NODE_GENERAL, NODE_EDITORS, NODE_TEXT_EDITORS)
            .existsSubItemWithRegex(NODE_ANNOTATIONS));
    }

    @Test
    public void existsTreeItemInView() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS1);
        IRemoteBotView view = alice.remoteBot().view(VIEW_PACKAGE_EXPLORER);
        view.show();
        assertTrue(view.bot().tree().existsSubItem(PROJECT1));

        assertTrue(alice.superBot().views().packageExplorerView().tree()
            .existsWithRegex(PROJECT1));
        assertTrue(view.bot().tree().selectTreeItem(PROJECT1, SRC, PKG1)
            .existsSubItem(CLS1 + SUFFIX_JAVA));
        alice.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS1_SUFFIX);
    }

    @Test
    public void existsTreeItemWithRegexsInView() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS1);
        IRemoteBotView view = alice.remoteBot().view(VIEW_PACKAGE_EXPLORER);
        view.show();

        assertTrue(view.bot().tree()
            .existsSubItemWithRegex(changeToRegex(PROJECT1)));
        assertTrue(alice.superBot().views().packageExplorerView().tree()
            .existsWithRegex(PROJECT1));
        assertTrue(view.bot().tree().selectTreeItem(PROJECT1, SRC)
            .existsSubItemWithRegex(changeToRegex(PKG1)));

        assertTrue(alice.superBot().views().packageExplorerView()
            .selectSrc(PROJECT1).existsWithRegex(PKG1));
        assertTrue(view.bot().tree().selectTreeItem(PROJECT1, SRC, PKG1)
            .existsSubItemWithRegex(changeToRegex(CLS1)));

        assertTrue(alice.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS1_SUFFIX));
        alice.superBot().views().packageExplorerView().tree().newC()
            .cls(PROJECT1, PKG1, CLS2);

        assertTrue(view.bot().tree().selectTreeItem(PROJECT1, SRC, PKG1)
            .existsSubItemWithRegex(changeToRegex(CLS2)));
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS2_SUFFIX));
    }

    @Test
    public void existsTreeItemWithRegexsInView2() throws RemoteException {
        alice.superBot().views().sarosView()
            .connectWith(alice.getJID(), alice.getPassword());
        assertTrue(alice.remoteBot().view(VIEW_SAROS).bot().tree()
            .selectTreeItem(NODE_BUDDIES)
            .existsSubItemWithRegex(changeToRegex("bob_stf")));
    }

    @Test
    public void selectTreeItemInView() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.remoteBot().view(VIEW_PACKAGE_EXPLORER).show();
        assertTrue(alice.remoteBot().isEditorOpen(CLS1_SUFFIX));
        alice.remoteBot().view(VIEW_PACKAGE_EXPLORER)
            .toolbarButtonWithRegex(TB_COLLAPSE_ALL).click();
        alice.remoteBot().view(VIEW_PACKAGE_EXPLORER).bot().tree()
            .selectTreeItem(PROJECT1, SRC, PKG1, CLS1_SUFFIX);
        alice.remoteBot().menu(MENU_FILE).menu(MENU_CLOSE).click();

        assertFalse(alice.remoteBot().isEditorOpen(CLS1_SUFFIX));
    }

    @Test
    public void selectTreeItemInShell() throws RemoteException {
        alice.remoteBot().menu(MENU_SAROS).menu(MENU_PREFERENCES).click();

        alice.remoteBot().waitUntilShellIsOpen(SHELL_PREFERNCES);
        IRemoteBotShell shell = alice.remoteBot().shell(SHELL_PREFERNCES);
        shell.activate();
        shell.bot().tree().selectTreeItem(NODE_SAROS);
        assertTrue(shell
            .bot()
            .buttonInGroup(GeneralPreferencePage.CHANGE_BTN_TEXT,
                GeneralPreferencePage.ACCOUNT_GROUP_TITLE).isVisible());
    }

    @Test
    public void selectTreeItemWithRegexs() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(SVN_PROJECT_COPY);
        alice
            .superBot()
            .views()
            .packageExplorerView()
            .selectProject(SVN_PROJECT_COPY)
            .team()
            .shareProjectUsingSpecifiedFolderName(SVN_REPOSITORY_URL,
                SVN_PROJECT_PATH);

        IRemoteBotView view = alice.remoteBot().view(VIEW_PACKAGE_EXPLORER);
        view.show();
        view.bot()
            .tree()
            .selectTreeItemWithRegex(
                changeToRegex(getClassNodes(SVN_PROJECT_COPY, SVN_PKG, SVN_CLS1)));
    }

    @Test
    public void selectTreeItemWithRegexsInView() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(SVN_PROJECT_COPY);
        alice
            .superBot()
            .views()
            .packageExplorerView()
            .selectProject(SVN_PROJECT_COPY)
            .team()
            .shareProjectUsingSpecifiedFolderName(SVN_REPOSITORY_URL,
                SVN_PROJECT_PATH);
        alice.remoteBot().view(VIEW_PACKAGE_EXPLORER).show();
        alice.superBot().views().packageExplorerView()
            .selectClass(SVN_PROJECT_COPY, SVN_PKG, SVN_CLS1).open();

        alice.remoteBot().editor(SVN_CLS1_SUFFIX).setTextWithoutSave(CP1);
        assertTrue(alice.remoteBot().editor(SVN_CLS1_SUFFIX).isDirty());
        alice.remoteBot().view(VIEW_PACKAGE_EXPLORER)
            .toolbarButtonWithRegex(TB_COLLAPSE_ALL + ".*").click();

        alice
            .remoteBot()
            .view(VIEW_PACKAGE_EXPLORER)
            .bot()
            .tree()
            .selectTreeItemWithRegex(
                changeToRegex(getClassNodes(SVN_PROJECT_COPY, SVN_PKG, SVN_CLS1)));
        alice.remoteBot().menu(MENU_FILE).menu(MENU_SAVE).click();
        assertFalse(alice.remoteBot().editor(SVN_CLS1_SUFFIX).isDirty());
    }

    @Test
    public void existsContextOfTreeItemInView() throws RemoteException {
        alice.superBot().views().sarosView()
            .connectWith(alice.getJID(), alice.getPassword());
        assertTrue(alice.remoteBot().view(VIEW_SAROS).bot().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES, "bob_stf.*")
            .existsContextMenu(CM_RENAME));
    }

    @Test
    public void existsSubmenuOfContextOfTreeItemInView() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        String[] contextNames = { MENU_NEW, MENU_PACKAGE };
        assertTrue(alice.remoteBot().view(VIEW_PACKAGE_EXPLORER).bot().tree()
            .selectTreeItem(PROJECT1).existsContextMenu(contextNames));

    }

    @Test
    public void isContextOfTreeItemInViewEnabled() throws RemoteException {
        alice.superBot().views().sarosView()
            .connectWith(alice.getJID(), alice.getPassword());
        alice.superBot().views().sarosView().waitUntilIsConnected();
        assertTrue(alice.remoteBot().view(VIEW_SAROS).bot().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES, "bob_stf.*")
            .isContextMenuEnabled(CM_RENAME));
    }

    @Test
    public void clickContextsOfTreeItemInView() throws RemoteException {
        alice.superBot().views().sarosView()
            .connectWith(alice.getJID(), alice.getPassword());
        alice.remoteBot().view(VIEW_SAROS).bot().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES, "bob_stf.*")
            .contextMenus(CM_RENAME).click();
        assertTrue(alice.remoteBot().isShellOpen(SHELL_SET_NEW_NICKNAME));
    }

    @Test
    public void clickSubMenuOfContextsOfTreeItemInView() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);

        alice.remoteBot().view(VIEW_PACKAGE_EXPLORER).bot().tree()
            .selectTreeItem(PROJECT1).contextMenus(MENU_NEW, MENU_PACKAGE)
            .click();

        alice.remoteBot().waitUntilShellIsOpen(SHELL_NEW_JAVA_PACKAGE);
        assertTrue(alice.remoteBot().shell(SHELL_NEW_JAVA_PACKAGE).activate());
    }
}
