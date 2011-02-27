package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.EditM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.FileM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.RefactorM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.SarosM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.WindowM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.ConsoleView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.PEView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.ProgressView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.ChatView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RSView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RosterView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.SessionView;

public interface SarosBot extends Remote {

    /**********************************************
     * 
     * finders
     * 
     **********************************************/
    public FileM file() throws RemoteException;

    public EditM edit() throws RemoteException;

    public RefactorM refactor() throws RemoteException;

    public SarosM saros() throws RemoteException;

    public WindowM window() throws RemoteException;

    public ChatView chatView() throws RemoteException;

    public RosterView buddiesView() throws RemoteException;

    public RSView remoteScreenView() throws RemoteException;

    public SessionView sessionView() throws RemoteException;

    public ConsoleView consoleView() throws RemoteException;

    public PEView packageExplorerView() throws RemoteException;

    public ProgressView progressView() throws RemoteException;

    public void setJID(JID jid) throws RemoteException;

}