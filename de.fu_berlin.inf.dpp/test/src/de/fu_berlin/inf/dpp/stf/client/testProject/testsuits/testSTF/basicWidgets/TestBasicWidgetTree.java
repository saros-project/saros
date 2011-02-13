package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.basicWidgets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.Shell;
import de.fu_berlin.inf.dpp.ui.GeneralPreferencePage;

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
        alice.menu.clickMenuWithTexts(MENU_WINDOW, MENU_SHOW_VIEW, MENU_OTHER);
        Shell shell = alice.bot.shell(SHELL_SHOW_VIEW);
        shell.activateAndWait();
        assertTrue(shell.bot_().tree().selectTreeItem(NODE_GENERAL)
            .existsSubItem(NODE_CONSOLE));

    }

    @Test
    public void existsTreeItemInShell2() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_PREFERENCES);
        Shell shell = alice.bot().shell(SHELL_PREFERNCES);
        shell.activateAndWait();
        assertTrue(shell.bot_().tree()
            .selectTreeItem(NODE_GENERAL, NODE_EDITORS, NODE_TEXT_EDITORS)
            .existsSubItem(NODE_ANNOTATIONS));
    }

    @Test
    public void existsTreeItemWithRegexsInShell() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_PREFERENCES);
        Shell shell = alice.bot().shell(SHELL_PREFERNCES);
        shell.activateAndWait();
        assertTrue(shell.bot_().tree()
            .selectTreeItem(NODE_GENERAL, NODE_EDITORS, NODE_TEXT_EDITORS)
            .existsSubItemWithRegex(NODE_ANNOTATIONS));
    }

    @Test
    public void existsTreeItemInView() throws RemoteException {
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        STFView view = alice.bot().view(VIEW_PACKAGE_EXPLORER);
        view.setFocus();
        assertTrue(view.bot_().tree().existsSubItem(PROJECT1));

        assertTrue(alice.fileM.existsProjectNoGUI(PROJECT1));
        assertTrue(view.bot_().tree().selectTreeItem(PROJECT1, SRC, PKG1)
            .existsSubItem(CLS1 + SUFFIX_JAVA));
        alice.fileM.existsClassNoGUI(PROJECT1, PKG1, CLS1);
    }

    @Test
    public void existsTreeItemWithRegexsInView() throws RemoteException {
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        STFView view = alice.bot().view(VIEW_PACKAGE_EXPLORER);
        view.setFocus();

        assertTrue(view.bot_().tree()
            .existsSubItemWithRegexs(changeToRegex(PROJECT1)));
        assertTrue(alice.fileM.existsProjectNoGUI(PROJECT1));
        assertTrue(view.bot_().tree().selectTreeItem(PROJECT1, SRC)
            .existsSubItemWithRegex(changeToRegex(PKG1)));

        assertTrue(alice.fileM.existsPkgNoGUI(PROJECT1, PKG1));
        assertTrue(view.bot_().tree().selectTreeItem(PROJECT1, SRC, PKG1)
            .existsSubItemWithRegex(changeToRegex(CLS1)));

        assertTrue(alice.fileM.existsClassNoGUI(PROJECT1, PKG1, CLS1));
        alice.fileM.newClass(PROJECT1, PKG1, CLS2);

        assertTrue(view.bot_().tree().selectTreeItem(PROJECT1, SRC, PKG1)
            .existsSubItemWithRegex(changeToRegex(CLS2)));
        assertTrue(alice.fileM.existsClassNoGUI(PROJECT1, PKG1, CLS2));
    }

    @Test
    public void existsTreeItemWithRegexsInView2() throws RemoteException {
        alice.sarosBuddiesV.connectNoGUI(alice.jid, alice.password);
        assertTrue(alice.bot().view(VIEW_SAROS_BUDDIES).bot_().tree()
            .existsSubItemWithRegexs(changeToRegex(NODE_BUDDIES)));
        assertTrue(alice.bot().view(VIEW_SAROS_BUDDIES).bot_().tree()
            .selectTreeItem(NODE_BUDDIES)
            .existsSubItemWithRegex(changeToRegex("bob_stf")));
    }

    @Test
    public void selectTreeItemInView() throws RemoteException {
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.bot().view(VIEW_PACKAGE_EXPLORER).setFocus();
        assertTrue(alice.editor.isJavaEditorOpen(CLS1));
        alice.toolbarButton.clickToolbarButtonWithRegexTooltipOnView(
            VIEW_PACKAGE_EXPLORER, TB_COLLAPSE_ALL);
        alice.bot().view(VIEW_PACKAGE_EXPLORER).bot_().tree()
            .selectTreeItem(PROJECT1, SRC, PKG1, CLS1 + SUFFIX_JAVA);
        alice.menu.clickMenuWithTexts(MENU_FILE, MENU_CLOSE);
        assertFalse(alice.editor.isJavaEditorOpen(CLS1));
    }

    @Test
    public void selectTreeItemInShell() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_PREFERENCES);
        Shell shell = alice.bot.shell(SHELL_PREFERNCES);
        shell.activateAndWait();
        shell.bot_().tree().selectTreeItem(NODE_SAROS);
        assertTrue(shell
            .bot_()
            .buttonInGroup(GeneralPreferencePage.CHANGE_BTN_TEXT,
                GeneralPreferencePage.ACCOUNT_GROUP_TITLE).isVisible());
    }

    @Test
    public void selectTreeItemWithRegexs() throws RemoteException {
        alice.fileM.newJavaProject(SVN_PROJECT_COPY);
        alice.team.shareProjectWithSVNUsingSpecifiedFolderName(
            VIEW_PACKAGE_EXPLORER, SVN_PROJECT_COPY, SVN_REPOSITORY_URL,
            SVN_PROJECT_PATH);

        STFView view = alice.bot().view(VIEW_PACKAGE_EXPLORER);
        view.setFocus();
        view.bot_()
            .tree()
            .selectTreeItemWithRegex(
                changeToRegex(getClassNodes(SVN_PROJECT_COPY, SVN_PKG, SVN_CLS1)));
    }

    @Test
    public void selectTreeItemWithRegexsInView() throws RemoteException {
        alice.fileM.newJavaProject(SVN_PROJECT_COPY);
        alice.team.shareProjectWithSVNUsingSpecifiedFolderName(
            VIEW_PACKAGE_EXPLORER, SVN_PROJECT_COPY, SVN_REPOSITORY_URL,
            SVN_PROJECT_PATH);
        alice.bot().view(VIEW_PACKAGE_EXPLORER).setFocus();
        alice.openC.openClass(VIEW_PACKAGE_EXPLORER, SVN_PROJECT_COPY, SVN_PKG,
            SVN_CLS1);
        alice.editor.setTextInEditorWithoutSave(CP1, SVN_CLS1_SUFFIX);
        assertTrue(alice.editor.isClassDirty(SVN_PROJECT_COPY, SVN_PKG,
            SVN_CLS1, ID_JAVA_EDITOR));
        alice.toolbarButton.clickToolbarButtonWithRegexTooltipOnView(
            VIEW_PACKAGE_EXPLORER, TB_COLLAPSE_ALL);

        alice
            .bot()
            .view(VIEW_PACKAGE_EXPLORER)
            .bot_()
            .tree()
            .selectTreeItemWithRegex(
                changeToRegex(getClassNodes(SVN_PROJECT_COPY, SVN_PKG, SVN_CLS1)));
        alice.menu.clickMenuWithTexts(MENU_FILE, MENU_SAVE);
        assertFalse(alice.editor.isClassDirty(SVN_PROJECT_COPY, SVN_PKG,
            SVN_CLS1, ID_JAVA_EDITOR));
    }

    @Test
    public void existsContextOfTreeItemInView() throws RemoteException {
        alice.sarosBuddiesV.connectNoGUI(alice.jid, alice.password);
        assertTrue(alice.bot().view(VIEW_SAROS_BUDDIES).bot_().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES, "bob_stf.*")
            .existsContextMenu(CM_RENAME));
    }

    @Test
    public void existsSubmenuOfContextOfTreeItemInView() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        String[] contextNames = { CM_SAROS, CM_SHARE_PROJECT };
        assertTrue(alice.bot().view(VIEW_PACKAGE_EXPLORER).bot_().tree()
            .selectTreeItem(PROJECT1).existsContextMenu(contextNames));

    }

    @Test
    public void isContextOfTreeItemInViewEnabled() throws RemoteException {
        alice.sarosBuddiesV.connectNoGUI(alice.jid, alice.password);
        assertTrue(alice.bot().view(VIEW_SAROS_BUDDIES).bot_().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES, "bob_stf.*")
            .isContextMenuEnabled(CM_RENAME));

        assertFalse(alice.bot().view(VIEW_SAROS_BUDDIES).bot_().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES, "bob_stf.*")
            .isContextMenuEnabled(CM_INVITE_BUDDY));
    }

    @Test
    public void isSubmenuOfContextOfTreeItemInViewEnabled()
        throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        String[] contextNames1 = { CM_SAROS, CM_SHARE_PROJECT };
        String[] contextNames2 = { CM_SAROS, CM_ADD_TO_SESSION };
        assertTrue(alice.bot().view(VIEW_PACKAGE_EXPLORER).bot_().tree()
            .selectTreeItem(PROJECT1).isContextMenuEnabled(contextNames1));

        assertFalse(alice.bot().view(VIEW_PACKAGE_EXPLORER).bot_().tree()
            .selectTreeItem(PROJECT1).isContextMenuEnabled(contextNames2));
    }

    @Test
    public void clickContextsOfTreeItemInView() throws RemoteException {
        alice.sarosBuddiesV.connectNoGUI(alice.jid, alice.password);
        alice.bot().view(VIEW_SAROS_BUDDIES).bot_().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES, "bob_stf.*")
            .contextMenu(CM_RENAME).click();
        assertTrue(alice.bot().isShellOpen(SHELL_SET_NEW_NICKNAME));
    }

    @Test
    public void clickSubMenuOfContextsOfTreeItemInView() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);

        alice.bot().view(VIEW_PACKAGE_EXPLORER).bot_().tree()
            .selectTreeItem(PROJECT1).contextMenu(CM_SAROS, CM_SHARE_PROJECT)
            .click();

        assertTrue(alice.bot().shell(SHELL_INVITATION).activateAndWait());
    }
}
