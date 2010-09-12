package de.fu_berlin.inf.dpp.stf.swtbot;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

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
     * Removes and deletes a project identified by it's name on the View called
     * "Package Explorer".
     */
    public void removeProject(String projectName) throws RemoteException;

    /**
     * Convenient method for
     * {@link IRmiSWTWorkbenchBot#openViewByName(String, String)} to add the
     * View called "Package Explorer".
     */
    public void openJavaPackageExplorerView() throws RemoteException;

    /**
     * Convenient method for
     * {@link IRmiSWTWorkbenchBot#openViewByName(String, String)} to add the
     * View called "Problems".
     */
    public void openProblemsView() throws RemoteException;

    /**
     * Convenient method for
     * {@link IRmiSWTWorkbenchBot#openViewByName(String, String)} to add the
     * View called "Project Explorer".
     */
    public void openProjectExplorerView() throws RemoteException;

    /**
     * Add a View using Window->Show View->Other...
     * 
     * @param category
     *            example: "General"
     * @param nodeName
     *            example: "Console"
     */
    public void openViewByName(String category, String nodeName)
        throws RemoteException;

    /**
     * Clicks {@link SWTBotButton} with the given buttonText in a pop up window
     * ({@link SWTBotShell}) with given title.
     */
    public void confirmWindow(String title, String buttonText)
        throws RemoteException;

    /**
     * Returns true if the given projectName was found on the {@link SWTBotTree}
     * on the {@link SWTBotView} with the given viewTitle.
     */
    public boolean isProjectInWorkspace(String viewTitle, String projectName)
        throws RemoteException;

    /**
     * Convenient method for
     * {@link RmiSWTWorkbenchBot#isProjectInWorkspace(String, String)}. It
     * checks the {@link SWTBotView} with title "Package Explorer" for the given
     * projectName.
     */
    public boolean isProjectInWorkspacePackageExplorer(String projectName)
        throws RemoteException;

    /**
     * Convenient method for
     * {@link RmiSWTWorkbenchBot#isProjectInWorkspace(String, String)}. It
     * checks the {@link SWTBotView} with title "Project Explorer" for the given
     * projectName.
     */
    public boolean isProjectInWorkspaceProjectExplorer(String projectName)
        throws RemoteException;

    /**
     * Adds a new Java class with the given name in given project inside given
     * package.
     */
    public void newJavaClass(String project, String pkg, String name)
        throws RemoteException;

    /**
     * Adds a new Java Project with a random name with prefix
     * 
     * <pre>
     * &quot;Foo-&quot;
     * </pre>
     */
    public void newJavaProject() throws RemoteException;

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
    public SWTBotShell getCurrentActiveShell() throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTWorkbenchBot#shell(String)}.activate();
     * </pre>
     */
    public SWTBotShell activateShellWithText(String text)
        throws RemoteException;

    /**
     * Clicks on a {@link SWTBotButton} with the specified none. This is the
     * same as the following method chain::
     * 
     * <pre>
     * {@link SWTWorkbenchBot#button()}.click();
     * </pre>
     */
    public void clickButton() throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotButton} button = {@link SWTWorkbenchBot#button(String,int)};
     * button.click();
     * </pre>
     */
    public void clickButton(String mnemonicText, int index)
        throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotButton} button = {@link SWTWorkbenchBot#button(int)};
     * button.click();
     * </pre>
     */
    public void clickButton(int num) throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotButton} button = {@link SWTWorkbenchBot#button(String)};
     * button.click();
     * </pre>
     */
    public void clickButton(String name) throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotMenu} menu = {@link SWTWorkbenchBot#menu(String)};
     * menu.click();
     * </pre>
     */
    public void clickMenuWithText(String text) throws RemoteException;

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
    public void clickMenuWithTexts(String... names) throws RemoteException;

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
    public void clickToolbarButtonWithTextInViewWithTitle(String title,
        String buttonText) throws RemoteException;

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
    public SWTBotToolbarButton clickToolbarButtonWithTooltipInViewWithTitle(
        String title, String buttonTooltip) throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotView} view = {@link SWTWorkbenchBot#viewByTitle(String)};
     * view.close();
     * </pre>
     */
    public void closeViewByTitle(String title) throws RemoteException;

    /**
     * Returns a list of {@link String} from {@link SWTBotView} titles got by:
     * 
     * <pre>
     * {@link SWTWorkbenchBot#views()};
     * </pre>
     */
    public List<String> getViewTitles() throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotButton} button = {@link SWTWorkbenchBot#button(int)};
     * button.isEnabled();
     * </pre>
     */
    public boolean isButtonEnabled(int num) throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotButton} button = {@link SWTWorkbenchBot#button(String)};
     * button.isEnabled();
     * </pre>
     */
    public boolean isButtonEnabled(String name) throws RemoteException;

    /**
     * Returns true a {@link SWTBotShell} with the given title is open.
     */
    public boolean isShellOpenByTitle(String title) throws RemoteException;

    /**
     * Returns true if a {@link SWTBotView} with the given title is visible.
     */
    public boolean isViewOpen(String title) throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotView} view = {@link SWTWorkbenchBot#viewByTitle(String)};
     * view.setFocus();
     * </pre>
     */
    public void setFocusOnViewByTitle(String title) throws RemoteException;

    /**
     * This is the same as the following method chain:
     * 
     * <pre>
     * {@link SWTBotText} text = {@link SWTWorkbenchBot#textWithLabel(String)};
     * text.setText(String);
     * </pre>
     */
    public void setTextWithLabel(String label, String text)
        throws RemoteException;

    /**
     * This is the same as calling the following method chain:
     * 
     * <pre>
     * {@link SWTBotText} text = {@link SWTWorkbenchBot#text(String)};
     * text.setText(String);
     * </pre>
     */
    public void setTextWithText(String match, String replace)
        throws RemoteException;

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

    /**
     * Lin
     */
    public void activeMusician() throws RemoteException;

    public boolean isPerspectiveOpen(String title) throws RemoteException;

    public void openPerspectiveByName(String nodeName) throws RemoteException;

    public void setTextInClass(String contents, String projectName,
        String packageName, String className) throws RemoteException;

    public String getTextOfClass(String projectName, String packageName,
        String className) throws RemoteException;

    public void openFile(String projectName, String packageName,
        String className) throws RemoteException;

    public boolean isEditorActive(String className) throws RemoteException;

    public void activeJavaEditor(String className) throws RemoteException;

    public void clickCheckBox(String title) throws RemoteException;

    // public void showWindowUntilClosed(String title) throws RemoteException;

    public boolean equalFieldTextWithText(String label, String text)
        throws RemoteException;

    public void clickContextMenuOfTableInViewWithTitle(String viewName,
        String itemName, String contextName) throws RemoteException;

    public SWTBotToolbarButton getToolbarButtonWithTooltipInViewWithTitle(
        String title, String buttonTooltip) throws RemoteException;

    public void confirmWindowWithTable(String itemName, String shellName,
        String buttonName) throws RemoteException;

    public void confirmWindowWithTreeWithFilterText(String shellName,
        String category, String itemName, String buttonName)
        throws RemoteException;

    public SWTBotView getViewByTitle(String title) throws RemoteException;

    public void clickCheckBox() throws RemoteException;

    public void clickContextMenuOfTreeInViewWithTitle(String viewName,
        String contextName, String... itemNames) throws RemoteException;

    /*
     * waitUntil
     */
    public void waitUntilShellCloses(SWTBotShell shell) throws RemoteException;

    public void waitUntilTableHasRows(int row) throws RemoteException;

    public void waitUntilButtonEnabled(String mnemonicText)
        throws RemoteException;

    public void waitUntilConnect() throws RemoteException;

    public void waitUntilShellActive(String title) throws RemoteException;

    public void clickRadio(String title) throws RemoteException;

    public boolean existContextMenuOfTableItemOnView(String viewName,
        String itemName, String contextName) throws RemoteException;

    public void getProjectFromSVN(String path) throws RemoteException;

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
    public void selectTreeWithLabel(String name) throws RemoteException;

    public SWTBotTreeItem selectTreeWithLabelsInViewWithTitle(String viewTitle,
        String... itemNames) throws RemoteException;

    public SWTBotShell getShellWithText(String text) throws RemoteException;

}
