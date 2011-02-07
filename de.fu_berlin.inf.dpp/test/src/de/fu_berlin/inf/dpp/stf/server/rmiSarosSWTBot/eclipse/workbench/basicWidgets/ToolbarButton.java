package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponent;

public interface ToolbarButton extends EclipseComponent {

    /**
     * click the toolbar button specified with the given buttonTooltip in the
     * passed view.<br/>
     * 
     * 
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param tooltipText
     *            the tooltip of the toolbar button which you want to click.
     * 
     */
    public void clickToolbarButtonWithTooltipInView(String viewTitle,
        String tooltipText) throws RemoteException;

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
     * 
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
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param tooltipText
     *            the tooltip of the toolbar button which you want to know, if
     *            it exists.
     * @return<tt>true</tt>, if the given toolbar button exists.
     * @throws RemoteException
     */
    public boolean existsToolbarButtonInview(String viewTitle,
        String tooltipText) throws RemoteException;

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
}
