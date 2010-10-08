package de.fu_berlin.inf.dpp.stf.swtbot;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

/**
 * This Interface is the stub for remote {@link SWTWorkbenchBot}. The
 * implementation is called {@link RmiSWTWorkbenchBot} and is implemented using
 * delegation. It has two parts, that are called "high-level" and "low-level".
 * The "low-level" part has simple methods like
 * {@link RmiSWTWorkbenchBot#clickButton()}. The "high-level" part has more
 * abstraction and encapsulates several method calls on the enclosing delegation
 * class {@link SWTWorkbenchBot}.
 */
public interface IRmiSWTWorkbenchBot extends Remote {

    /*********************** high-level RMI exported Methods *******************/

    /**
     * Convenient method for
     * {@link IRmiSWTWorkbenchBot#shoViewWithName(String, String, String)} to
     * add the View called "Package Explorer".
     */
    public void showViewPackageExplorer() throws RemoteException;

    /**
     * Convenient method for
     * {@link IRmiSWTWorkbenchBot#openViewWithName(String, String, String)} to
     * add the View called "Problems".
     */
    public void showViewProblems() throws RemoteException;

    /**
     * Convenient method for
     * {@link IRmiSWTWorkbenchBot#openViewWithName(String, String, String)} to
     * add the View called "Project Explorer".
     */
    public void showViewProjectExplorer() throws RemoteException;

    /**
     * Add a View using Window->Show View->Other...
     * 
     * @param category
     *            example: "General"
     * @param nodeName
     *            example: "Console"
     */
    // public void openViewWithName(String viewTitle, String category,
    // String nodeName) throws RemoteException;

    /**
     * Clicks {@link SWTBotButton} with the given buttonText in a pop up window
     * ({@link SWTBotShell}) with given title.
     */
    public void confirmWindow(String title, String buttonText)
        throws RemoteException;

    // public boolean isTreeItemInViewExist(String viewName, String... labels)
    // throws RemoteException;
    //
    // public boolean isTreeItemInWindowExist(String title, String... labels)
    // throws RemoteException;

    // public boolean isTreeItemExist(SWTBotTree tree, String... labels)
    // throws RemoteException;

    // public boolean istableItemInViewExist(String viewName, String itemName)
    // throws RemoteException;

    // public boolean isTableItemInWindowExist(String title, String label)
    // throws RemoteException;

    // public boolean isTableItemExist(SWTBotTable table, String itemName)
    // throws RemoteException;

    // /**
    // * Returns true if the given projectName was found on the {@link
    // SWTBotTree}
    // * on the {@link SWTBotView} with the given viewTitle.
    // */
    // public boolean isProjectInWorkspace(String viewTitle, String projectName)
    // throws RemoteException;

    // /**
    // * Convenient method for
    // * {@link RmiSWTWorkbenchBot#isProjectInWorkspace(String, String)}. It
    // * checks the {@link SWTBotView} with title "Package Explorer" for the
    // given
    // * projectName.
    // */
    // public boolean isProjectInWorkspacePackageExplorer(String projectName)
    // throws RemoteException;

    // /**
    // * Convenient method for
    // * {@link RmiSWTWorkbenchBot#isProjectInWorkspace(String, String)}. It
    // * checks the {@link SWTBotView} with title "Project Explorer" for the
    // given
    // * projectName.
    // */
    // public boolean isProjectInWorkspaceProjectExplorer(String projectName)
    // throws RemoteException;

    /**
     * Adds a new Java class with the given name in given project inside given
     * package.
     */
    public void newClass(String project, String pkg, String name)
        throws RemoteException;

    /**
     * Adds a new Java Project with a random name with prefix
     * 
     * <pre>
     * &quot;Foo-&quot;
     * </pre>
     */
    // public void newJavaProject() throws RemoteException;

    /**
     * Adds a new Java Project with the given name.
     */
    public void newJavaProject(String name) throws RemoteException;

    /*********************** low-level RMI exported Methods *******************/

    /*
     * active
     */
    /**
     * This is the same as:
     * 
     * <pre>
     * {@link SWTWorkbenchBot#activeShell()};
     * </pre>
     */
    // public String getCurrentActiveShell() throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTWorkbenchBot#shell(String)}.activate();
     * </pre>
     */
    // public boolean activateShellWithText(String text) throws RemoteException;

    public boolean activateShellWithMatchText(String matchText)
        throws RemoteException;

    /**
     * Clicks on a {@link SWTBotButton} with the specified none. This is the
     * same as the following method chain::
     * 
     * <pre>
     * {@link SWTWorkbenchBot#button()}.click();
     * </pre>
     */
    // public void clickButton() throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotButton} button = {@link SWTWorkbenchBot#button(int)};
     * button.click();
     * </pre>
     */
    // public void clickButton(int num) throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotButton} button = {@link SWTWorkbenchBot#button(String)};
     * button.click();
     * </pre>
     */
    // public void clickButton(String name) throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotMenu} menu = {@link SWTWorkbenchBot#menu(String)};
     * menu.click();
     * </pre>
     */
    // public void clickMenuWithText(String text) throws RemoteException;

    /**
     * Traverse a list of {@link SWTBotMenu} identified by the given list of
     * Strings. The last menu will be clicked. Use this method if you would use
     * the fluent interface on the local way of using SWTBot. Example:
     * 
     * <pre>
     * String s1 = "Menu1", s2 = "Menu2", s3 = "Menu3";
     * {@link SWTWorkbenchBot}.menu(s1).menu(s2).menu(s3).click();
     * </pre>
     */
    // public void clickMenuWithTexts(String... names) throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotView} view = {@link SWTWorkbenchBot}.viewByTitle(title);
     * if (view != null) {
     *     for (SWTBotToolbarButton button : view.getToolbarButtons()) {
     *         if (button.getText().matches(buttonText)) {
     *             button.click();
     *             return;
     *         }
     *     }
     * }
     * </pre>
     * 
     * @param title
     *            The title of the {@link SWTBotView}, where the
     *            {@link SWTBotToolbarButton} sits on.
     * @param buttonText
     *            A {@link String} that matches the text of the buttonText
     *            identifying the {@link SWTBotToolbarButton} that should be
     *            clicked.
     * @throws RemoteException
     *             if no matching {@link SWTBotToolbarButton} was found.
     */
    // public void clickToolbarButtonWithTextInViewWithTitle(String title,
    // String buttonText) throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotView} view = {@link SWTWorkbenchBot}.viewByTitle(title);
     * if (view != null) {         
     *   for ({@link SWTBotToolbarButton} button : view.getToolbarButtons()) {
     *     if (button.getToolTipText().matches(buttonTooltip)) {
     *       button.click();
     *       return;
     *     }
     *   }
     * }
     * </pre>
     * 
     * @throws RemoteException
     *             if no matching {@link SWTBotToolbarButton} was found.
     */
    // public SWTBotToolbarButton clickToolbarButtonWithTooltipInView(
    // String title, String buttonTooltip) throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotView} view = {@link SWTWorkbenchBot#viewByTitle(String)};
     * view.close();
     * </pre>
     */
    // public void closeViewWithText(String title) throws RemoteException;

    /**
     * Returns a list of {@link String} from {@link SWTBotView} titles got by:
     * 
     * <pre>
     * {@link SWTWorkbenchBot#views()};
     * </pre>
     */
    // public List<String> getViewTitles() throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotButton} button = {@link SWTWorkbenchBot#button(int)};
     * button.isEnabled();
     * </pre>
     */
    // public boolean isButtonEnabled(int num) throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotButton} button = {@link SWTWorkbenchBot#button(String)};
     * button.isEnabled();
     * </pre>
     */
    // public boolean isButtonEnabled(String name) throws RemoteException;

    /**
     * Returns true a {@link SWTBotShell} with the given title is open.
     */
    public boolean isShellActive(String title) throws RemoteException;

    /**
     * Returns true if a {@link SWTBotView} with the given title is visible.
     */
    // public boolean isViewOpen(String title) throws RemoteException;

    // public boolean isViewActive(String title) throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotView} view = {@link SWTWorkbenchBot#viewByTitle(String)};
     * view.setFocus();
     * </pre>
     */
    // public void activateViewWithTitle(String title) throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotText} text = {@link SWTWorkbenchBot#textWithLabel(String)};
     * text.setText(String);
     * </pre>
     */
    // public void setTextWithLabel(String label, String text)
    // throws RemoteException;

    /**
     * This is the same as calling the following method chain:
     * 
     * <pre>
     * {@link SWTBotText} text = {@link SWTWorkbenchBot#text(String)};
     * text.setText(String);
     * </pre>
     */
    // public void setTextWithoutLabel(String match, String replace)
    // throws RemoteException;

    /**
     * This is the same as calling the following method:
     * 
     * <pre>
     * {@link SWTWorkbenchBot#sleep(long)}
     * </pre>
     */
    public void sleep(long millis) throws RemoteException;

    // /**
    // * It waits as long a shell with given title was found. This method is
    // * needed for synchronization purposes.
    // */
    // public void waitOnShellByTitle(String title) throws RemoteException;

    /**
     * This is the same as calling the following method:
     * 
     * <pre>
     * {@link SWTWorkbenchBot#captureScreenshot(String)}
     * </pre>
     */
    public void captureScreenshot(String filename) throws RemoteException;

    // public boolean isPerspectiveActive(String title) throws RemoteException;

    public void waitUntilShellCloses(String shellText) throws RemoteException;

    // public void openPerspectiveWithName(String nodeName) throws
    // RemoteException;

    // public void setTextInJavaEditor(String contents, String projectName,
    // String packageName, String className) throws RemoteException;

    public String getTextOfJavaEditor(String projectName, String packageName,
        String className) throws RemoteException;

    public void openClass(String projectName, String packageName,
        String className) throws RemoteException;

    // public void openFileWithEditorInView(String viewName, String... names)
    // throws RemoteException;

    public boolean isJavaEditorActive(String className) throws RemoteException;

    // public boolean isEditorActive(String className) throws RemoteException;

    // public void activeJavaEditor(String className) throws RemoteException;
    // public void activateEditor(String name) throws RemoteException;

    // public void clickCheckBox(String title) throws RemoteException;

    // public void showWindowUntilClosed(String title) throws RemoteException;

    public boolean isTextWithLabelEqualWithText(String label, String text)
        throws RemoteException;

    // public void clickContextMenuOfTableInView(String viewName, String
    // itemName,
    // String contextName) throws RemoteException;

    // public SWTBotToolbarButton getToolbarButtonWithTooltipInView(String
    // title,
    // String buttonTooltip) throws RemoteException;

    public void confirmWindowWithTable(String itemName, String shellName,
        String buttonName) throws RemoteException;

    public void confirmWindowWithTreeWithFilterText(String shellName,
        String category, String itemName, String buttonName)
        throws RemoteException;

    // public SWTBotView getViewWithText(String title) throws RemoteException;

    // public void clickCheckBox() throws RemoteException;

    // public void clickContextMenuOfTreeInView(String viewName,
    // String contextName, String... itemNames) throws RemoteException;

    /*
     * waitUntil
     */
    public void waitUntilShellCloses(SWTBotShell shell) throws RemoteException;

    public void waitUntilTableHasRows(int row) throws RemoteException;

    public void waitUntilButtonEnabled(String mnemonicText)
        throws RemoteException;

    public void waitUntilShellActive(String title) throws RemoteException;

    public void waitUntilTableItemExisted(SWTBotTable table,
        String tableItemName) throws RemoteException;

    public void waitUnitButtonWithTooltipTextEnabled(String tooltipText)
        throws RemoteException;

    // public void clickRadio(String title) throws RemoteException;

    // public boolean isContextMenuOfTableItemInViewExist(String viewName,
    // String itemName, String contextName) throws RemoteException;

    public void importProjectFromSVN(String path) throws RemoteException;

    public void waitUntilContextMenuOfTableItemEnabled(
        SWTBotTableItem tableItem, String context) throws RemoteException;

    /*
     * select tree
     */
    /**
     * This is the same as the following method:
     * 
     * <pre>
     * {@link SWTBotTree} tree = {@link SWTWorkbenchBot#treeWithLabel(String)};
     * </pre>
     */
    // public SWTBotTree selectTreeWithLabel(String name) throws
    // RemoteException;

    // public SWTBotTreeItem selectTreeWithLabelsInView(String viewTitle,
    // String... itemNames) throws RemoteException;

    // public SWTBotShell getShellWithText(String text) throws RemoteException;

    // public SWTBotTableItem selectTableItemWithLabelInView(String viewTitle,
    // String label) throws RemoteException;

    // public void waitUntilEditorActive(String name) throws RemoteException;

    // public void selectCheckBoxInTable(String invitee) throws RemoteException;

    // public void selectCheckBoxsInTable(List<String> invitees)
    // throws RemoteException;

    public void confirmWindowWithCheckBox(String shellName, String buttonName,
        String... itemNames) throws RemoteException;

    // public void clickToolbarPushButtonWithTooltipInView(String viewTitle,
    // String tooltip) throws RemoteException;

    public void confirmWindowWithTree(String shellName, String buttonName,
        String... itemNames) throws RemoteException;

    // public boolean isPerspectiveOpen(String title) throws RemoteException;

    public boolean isShellOpen(String title) throws RemoteException;

    // public boolean isEditorOpen(String name) throws RemoteException;

    // public void setTextinEditor(String contents, String fileName)
    // throws RemoteException;

    // public boolean isTreeItemOfTreeExisted(SWTBotTree tree, String label)
    // throws RemoteException;

    public void waitUntilFileEqualWithFile(String projectName,
        String packageName, String className, String file)
        throws RemoteException;

    // public boolean isTreeItemExist(String viewTitle, String... paths)
    // throws RemoteException;

    // public SWTBotTreeItem getTreeWithLabels(SWTBotTree tree, String...
    // labels)
    // throws RemoteException;

    public void selectLineInJavaEditor(int line, String fileName)
        throws RemoteException;

    // public void selectLineInEditor(int line, String fileName)
    // throws RemoteException;

    // public SWTBotTreeItem selectTreeWithLabels(SWTBotTree tree,
    // String... labels) throws RemoteException;

    public void waitUntilTreeExisted(SWTBotTree tree, String nodeName)
        throws RemoteException;

    public void setBreakPoint(int line, String projectName, String packageName,
        String className) throws RemoteException;

    public void waitUntilJavaEditorActive(String className)
        throws RemoteException;

    public void activateJavaEditor(String className) throws RemoteException;

    // public void debugJavaFile(String projectName, String packageName,
    // String className) throws RemoteException;

    public int getJavaCursorLinePosition(String projectName,
        String packageName, String className) throws RemoteException;

    public String getJavaTextOnLine(String projectName, String packageName,
        String className, int line) throws RemoteException;

    public SWTBotEclipseEditor getJavaEditor(String className)
        throws RemoteException;

    public RGB getJavaLineBackground(String projectName, String packageName,
        String className, int line) throws RemoteException;

    public void openPerspectiveJava() throws RemoteException;

    public void openPerspectiveDebug() throws RemoteException;

    public void activatePackageExplorerView() throws RemoteException;

    public boolean isJavaProjectExist(String projectName)
        throws RemoteException;

    public boolean isClassExist(String projectName, String pkg, String className)
        throws RemoteException;

    public boolean isInSVN() throws RemoteException;

    public String getRevision(String fullPath) throws RemoteException;

    public String getURLOfRemoteResource(String fullPath)
        throws RemoteException;

    public void switchToTag() throws RemoteException;

    // public SWTBotTreeItem getTreeItemWithMatchText(SWTBotTree tree,
    // String... regexs) throws RemoteException;

    // public boolean isTreeItemWithMatchTextExist(SWTBotTree tree,
    // String... regexs) throws RemoteException;

    // public boolean isMenusOfContextMenuOfTreeItemInViewExist(String
    // viewTitle,
    // String[] matchTexts, String... contexts) throws RemoteException;

    // public void clickMenusOfContextMenuOfTreeItemInView(String viewName,
    // String[] matchTexts, String... contexts) throws RemoteException;

    public void disConnectSVN() throws RemoteException;

    public void waitUntilProjectInSVN(String projectName)
        throws RemoteException;

    public void waitUntilProjectNotInSVN(String projectName)
        throws RemoteException;

    public void connectSVN() throws RemoteException;

    public void switchToOtherRevision() throws RemoteException;

    public void switchToOtherRevision(String CLS_PATH) throws RemoteException;

    public void deleteProject(String fullPath) throws RemoteException;

    public boolean isClassExistGUI(String... matchTexts) throws RemoteException;

    public void revert() throws RemoteException;

    public boolean isResourceExist(String fullPath) throws RemoteException;

    // public List<String> getAllProjects() throws RemoteException;

    public void deleteProjectGui(String projectName) throws RemoteException;

    public void deleteFileGui(String... labels) throws RemoteException;

    public void deleteClass(String projectName, String pkg, String className)
        throws RemoteException;

    // public List<String> getPerspectiveTitles() throws RemoteException;

    // public List<String> getEditorTitles() throws RemoteException;

    public void newJavaProjectWithClass(String projectName, String pkg,
        String className) throws RemoteException;

    public void renameFile(String newName, String... texts)
        throws RemoteException;

    public void renameClass(String newName, String projectName, String pkg,
        String className) throws RemoteException;

    public void waitUntilClassExist(String projectName, String pkg,
        String className) throws RemoteException;

    public void waitUntilClassNotExist(String projectName, String pkg,
        String className) throws RemoteException;

    public boolean isPkgExist(String projectName, String pkg)
        throws RemoteException;

    public void newPackage(String projectName, String pkg)
        throws RemoteException;

    public void waitUntilPkgExist(String projectName, String pkg)
        throws RemoteException;

    public void waitUntilPkgNotExist(String projectName, String pkg)
        throws RemoteException;

    public void deletePkg(String projectName, String pkg)
        throws RemoteException;

    public void moveClassTo(String projectName, String pkg, String className,
        String targetProject, String targetPkg) throws RemoteException;

    // public InputStream getContentOfClass(String projectName, String pkg,
    // String className) throws RemoteException;
    //
    // public boolean isTwoClassSame(String projectName1, String pkg1,
    // String className1, String projectName2, String pkg2, String className2)
    // throws RemoteException, IOException;

    public SWTBotShell getEclipseShell() throws RemoteException;

    public void resetWorkbench() throws RemoteException;

    public void renamePkg(String newName, String... texts)
        throws RemoteException;

    public void activateEclipseShell() throws RemoteException;

    public void closeWelcomeView() throws RemoteException;

    public void closePackageExplorerView() throws RemoteException;

    public void deleteAllProjects() throws RemoteException;

    public void closeShell(String title) throws RemoteException;

    public boolean isJavaPerspectiveActive() throws RemoteException;

    public boolean isDebugPerspectiveActive() throws RemoteException;

    public void newClassImplementsRunnable(String projectName, String pkg,
        String className) throws RemoteException;
}
