package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.waits.Conditions;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponent;

public interface STFBotTable extends EclipseComponent {

    /**********************************************
     * 
     * actions with basic widget: {@link SWTBotTable}.
     * 
     **********************************************/

    /***************** exists table item ****************** */
    /**
     * this method is suitable for STFBotShell widget
     * 
     * @param itemText
     *            text of a table item.
     * @return<tt>true</tt>, if the tableItem specified with the given itemText
     *                       exists in the table: bot.table().
     * 
     * @throws RemoteException
     */
    public boolean existsTableItem(String itemText) throws RemoteException;

    /**
     * this method is suitable for view widget
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param itemText
     *            text of a table item.
     * @return<tt>true</tt>, if the tableItem specified with the given itemText
     *                       in the view specified with the given viewTitle
     *                       exists in the table:
     *                       bot.getViewTitle(viewTitle).bot().table().
     * @throws RemoteException
     */
    public boolean existsTableItemInView(String viewTitle, String itemText)
        throws RemoteException;

    /**
     * 
     * 
     * Selects the tableItem matching the given itemText. This method is
     * suitable for STFBotShell widget.
     * 
     * @param itemText
     *            text of the selected table item.
     * 
     * @throws RemoteException
     */
    public void selectTableItem(String itemText) throws RemoteException;

    /**
     * Selects the tableItem matching the given itemText in the given view. This
     * method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param itemText
     *            text of the selected table item.
     * @throws RemoteException
     */
    public void selectTableItemInView(String viewTitle, String itemText)
        throws RemoteException;

    /***************** exists context of table item ****************** */
    /**
     * This method is suitable for shell widget.
     * 
     * @param itemText
     *            text of the selected table item.
     * @param contextName
     *            the name of the context of the selected tableItem.
     * @return<tt></tt>, if the specified contextMenu exists.
     * @throws RemoteException
     */
    public boolean existsContextMenuOfTableItem(String itemText,
        String contextName) throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param itemText
     *            the name of the table item.
     * @param contextName
     *            the name of the context menu of the given tableItem specified
     *            with the given itemText
     * @return <tt>true</tt> if the specified context menu of the select table
     *         item exists.
     */
    public boolean existsContextMenuOfTableItemInView(String viewTitle,
        String itemText, String contextName) throws RemoteException;

    /***************** click context of table item ****************** */
    /**
     * This method is suitable for shell widget.
     * 
     * Click the context menu matching the given contextName of the selected
     * table item matching the given itemText.<br/>
     * Operational steps:
     * <ol>
     * <li>select a table item</li>
     * <li>then click the specified context menu on it</li>
     * </ol>
     * 
     * @param itemText
     *            text of the selected table item, whose context menu you want
     *            to click.
     * @param contextName
     *            the text on the context menu
     * 
     */
    public void clickContextMenuOfTableItem(String itemText, String contextName)
        throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * Click a context menu of the selected table item in the given view.
     * <p>
     * Operational steps:
     * <ol>
     * <li>select a table item</li>
     * <li>then click the specified context menu on it</li>
     * </ol>
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param itemName
     *            the table item' name, whose context menu you want to click.
     * 
     */
    public void clickContextMenuOfTableItemInView(String viewTitle,
        String itemName, String contextName) throws RemoteException;

    /***************** is context of table item visible ****************** */

    /**
     * This method is suitable for shell widget.
     * 
     * @param itemText
     *            text of the selected table item.
     * @param contextName
     *            the text on the context menu.
     * @return<tt>true</tt>, if the specified contextMenu of the selected
     *                       tableItem is visible.
     * @throws RemoteException
     */
    public boolean isContextMenuOfTableItemVisible(String itemText,
        String contextName) throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param itemText
     *            text of the selected table item.
     * @param contextName
     *            the text on the context menu.
     * @return<tt>true</tt>, if the specified contextMenu of the selected
     *                       tableItem is visible.
     * @throws RemoteException
     */
    public boolean isContextMenuOfTableItemVisibleInView(String viewTitle,
        String itemText, String contextName) throws RemoteException;

    /***************** is context of tree item enabled ****************** */
    /**
     * This method is suitable for shell widget.
     * 
     * @param itemText
     *            text of the selected table item.
     * @param contextName
     *            the text on the context menu
     * @return<tt>true</tt>, if the specified contextMenu of the selected
     *                       tableItem is enabled.
     * @throws RemoteException
     */
    public boolean isContextMenuOfTableItemEnabled(String itemText,
        String contextName) throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param itemText
     *            text of the selected table item.
     * @param contextName
     *            the text on the context menu
     * @return<tt>true</tt>, if the specified contextMenu of the selected
     *                       tableItem is enabled.
     * @throws RemoteException
     */
    public boolean isContextMenuOfTableItemEnabledInView(String viewTitle,
        String itemText, String contextName) throws RemoteException;

    /***************** checkbox of tree item ****************** */
    /**
     * checks the checkBox of the tableItem specified with the given itemText.
     * 
     * @param itemText
     *            text of the selected table item.
     * @throws RemoteException
     */
    public void selectCheckBoxInTable(String itemText) throws RemoteException;

    /**
     * checks the checkBox of all the tableItems specified with the given
     * itemTexts.
     * 
     * @param itemTexts
     *            the text list of all the tableItems, whose checkbox may be
     *            selected.
     * @throws RemoteException
     */
    public void selectCheckBoxsInTable(List<String> itemTexts)
        throws RemoteException;

    /**
     * 
     * @return the names of all columns of the table.
     * @throws RemoteException
     */
    public List<String> getTableColumns() throws RemoteException;

    /***************** waits until ****************** */
    /**
     * waits until the specified tableItem existed.
     * 
     * @param basic
     *            the object {@link BasicWidgets}
     * @param itemText
     *            name of the table item, which you want to check,if it exists.
     * @throws RemoteException
     */
    public void waitUntilTableItemExisted(STFBotTable basic, String itemText)
        throws RemoteException;

    /**
     * @throws RemoteException
     * @see Conditions#tableHasRows(org.eclipse.swtbot.swt.finder.widgets.SWTBotTable,
     *      int)
     */
    public void waitUntilTableHasRows(int row) throws RemoteException;

    /**
     * waits until the specified contextMenu of the selected tableItem enabled.
     * 
     * @param basic
     *            the object {@link BasicWidgets}
     * @param itemText
     *            name of the table item, whose context menu you want to
     *            check,if it is enabled.
     * @param contextName
     *            the name on the context menu.
     * @throws RemoteException
     */
    public void waitUntilIsContextMenuOfTableItemEnabled(STFBotTable basic,
        String itemText, String contextName) throws RemoteException;
}
