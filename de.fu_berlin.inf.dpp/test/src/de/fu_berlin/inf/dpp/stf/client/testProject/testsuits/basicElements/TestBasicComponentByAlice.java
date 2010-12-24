package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.STFTest;
import de.fu_berlin.inf.dpp.ui.GeneralPreferencePage;

public class TestBasicComponentByAlice extends STFTest {
    private static final Logger log = Logger
        .getLogger(TestBasicComponentByAlice.class);

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbenchs();
    }

    @AfterClass
    public static void runAfterClass() {
        //
    }

    @Before
    public void runBeforeEveryTest() {
        //
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        deleteProjectsByActiveTesters();
    }

    @Test
    public void existsTreeItemInShell() throws RemoteException {
        alice.mainMenu.clickMenuWithTexts("Window", "Show View", "Other...");
        alice.shell.activateShellWaitingUntilOpened("Show View");
        assertTrue(alice.basic.existsTreeItemInTreeNode("Console", "General"));
    }

    @Test
    public void existsTreeItemInShell2() throws RemoteException {
        alice.mainMenu.clickMenuWithTexts("Saros", "Preferences");
        alice.shell.activateShellWaitingUntilOpened("Preferences");
        assertTrue(alice.basic.existsTreeItemInTreeNode("Annotations",
            "General", "Editors", "Text Editors"));
    }

    @Test
    public void existsTreeItemWithRegexsInShell() throws RemoteException {
        alice.mainMenu.clickMenuWithTexts("Saros", "Preferences");
        alice.shell.activateShellWaitingUntilOpened("Preferences");
        assertTrue(alice.basic.existsTreeItemWithRegexs("General", "Editors",
            "Text Editors", "Annotations"));
    }

    @Test
    public void existsTreeItemInView() throws RemoteException {
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.pEV.setFocusOnPEView();
        assertTrue(alice.basic.existsTreeItemInTreeInView("Package Explorer",
            PROJECT1));
        assertTrue(alice.pEV.existsProject(PROJECT1));
        assertTrue(alice.basic.existsTreeItemInTreeNodeInView(
            "Package Explorer", CLS1 + ".java", PROJECT1, SRC, PKG1));
        alice.pEV.existsClass(PROJECT1, PKG1, CLS1);
    }

    @Test
    public void existsTreeItemWithRegexsInView() throws RemoteException {
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.pEV.setFocusOnPEView();
        assertTrue(alice.basic.existsTreeItemWithRegexsInView(
            "Package Explorer", PROJECT1));
        assertTrue(alice.pEV.existsProject(PROJECT1));
        assertTrue(alice.basic.existsTreeItemWithRegexsInView(
            "Package Explorer", PROJECT1, SRC, PKG1));
        assertTrue(alice.pEV.existsPkg(PROJECT1, PKG1));
        assertTrue(alice.basic.existsTreeItemWithRegexsInView(
            "Package Explorer", PROJECT1, SRC, PKG1, CLS1 + ".*"));
        assertTrue(alice.pEV.existsClass(PROJECT1, PKG1, CLS1));
        alice.pEV.newClass(PROJECT1, PKG1, CLS2);
        assertTrue(alice.basic.existsTreeItemWithRegexsInView(
            "Package Explorer", PROJECT1, SRC, PKG1, CLS2 + ".*"));
        assertTrue(alice.pEV.existsClass(PROJECT1, PKG1, CLS2));
    }

    @Test
    public void existsTreeItemWithRegexsInView2() throws RemoteException {
        alice.rosterV.connect(alice.jid, alice.password);
        assertTrue(alice.basic.existsTreeItemWithRegexsInView("Roster",
            "Buddies"));
        assertTrue(alice.basic.existsTreeItemWithRegexsInView("Roster",
            "Buddies", "bob_stf.*"));
    }

    @Test
    public void selectTreeItemInView() throws RemoteException {
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.pEV.setFocusOnPEView();
        assertTrue(alice.editor.isJavaEditorOpen(CLS1));
        alice.basic.clickToolbarButtonWithRegexTooltipInView(
            "Package Explorer", "Collapse All");
        alice.basic.selectTreeItemInView("Package Explorer", PROJECT1, SRC,
            PKG1, CLS1 + ".java");
        alice.mainMenu.clickMenuWithTexts("File", "Close");
        assertFalse(alice.editor.isJavaEditorOpen(CLS1));
    }

    @Test
    public void selectTreeItemInShell() throws RemoteException {
        alice.mainMenu.clickMenuWithTexts("Saros", "Preferences");
        alice.shell.activateShellWaitingUntilOpened("Preferences");
        alice.basic.selectTreeItem("Saros");
        assertTrue(alice.basic.existsButtonInGroup(
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
        alice.pEV.newJavaProject(SVN_PROJECT_COPY);
        alice.pEV.shareProjectWithSVNUsingSpecifiedFolderName(SVN_PROJECT_COPY,
            SVN_REPOSITORY_URL, SVN_PROJECT_PATH);
        alice.pEV.setFocusOnPEView();
        alice.basic.selectTreeItemWithRegexs(changeToRegex(getClassNodes(
            SVN_PROJECT_COPY, "pkg", "Test")));
    }

    @Test
    public void selectTreeItemWithRegexsInView() throws RemoteException {
        alice.pEV.newJavaProject(SVN_PROJECT_COPY);
        alice.pEV.shareProjectWithSVNUsingSpecifiedFolderName(SVN_PROJECT_COPY,
            SVN_REPOSITORY_URL, SVN_PROJECT_PATH);
        alice.pEV.setFocusOnPEView();
        alice.pEV.openClass(SVN_PROJECT_COPY, "pkg", "Test");
        alice.editor.setTextInJavaEditorWithoutSave(CP1, SVN_PROJECT_COPY,
            "pkg", "Test");
        assertTrue(alice.editor.isClassDirty(SVN_PROJECT_COPY, "pkg", "Test",
            ID_JAVA_EDITOR));
        alice.basic.clickToolbarButtonWithRegexTooltipInView(
            "Package Explorer", "Collapse All");
        alice.basic.selectTreeItemWithRegexsInView("Package Explorer",
            changeToRegex(getClassNodes(SVN_PROJECT_COPY, "pkg", "Test")));
        alice.mainMenu.clickMenuWithTexts("File", "Save");
        assertFalse(alice.editor.isClassDirty(SVN_PROJECT_COPY, "pkg", "Test",
            ID_JAVA_EDITOR));
    }

    @Test
    public void existsContextOfTreeItemInView() throws RemoteException {
        alice.rosterV.connect(alice.jid, alice.password);
        assertTrue(alice.basic.existsContextOfTreeItemInView("Roster",
            "Rename...", "Buddies", "bob_stf.*"));
    }

    @Test
    public void existsSubmenuOfContextOfTreeItemInView() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        String[] contextNames = { "Saros", "Share project..." };
        assertTrue(alice.basic.existsSubmenuOfContextOfTreeItemInView(
            "Package Explorer", contextNames, PROJECT1));
    }

    @Test
    public void isContextOfTreeItemInViewEnabled() throws RemoteException {
        alice.rosterV.connect(alice.jid, alice.password);
        assertTrue(alice.basic.isContextOfTreeItemInViewEnabled("Roster",
            "Rename...", "Buddies", "bob_stf.*"));
        assertFalse(alice.basic.isContextOfTreeItemInViewEnabled("Roster",
            "Invite user...", "Buddies", "bob_stf.*"));
    }

    @Test
    public void isSubmenuOfContextOfTreeItemInViewEnabled()
        throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        String[] contextNames1 = { "Saros", "Share project..." };
        String[] contextNames2 = { "Saros", "Add to session (experimental)..." };
        assertTrue(alice.basic.isSubmenuOfContextOfTreeItemInViewEnabled(
            "Package Explorer", contextNames1, PROJECT1));
        assertFalse(alice.basic.isSubmenuOfContextOfTreeItemInViewEnabled(
            "Package Explorer", contextNames2, PROJECT1));
    }

    @Test
    public void clickContextsOfTreeItemInView() throws RemoteException {
        alice.rosterV.connect(alice.jid, alice.password);
        alice.basic.clickContextsOfTreeItemInView("Roster", "Rename...",
            "Buddies", "bob_stf.*");
        assertTrue(alice.shell.isShellOpen("Set new nickname"));
    }

    @Test
    public void clickSubMenuOfContextsOfTreeItemInView() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        String[] contextNames1 = { "Saros", "Share project..." };

        alice.basic.clickSubMenuOfContextsOfTreeItemInView("Package Explorer",
            contextNames1, PROJECT1);
        assertTrue(alice.shell.activateShellWaitingUntilOpened("Invitation"));
    }

    public String[] getClassNodes(String projectName, String pkg,
        String className) {
        String[] nodes = { projectName, SRC, pkg, className + ".java" };
        return nodes;
    }

    public String[] changeToRegex(String... texts) {
        String[] matchTexts = new String[texts.length];
        for (int i = 0; i < texts.length; i++) {
            matchTexts[i] = texts[i] + "( .*)?";
        }
        return matchTexts;
    }
}
