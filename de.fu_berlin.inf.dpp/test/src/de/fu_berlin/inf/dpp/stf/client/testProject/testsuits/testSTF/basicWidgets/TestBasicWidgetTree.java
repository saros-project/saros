package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.basicWidgets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;
import de.fu_berlin.inf.dpp.ui.GeneralPreferencePage;

public class TestBasicWidgetTree extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbenchs();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        deleteAllProjectsByActiveTesters();
    }

    @Test
    public void existsTreeItemInShell() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_WINDOW, MENU_SHOW_VIEW, MENU_OTHER);
        alice.shell.activateShellWithWaitingOpen(SHELL_SHOW_VIEW);
        assertTrue(alice.tree.existsSubItemInTreeItem(NODE_CONSOLE,
            NODE_GENERAL));
    }

    @Test
    public void existsTreeItemInShell2() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_PREFERENCES);
        alice.shell.activateShellWithWaitingOpen(SHELL_PREFERNCES);
        assertTrue(alice.tree.existsSubItemInTreeItem(NODE_ANNOTATIONS,
            NODE_GENERAL, NODE_EDITORS, NODE_TEXT_EDITORS));
    }

    @Test
    public void existsTreeItemWithRegexsInShell() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_PREFERENCES);
        alice.shell.activateShellWithWaitingOpen(SHELL_PREFERNCES);
        assertTrue(alice.tree.existsTreeItemWithRegexs(NODE_GENERAL,
            NODE_EDITORS, NODE_TEXT_EDITORS, NODE_ANNOTATIONS));
    }

    @Test
    public void existsTreeItemInView() throws RemoteException {
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.view.setFocusOnViewByTitle(VIEW_PACKAGE_EXPLORER);
        assertTrue(alice.tree.existsTreeItemInTreeInView(VIEW_PACKAGE_EXPLORER,
            PROJECT1));
        assertTrue(alice.fileM.existsProjectNoGUI(PROJECT1));
        assertTrue(alice.tree.existsSubItemInTreeItemInView(
            VIEW_PACKAGE_EXPLORER, CLS1 + SUFFIX_JAVA, PROJECT1, SRC, PKG1));
        alice.fileM.existsClassNoGUI(PROJECT1, PKG1, CLS1);
    }

    @Test
    public void existsTreeItemWithRegexsInView() throws RemoteException {
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.view.setFocusOnViewByTitle(VIEW_PACKAGE_EXPLORER);
        assertTrue(alice.tree.existsTreeItemWithRegexsInView(
            VIEW_PACKAGE_EXPLORER, PROJECT1));
        assertTrue(alice.fileM.existsProjectNoGUI(PROJECT1));
        assertTrue(alice.tree.existsTreeItemWithRegexsInView(
            VIEW_PACKAGE_EXPLORER, PROJECT1, SRC, PKG1));
        assertTrue(alice.fileM.existsPkgNoGUI(PROJECT1, PKG1));
        assertTrue(alice.tree.existsTreeItemWithRegexsInView(
            VIEW_PACKAGE_EXPLORER, PROJECT1, SRC, PKG1, CLS1 + ".*"));
        assertTrue(alice.fileM.existsClassNoGUI(PROJECT1, PKG1, CLS1));
        alice.fileM.newClass(PROJECT1, PKG1, CLS2);
        assertTrue(alice.tree.existsTreeItemWithRegexsInView(
            VIEW_PACKAGE_EXPLORER, PROJECT1, SRC, PKG1, CLS2 + ".*"));
        assertTrue(alice.fileM.existsClassNoGUI(PROJECT1, PKG1, CLS2));
    }

    @Test
    public void existsTreeItemWithRegexsInView2() throws RemoteException {
        alice.sarosBuddiesV.connectNoGUI(alice.jid, alice.password);
        assertTrue(alice.tree.existsTreeItemWithRegexsInView(
            VIEW_SAROS_BUDDIES, NODE_BUDDIES));
        assertTrue(alice.tree.existsTreeItemWithRegexsInView(
            VIEW_SAROS_BUDDIES, NODE_BUDDIES, "bob_stf.*"));
    }

    @Test
    public void selectTreeItemInView() throws RemoteException {
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.view.setFocusOnViewByTitle(VIEW_PACKAGE_EXPLORER);
        assertTrue(alice.editor.isJavaEditorOpen(CLS1));
        alice.toolbarButton.clickToolbarButtonWithRegexTooltipInView(
            VIEW_PACKAGE_EXPLORER, TB_COLLAPSE_ALL);
        alice.tree.selectTreeItemInView(VIEW_PACKAGE_EXPLORER, PROJECT1, SRC,
            PKG1, CLS1 + SUFFIX_JAVA);
        alice.menu.clickMenuWithTexts(MENU_FILE, MENU_CLOSE);
        assertFalse(alice.editor.isJavaEditorOpen(CLS1));
    }

    @Test
    public void selectTreeItemInShell() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_PREFERENCES);
        alice.shell.activateShellWithWaitingOpen(SHELL_PREFERNCES);
        alice.tree.selectTreeItem(NODE_SAROS);
        assertTrue(alice.button.existsButtonInGroup(
            GeneralPreferencePage.CHANGE_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE));
    }

    /**
     * To select a node with given regexs in a view, you should need to use the
     * method selectTreeItemWithRegexsInView, which use
     * bot.getViewByTitle("view title").bot().tree() instead of using
     * bot.tree().
     * 
     * @throws RemoteException
     */
    @Test(expected = WidgetNotFoundException.class)
    public void selectTreeItemWithRegexs() throws RemoteException {
        alice.fileM.newJavaProject(SVN_PROJECT_COPY);
        alice.team.shareProjectWithSVNUsingSpecifiedFolderName(
            VIEW_PACKAGE_EXPLORER, SVN_PROJECT_COPY, SVN_REPOSITORY_URL,
            SVN_PROJECT_PATH);
        alice.view.setFocusOnViewByTitle(VIEW_PACKAGE_EXPLORER);
        alice.tree.selectTreeItemWithRegexs(changeToRegex(getClassNodes(
            SVN_PROJECT_COPY, SVN_PKG, SVN_CLS1)));
    }

    @Test
    public void selectTreeItemWithRegexsInView() throws RemoteException {
        alice.fileM.newJavaProject(SVN_PROJECT_COPY);
        alice.team.shareProjectWithSVNUsingSpecifiedFolderName(
            VIEW_PACKAGE_EXPLORER, SVN_PROJECT_COPY, SVN_REPOSITORY_URL,
            SVN_PROJECT_PATH);
        alice.view.setFocusOnViewByTitle(VIEW_PACKAGE_EXPLORER);
        alice.openC.openClass(VIEW_PACKAGE_EXPLORER, SVN_PROJECT_COPY, SVN_PKG,
            SVN_CLS1);
        alice.editor.setTextInEditorWithoutSave(CP1, SVN_CLS1_SUFFIX);
        assertTrue(alice.editor.isClassDirty(SVN_PROJECT_COPY, SVN_PKG,
            SVN_CLS1, ID_JAVA_EDITOR));
        alice.toolbarButton.clickToolbarButtonWithRegexTooltipInView(
            VIEW_PACKAGE_EXPLORER, TB_COLLAPSE_ALL);
        alice.tree.selectTreeItemWithRegexsInView(VIEW_PACKAGE_EXPLORER,
            changeToRegex(getClassNodes(SVN_PROJECT_COPY, SVN_PKG, SVN_CLS1)));
        alice.menu.clickMenuWithTexts(MENU_FILE, MENU_SAVE);
        assertFalse(alice.editor.isClassDirty(SVN_PROJECT_COPY, SVN_PKG,
            SVN_CLS1, ID_JAVA_EDITOR));
    }

    @Test
    public void existsContextOfTreeItemInView() throws RemoteException {
        alice.sarosBuddiesV.connectNoGUI(alice.jid, alice.password);
        assertTrue(alice.tree.existsContextMenuOfTreeItemInView(
            VIEW_SAROS_BUDDIES, CM_RENAME, NODE_BUDDIES, "bob_stf.*"));
    }

    @Test
    public void existsSubmenuOfContextOfTreeItemInView() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        String[] contextNames = { CM_SAROS, CM_SHARE_PROJECT };
        assertTrue(alice.tree.existsContextMenusOfTreeItemInView(
            VIEW_PACKAGE_EXPLORER, contextNames, PROJECT1));
    }

    @Test
    public void isContextOfTreeItemInViewEnabled() throws RemoteException {
        alice.sarosBuddiesV.connectNoGUI(alice.jid, alice.password);
        assertTrue(alice.tree.isContextMenuOfTreeItemInViewEnabled(
            VIEW_SAROS_BUDDIES, CM_RENAME, NODE_BUDDIES, "bob_stf.*"));
        assertFalse(alice.tree.isContextMenuOfTreeItemInViewEnabled(
            VIEW_SAROS_BUDDIES, CM_INVITE_BUDDY, NODE_BUDDIES, "bob_stf.*"));
    }

    @Test
    public void isSubmenuOfContextOfTreeItemInViewEnabled()
        throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        String[] contextNames1 = { CM_SAROS, CM_SHARE_PROJECT };
        String[] contextNames2 = { CM_SAROS, CM_ADD_TO_SESSION };
        assertTrue(alice.tree.isContextMenusOfTreeItemInViewEnabled(
            VIEW_PACKAGE_EXPLORER, contextNames1, PROJECT1));
        assertFalse(alice.tree.isContextMenusOfTreeItemInViewEnabled(
            VIEW_PACKAGE_EXPLORER, contextNames2, PROJECT1));
    }

    @Test
    public void clickContextsOfTreeItemInView() throws RemoteException {
        alice.sarosBuddiesV.connectNoGUI(alice.jid, alice.password);
        alice.tree.clickContextMenuOfTreeItemInView(VIEW_SAROS_BUDDIES,
            CM_RENAME, NODE_BUDDIES, "bob_stf.*");
        assertTrue(alice.shell.isShellOpen(SHELL_SET_NEW_NICKNAME));
    }

    @Test
    public void clickSubMenuOfContextsOfTreeItemInView() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        String[] contextNames1 = { CM_SAROS, CM_SHARE_PROJECT };

        alice.tree.clickContextMenusOfTreeItemInView(VIEW_PACKAGE_EXPLORER,
            contextNames1, PROJECT1);
        assertTrue(alice.shell.activateShellWithWaitingOpen(SHELL_INVITATION));
    }

}
