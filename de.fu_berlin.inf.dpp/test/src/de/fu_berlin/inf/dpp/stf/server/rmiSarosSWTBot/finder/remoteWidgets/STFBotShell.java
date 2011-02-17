package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;
import java.util.Map;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponent;

public interface STFBotShell extends EclipseComponent {

    /**********************************************
     * 
     * open/close/activate the view
     * 
     **********************************************/
    /**
     * activate the shell specified with the given title.
     * 
     * @param title
     *            the title of the shell.
     * @return <tt>true</tt>, it the given shell is open and can be activated.
     */
    public boolean activate() throws RemoteException;

    public boolean isActive() throws RemoteException;

    public void closeShell() throws RemoteException;

    /**********************************************
     * 
     * get
     * 
     **********************************************/
    /**
     * 
     * @return the title of the active shell.
     * @throws RemoteException
     */
    public String getTextOfActiveShell() throws RemoteException;

    public String getErrorMessageInShell() throws RemoteException;

    /**********************************************
     * 
     * exists the given widget in the shell
     * 
     **********************************************/
    /**
     * 
     * @param title
     *            the title of the shell.
     * @param tableItemName
     *            the name of the tableItem.
     * @return <tt>true</tt>, if the given tableItem is existed in the shell.
     * @throws RemoteException
     */
    public boolean existsTableItemInShell(String tableItemName)
        throws RemoteException;

    /**********************************************
     * 
     * waits until the widget...
     * 
     **********************************************/

    /**
     * waits until the given STFBotShell is active.
     * 
     * @param title
     *            the title of the shell.
     * @throws RemoteException
     */
    public void waitUntilActive() throws RemoteException;

    public void waitShortUntilIsShellClosed() throws RemoteException;

    /**
     * waits until the given STFBotShell is closed.
     * 
     * @param title
     *            the title of the shell.
     * @throws RemoteException
     */
    public void waitLongUntilShellClosed() throws RemoteException;

    public void setShellTitle(String title) throws RemoteException;

    public STFBot bot_() throws RemoteException;

    /**********************************************
     * 
     * confirm shell
     * 
     **********************************************/
    /**
     * confirm a pop-up window.
     * 
     * @param title
     *            title of the shell.
     * @param buttonText
     *            text of the button in the shell.
     * 
     */
    public void confirm(String buttonText) throws RemoteException;

    /**
     * confirm a pop-up window with a tree. You should first select a tree node
     * and then confirm with button.
     * 
     * @param title
     *            title of the shell
     * @param buttonText
     *            text of the button
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * 
     */
    public void confirmShellWithTree(String buttonText, String... nodes)
        throws RemoteException;

    public void confirmShellWithTextField(String textLabel, String text,
        String buttonText) throws RemoteException;

    public void confirmWithTextFieldAndWait(Map<String, String> labelsAndTexts,
        String buttonText) throws RemoteException;

    public void confirmShellWithTreeWithWaitingExpand(String buttonText,
        String... nodes) throws RemoteException;

    /**
     * confirm a pop-up window with a checkbox.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * @param isChecked
     *            if the checkbox selected or not.
     * @throws RemoteException
     */
    public void confirmWindowWithCheckBox(String buttonText, boolean isChecked)
        throws RemoteException;

    /**
     * confirm a pop-up window with more than one checkbox.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * @param itemNames
     *            the labels of the checkboxs, which you want to select.
     * 
     */
    public void confirmWindowWithCheckBoxs(String buttonText,
        String... itemNames) throws RemoteException;

    /**
     * confirm a pop-up window with a table. You should first select a table
     * item and then confirm with button.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * @param itemName
     *            the name of the table item, which you want to select.
     * @throws RemoteException
     */
    public void confirmShellWithTable(String itemName, String buttonText)
        throws RemoteException;

    /**
     * confirm a pop-up window with a tree using filter text. You should first
     * input a filter text in the text field and then select a tree node,
     * confirm with button.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * @param teeNode
     *            tree node, which you want to select.
     * @param rootOfTreeNode
     *            root of the tree node.
     * @throws RemoteException
     */
    public void confirmShellWithTreeWithFilterText(String rootOfTreeNode,
        String teeNode, String buttonText) throws RemoteException;

}
