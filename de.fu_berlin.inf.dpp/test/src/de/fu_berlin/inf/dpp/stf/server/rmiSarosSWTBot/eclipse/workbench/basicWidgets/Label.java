package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;

public interface Label extends Remote {

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
     * @return<tt>true</tt>, if there are some label texts existed in the given
     *                       view. You can only see the label texts when you are
     *                       not in a session.
     * 
     * @param viewName
     *            the title on the view tab.
     */
    public boolean existsLabelInView(String viewName) throws RemoteException;

}
