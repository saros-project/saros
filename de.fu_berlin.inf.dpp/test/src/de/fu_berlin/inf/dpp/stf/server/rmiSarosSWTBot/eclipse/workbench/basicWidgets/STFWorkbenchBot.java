package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

public interface STFWorkbenchBot extends STFBot {
    public STFView view(String viewTitle) throws RemoteException;

    /**
     * @return the title list of all the views which are opened currently.
     * @see SWTWorkbenchBot#views()
     */
    public List<String> getTitlesOfOpenedViews() throws RemoteException;

    public boolean isViewOpen(String title) throws RemoteException;

    /**
     * open the given view specified with the viewId.
     * 
     * @param viewId
     *            the id of the view, which you want to open.
     */
    public void openById(String viewId) throws RemoteException;
}
