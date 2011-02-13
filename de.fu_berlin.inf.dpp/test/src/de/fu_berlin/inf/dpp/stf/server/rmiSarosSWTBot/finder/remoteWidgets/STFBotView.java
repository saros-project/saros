package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponent;

public interface STFBotView extends EclipseComponent {

    /**
     * Waits until the {@link SarosSWTBotPreferences#SAROS_TIMEOUT} is reached
     * or the view is active.
     * 
     * @param viewName
     *            name of the view, which should be active. //
     */
    public void waitUntilIsActive() throws RemoteException;

    /**
     * Set focus on the specified view. It should be only called if View is
     * open.
     * 
     * @param title
     *            the title on the view tab.
     * @see SWTBotView#setFocus()
     */
    public void setFocus() throws RemoteException;

    /**
     * @param title
     *            the title on the view tab.
     * @return <tt>true</tt> if the specified view is active.
     */
    public boolean isActive() throws RemoteException;

    /**
     * close the specified view
     * 
     * @param title
     *            the title on the view tab.
     */
    public void close() throws RemoteException;

    /**
     * close the given view specified with the viewId.
     * 
     * @param viewId
     *            the id of the view, which you want to close.
     */
    public void closeById(final String viewId) throws RemoteException;

    public void setViewTitle(String title) throws RemoteException;

    public STFBot bot_() throws RemoteException;

}
