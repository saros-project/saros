package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.OpenCImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.SarosCImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.TeamCImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.EditM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.EditMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.FileM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.FileMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.RefactorM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.RefactorMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.SarosM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.SarosMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.WindowM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.WindowMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.ConsoleView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.ConsoleViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.PEView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.PEViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.ProgressView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.ProgressViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.ChatView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.ChatViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RSView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RSViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RosterView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RosterViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.SessionView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.SessionViewImp;

public class SarosBotImp extends STF implements SarosBot {

    private static transient SarosBotImp self;

    private static OpenCImp openC;
    private static SarosCImp sarosC;
    private static TeamCImp teamC;
    private static EditMImp editM;
    private static FileMImp fileM;
    private static RefactorMImp refactorM;
    private static SarosMImp sarosM;

    private static WindowMImp windowM;
    private static ChatViewImp chatV;
    private static RosterViewImp rosterV;
    private static RSViewImp rsV;
    private static SessionViewImp sessionV;
    private static ConsoleViewImp consoleV;
    private static PEViewImp pEV;
    private static ProgressViewImp progressvV;

    /**
     * {@link SarosBotImp} is a singleton, but inheritance is possible.
     */
    public static SarosBotImp getInstance() {
        if (self != null)
            return self;
        self = new SarosBotImp();

        openC = OpenCImp.getInstance();
        sarosC = SarosCImp.getInstance();
        teamC = TeamCImp.getInstance();
        editM = EditMImp.getInstance();
        fileM = FileMImp.getInstance();
        refactorM = RefactorMImp.getInstance();
        sarosM = SarosMImp.getInstance();
        windowM = WindowMImp.getInstance();
        chatV = ChatViewImp.getInstance();
        rosterV = RosterViewImp.getInstance();
        rsV = RSViewImp.getInstance();
        sessionV = SessionViewImp.getInstance();
        consoleV = ConsoleViewImp.getInstance();
        pEV = PEViewImp.getInstance();
        progressvV = ProgressViewImp.getInstance();

        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * finders
     * 
     **********************************************/
    public FileM file() throws RemoteException {
        return fileM;
    }

    public EditM edit() throws RemoteException {
        return editM;
    }

    public RefactorM refactor() throws RemoteException {
        return refactorM;
    }

    public SarosM saros() throws RemoteException {
        return sarosM;
    }

    public WindowM window() throws RemoteException {
        return windowM;
    }

    public ChatView chatView() throws RemoteException {
        return chatV;
    }

    public RosterView buddiesView() throws RemoteException {
        return rosterV;
    }

    public RSView remoteScreenView() throws RemoteException {
        return rsV;
    }

    public SessionView sessionView() throws RemoteException {
        return sessionV;
    }

    public ConsoleView consoleView() throws RemoteException {
        return consoleV;
    }

    public PEView packageExplorerView() throws RemoteException {
        return pEV;
    }

    public ProgressView progressView() throws RemoteException {
        return progressvV;
    }

    public void setJID(JID jid) throws RemoteException {
        localJID = jid;
    }

}
