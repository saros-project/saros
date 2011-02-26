package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.basicWidgets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotView;
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
        alice.bot().menu(MENU_WINDOW).menu(MENU_SHOW_VIEW).menu(MENU_OTHER)
            .click();
        alice.bot().waitUntilShellIsOpen(SHELL_SHOW_VIEW);
        STFBotShell shell = alice.bot().shell(SHELL_SHOW_VIEW);
        shell.activate();
        assertTrue(shell.bot().tree().selectTreeItem(NODE_GENERAL)
            .existsSubItem(NODE_CONSOLE));

    }

    @Test
    public void existsTreeItemInShell2() throws RemoteException {
        alice.bot().menu(MENU_SAROS).menu(MENU_PREFERENCES).click();
        alice.bot().waitUntilShellIsOpen(SHELL_PREFERNCES);
        STFBotShell shell = alice.bot().shell(SHELL_PREFERNCES);
        shell.activate();
        assertTrue(shell.bot().tree()
            .selectTreeItem(NODE_GENERAL, NODE_EDITORS, NODE_TEXT_EDITORS)
            .existsSubItem(NODE_ANNOTATIONS));
    }

    @Test
    public void existsTreeItemWithRegexsInShell() throws RemoteException {
        alice.bot().menu(MENU_SAROS).menu(MENU_PREFERENCES).click();

        alice.bot().waitUntilShellIsOpen(SHELL_PREFERNCES);
        STFBotShell shell = alice.bot().shell(SHELL_PREFERNCES);
        shell.activate();
        assertTrue(shell.bot().tree()
            .selectTreeItem(NODE_GENERAL, NODE_EDITORS, NODE_TEXT_EDITORS)
            .existsSubItemWithRegex(NODE_ANNOTATIONS));
    }

    @Test
    public void existsTreeItemInView() throws RemoteException {
        alice.sarosBot().file().newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        STFBotView view = alice.bot().view(VIEW_PACKAGE_EXPLORER);
        view.show();
        assertTrue(view.bot().tree().existsSubItem(PROJECT1));

        assertTrue(alice.sarosBot().file().existsProjectNoGUI(PROJECT1));
        assertTrue(view.bot().tree().selectTreeItem(PROJECT1, SRC, PKG1)
            .existsSubItem(CLS1 + SUFFIX_JAVA));
        alice.sarosBot().file().existsClassNoGUI(PROJECT1, PKG1, CLS1);
    }

    @Test
    public void existsTreeItemWithRegexsInView() throws RemoteException {
        alice.sarosBot().file().newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        STFBotView view = alice.bot().view(VIEW_PACKAGE_EXPLORER);
        view.show();

        assertTrue(view.bot().tree()
            .existsSubItemWithRegexs(changeToRegex(PROJECT1)));
        assertTrue(alice.sarosBot().file().existsProjectNoGUI(PROJECT1));
        assertTrue(view.bot().tree().selectTreeItem(PROJECT1, SRC)
            .existsSubItemWithRegex(changeToRegex(PKG1)));

        assertTrue(alice.sarosBot().file().existsPkgNoGUI(PROJECT1, PKG1));
        assertTrue(view.bot().tree().selectTreeItem(PROJECT1, SRC, PKG1)
            .existsSubItemWithRegex(changeToRegex(CLS1)));

        assertTrue(alice.sarosBot().file()
            .existsClassNoGUI(PROJECT1, PKG1, CLS1));
        alice.sarosBot().file().newClass(PROJECT1, PKG1, CLS2);

        assertTrue(view.bot().tree().selectTreeItem(PROJECT1, SRC, PKG1)
            .existsSubItemWithRegex(changeToRegex(CLS2)));
        assertTrue(alice.sarosBot().file()
            .existsClassNoGUI(PROJECT1, PKG1, CLS2));
    }

    @Test
    public void existsTreeItemWithRegexsInView2() throws RemoteException {
        alice.sarosBot().buddiesView().connectNoGUI(alice.jid, alice.password);
        assertTrue(alice.bot().view(VIEW_SAROS_BUDDIES).bot().tree()
            .existsSubItemWithRegexs(changeToRegex(NODE_BUDDIES)));
        assertTrue(alice.bot().view(VIEW_SAROS_BUDDIES).bot().tree()
            .selectTreeItem(NODE_BUDDIES)
            .existsSubItemWithRegex(changeToRegex("bob_stf")));
    }

    @Test
    public void selectTreeItemInView() throws RemoteException {
        alice.sarosBot().file().newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.bot().view(VIEW_PACKAGE_EXPLORER).show();
        assertTrue(alice.bot().isEditorOpen(CLS1_SUFFIX));
        alice.bot().view(VIEW_PACKAGE_EXPLORER)
            .toolbarButtonWithRegex(TB_COLLAPSE_ALL).click();
        alice.bot().view(VIEW_PACKAGE_EXPLORER).bot().tree()
            .selectTreeItem(PROJECT1, SRC, PKG1, CLS1_SUFFIX);
        alice.bot().menu(MENU_FILE).menu(MENU_CLOSE).click();

        assertFalse(alice.bot().isEditorOpen(CLS1_SUFFIX));
    }

    @Test
    public void selectTreeItemInShell() throws RemoteException {
        alice.bot().menu(MENU_SAROS).menu(MENU_PREFERENCES).click();

        alice.bot().waitUntilShellIsOpen(SHELL_PREFERNCES);
        STFBotShell shell = alice.bot().shell(SHELL_PREFERNCES);
        shell.activate();
        shell.bot().tree().selectTreeItem(NODE_SAROS);
        assertTrue(shell
            .bot()
            .buttonInGroup(GeneralPreferencePage.CHANGE_BTN_TEXT,
                GeneralPreferencePage.ACCOUNT_GROUP_TITLE).isVisible());
    }

    @Test
    public void selectTreeItemWithRegexs() throws RemoteException {
        alice.sarosBot().file().newJavaProject(SVN_PROJECT_COPY);
        alice.sarosBot().shareProjectWithSVNUsingSpecifiedFolderName(
            VIEW_PACKAGE_EXPLORER, SVN_PROJECT_COPY, SVN_REPOSITORY_URL,
            SVN_PROJECT_PATH);

        STFBotView view = alice.bot().view(VIEW_PACKAGE_EXPLORER);
        view.show();
        view.bot()
            .tree()
            .selectTreeItemWithRegex(
                changeToRegex(getClassNodes(SVN_PROJECT_COPY, SVN_PKG, SVN_CLS1)));
    }

    @Test
    public void selectTreeItemWithRegexsInView() throws RemoteException {
        alice.sarosBot().file().newJavaProject(SVN_PROJECT_COPY);
        alice.sarosBot().shareProjectWithSVNUsingSpecifiedFolderName(
            VIEW_PACKAGE_EXPLORER, SVN_PROJECT_COPY, SVN_REPOSITORY_URL,
            SVN_PROJECT_PATH);
        alice.bot().view(VIEW_PACKAGE_EXPLORER).show();
        alice.sarosBot().packageExplorerView()
            .selectClass(SVN_PROJECT_COPY, SVN_PKG, SVN_CLS1)
            .contextMenu(CM_OPEN).click();

        alice.bot().editor(SVN_CLS1_SUFFIX).setTextWithoutSave(CP1);
        assertTrue(alice.bot().editor(SVN_CLS1_SUFFIX).isDirty());
        alice.bot().view(VIEW_PACKAGE_EXPLORER)
            .toolbarButtonWithRegex(TB_COLLAPSE_ALL + ".*").click();

        alice
            .bot()
            .view(VIEW_PACKAGE_EXPLORER)
            .bot()
            .tree()
            .selectTreeItemWithRegex(
                changeToRegex(getClassNodes(SVN_PROJECT_COPY, SVN_PKG, SVN_CLS1)));
        alice.bot().menu(MENU_FILE).menu(MENU_SAVE).click();
        assertFalse(alice.bot().editor(SVN_CLS1_SUFFIX).isDirty());
    }

    @Test
    public void existsContextOfTreeItemInView() throws RemoteException {
        alice.sarosBot().buddiesView().connectNoGUI(alice.jid, alice.password);
        assertTrue(alice.bot().view(VIEW_SAROS_BUDDIES).bot().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES, "bob_stf.*")
            .existsContextMenu(CM_RENAME));
    }

    @Test
    public void existsSubmenuOfContextOfTreeItemInView() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        String[] contextNames = { CM_SAROS, CM_SHARE_PROJECT };
        assertTrue(alice.bot().view(VIEW_PACKAGE_EXPLORER).bot().tree()
            .selectTreeItem(PROJECT1).existsContextMenu(contextNames));

    }

    @Test
    public void isContextOfTreeItemInViewEnabled() throws RemoteException {
        alice.sarosBot().buddiesView().connectNoGUI(alice.jid, alice.password);
        assertTrue(alice.bot().view(VIEW_SAROS_BUDDIES).bot().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES, "bob_stf.*")
            .isContextMenuEnabled(CM_RENAME));

        assertFalse(alice.bot().view(VIEW_SAROS_BUDDIES).bot().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES, "bob_stf.*")
            .isContextMenuEnabled(CM_INVITE_BUDDY));
    }

    @Test
    public void isSubmenuOfContextOfTreeItemInViewEnabled()
        throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        String[] contextNames1 = { CM_SAROS, CM_SHARE_PROJECT };
        String[] contextNames2 = { CM_SAROS, CM_ADD_TO_SESSION };
        assertTrue(alice.bot().view(VIEW_PACKAGE_EXPLORER).bot().tree()
            .selectTreeItem(PROJECT1).isContextMenuEnabled(contextNames1));

        assertFalse(alice.bot().view(VIEW_PACKAGE_EXPLORER).bot().tree()
            .selectTreeItem(PROJECT1).isContextMenuEnabled(contextNames2));
    }

    @Test
    public void clickContextsOfTreeItemInView() throws RemoteException {
        alice.sarosBot().buddiesView().connectNoGUI(alice.jid, alice.password);
        alice.bot().view(VIEW_SAROS_BUDDIES).bot().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES, "bob_stf.*")
            .contextMenu(CM_RENAME).click();
        assertTrue(alice.bot().isShellOpen(SHELL_SET_NEW_NICKNAME));
    }

    @Test
    public void clickSubMenuOfContextsOfTreeItemInView() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);

        alice.bot().view(VIEW_PACKAGE_EXPLORER).bot().tree()
            .selectTreeItem(PROJECT1).contextMenu(CM_SAROS, CM_SHARE_PROJECT)
            .click();

        alice.bot().waitUntilShellIsOpen(SHELL_INVITATION);
        assertTrue(alice.bot().shell(SHELL_INVITATION).activate());
    }
}
