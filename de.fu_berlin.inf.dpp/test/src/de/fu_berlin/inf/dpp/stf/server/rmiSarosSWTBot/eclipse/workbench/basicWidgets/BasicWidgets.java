package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosSWTBotPreferences;

/**
 * This interface contains convenience API to perform a action using basic
 * widgets. You can start off as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link Tester} object in your junit-test. (How
 * to do it please look at the javadoc in class {@link TestPattern} or read the
 * user guide in TWiki https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
 * <li>
 * then you can use the object basic initialized in {@link Tester} to access the
 * API :), e.g.
 * 
 * <pre>
 * alice.basic.clickButton(&quot;Finish&quot;);
 * </pre>
 * 
 * </li>
 * 
 * @author Lin
 */
public interface BasicWidgets extends Remote {

    /**********************************************
     * 
     * basic widget: {@link SWTBotText}.
     * 
     **********************************************/

    /**
     * set the given text into the {@link SWTBotText} with the specified
     * 
     * @param text
     *            the text which you want to insert into the text field
     * @param label
     *            the label on the widget.
     * @throws RemoteException
     */
    public void setTextInTextWithLabel(String text, String label)
        throws RemoteException;

    /**
     * 
     * @param label
     *            the label on the widget.
     * @return the text in the given {@link SWTBotText}
     * @throws RemoteException
     */
    public String getTextInTextWithLabel(String label) throws RemoteException;

    /**********************************************
     * 
     * basic widget: {@link SWTBotLabel}.
     * 
     **********************************************/
    /**
     * 
     * @return the text of the first found {@link SWTBotLabel}
     * @throws RemoteException
     */
    public String getTextOfLabel() throws RemoteException;

    /**
     * 
     * @param label
     *            the text of the label
     * @return<tt>true</tt>, if the given label exists.
     * @throws RemoteException
     */
    public boolean existsLabel(String label) throws RemoteException;

    /**
     * click the toolbar button specified with the given buttonTooltip in the
     * passed view.<br/>
     * 
     * <b>NOTE</b>, when you are not sure about the full tooltipText of the
     * toolbarButton, please use this method.
     * 
     * @param viewName
     *            the title on the view tab.
     * @param buttonTooltip
     *            the tooltip of the toolbar button which you want to click.
     * @return
     */
    public void clickToolbarButtonWithRegexTooltipInView(String viewName,
        String buttonTooltip) throws RemoteException;

    /**
     * click the toolbar button specified with the given tooltip in the given
     * view. e.g. connect. You need to pass the full tooltiptext.
     * 
     * @param viewName
     *            the title on the view tab.
     * @param tooltip
     *            the tooltip of the toolbar button which you want to click.
     */
    public void clickToolbarPushButtonWithTooltipInView(String viewName,
        String tooltip) throws RemoteException;

    /**
     * @return<tt>true</tt>, if there are some label texts existed in the given
     *                       view. You can only see the label texts when you are
     *                       not in a session.
     * 
     * @param viewName
     *            the title on the view tab.
     */
    public boolean existsLabelInView(String viewName) throws RemoteException;

    /**
     * 
     * @param viewName
     *            the title on the view tab.
     * @param buttonTooltip
     *            the tooltip of the toolbar button which you want to know, if
     *            it is enabled.
     * @return <tt>true</tt>, if the given toolbar button is enabled.
     */
    public boolean isToolbarButtonInViewEnabled(String viewName,
        String buttonTooltip) throws RemoteException;

    /**
     * 
     * Waits until the {@link SarosSWTBotPreferences#SAROS_TIMEOUT} is reached
     * or the view is active.
     * 
     * @param viewName
     *            name of the view, which should be active.
     */
    public void waitUntilViewActive(String viewName) throws RemoteException;

    /**
     * open the given view specified with the viewId.
     * 
     * @param viewId
     *            the id of the view, which you want to open.
     */
    public void openViewById(final String viewId) throws RemoteException;

    /**
     * @param title
     *            the title on the view tab.
     * @return <tt>true</tt> if the specified view is open.
     * @see ViewPart#getTitlesOfOpenedViews()
     */
    public boolean isViewOpen(String title) throws RemoteException;

    /**
     * Set focus on the specified view. It should be only called if View is
     * open.
     * 
     * @param title
     *            the title on the view tab.
     * @see SWTBotView#setFocus()
     */
    public void setFocusOnViewByTitle(String title) throws RemoteException;

    /**
     * @param title
     *            the title on the view tab.
     * @return <tt>true</tt> if the specified view is active.
     */
    public boolean isViewActive(String title) throws RemoteException;

    /**
     * close the specified view
     * 
     * @param title
     *            the title on the view tab.
     */
    public void closeViewByTitle(String title) throws RemoteException;

    /**
     * close the given view specified with the viewId.
     * 
     * @param viewId
     *            the id of the view, which you want to close.
     */
    public void closeViewById(final String viewId) throws RemoteException;

    /**
     * @return the title list of all the views which are opened currently.
     * @see SWTWorkbenchBot#views()
     */
    public List<String> getTitlesOfOpenedViews() throws RemoteException;

    /**********************************************
     * 
     * actions on the widget: {@link SWTBotMenu}.
     * 
     **********************************************/
    /**
     * clicks the main menus with the passed texts.
     * 
     * @param texts
     *            title of the menus, example: Window -> Show View -> Other...
     * 
     * @throws RemoteException
     */
    public void clickMenuWithTexts(String... texts) throws RemoteException;
}
