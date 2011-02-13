package de.fu_berlin.inf.dpp.stf.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotEditor;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotLabel;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotList;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotToolbarButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.Workbench;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.contextMenu.OpenC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.contextMenu.SarosC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.contextMenu.TeamC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.menuBar.EditM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.menuBar.FileM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.menuBar.RefactorM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.menuBar.SarosM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.menuBar.WindowM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.ConsoleView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.ConsoleViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.PEView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.ProgressView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.sarosViews.ChatView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.sarosViews.RSView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.sarosViews.RosterView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.sarosViews.SessionView;

public class RMIObjects {

    public PEView pEV;
    public ProgressView progressV;
    public RosterView sarosBuddiesV;
    public SessionView sarosSessionV;
    public RSView rSV;
    public ChatView chatV;
    public ConsoleView consoleV;

    public STFBotTable table;
    public STFBotTree tree;
    public STFBotButton button;
    public STFBotToolbarButton toolbarButton;
    public STFBotShell shell;
    public STFBotView view;
    public STFBotMenu menu;
    public STFBotLabel label;
    public STFBotText text;
    public STFBotList list;

    public Workbench workbench;

    public STFBotEditor editor;

    // menuBar
    public FileM fileM;
    public EditM editM;
    public RefactorM refactorM;
    public WindowM windowM;
    public SarosM sarosM;

    // contextMenu
    public TeamC team;
    public SarosC sarosC;
    public OpenC openC;

    public JID jid;
    public String password;

    private static RMIObjects rmiObjects;

    /**
     * {@link ConsoleViewImp} is a singleton, but inheritance is possible.
     */
    public static RMIObjects getInstance(String host, int port) {
        if (rmiObjects != null)
            return rmiObjects;
        rmiObjects = new RMIObjects(host, port);
        return rmiObjects;
    }

    public RMIObjects(String host, int port) {
        getRegistriedRmiObject(host, port);
    }

    private void getRegistriedRmiObject(String host, int port) {
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(host, port);

            chatV = (ChatView) registry.lookup("chatView");
            sarosBuddiesV = (RosterView) registry.lookup("rosterView");
            sarosSessionV = (SessionView) registry.lookup("sessionView");
            /*
             * TODO i am not sure, if i can pass the local value to remote
             * object. It worked for the local tests, but i don't know if it
             * work for the remote tests too.
             */
            sarosSessionV.setJID(jid);
            rSV = (RSView) registry.lookup("remoteScreenView");
            pEV = (PEView) registry.lookup("packageExplorerView");
            progressV = (ProgressView) registry.lookup("progressView");
            consoleV = (ConsoleView) registry.lookup("consoleView");
            workbench = (Workbench) registry.lookup("workbench");
            shell = (STFBotShell) registry.lookup("shell");
            editor = (STFBotEditor) registry.lookup("eclipseEditor");

            table = (STFBotTable) registry.lookup("table");
            tree = (STFBotTree) registry.lookup("tree");
            button = (STFBotButton) registry.lookup("button");
            toolbarButton = (STFBotToolbarButton) registry.lookup("toolbarButton");
            menu = (STFBotMenu) registry.lookup("menu");
            view = (STFBotView) registry.lookup("view");
            label = (STFBotLabel) registry.lookup("label");
            text = (STFBotText) registry.lookup("text");
            list = (STFBotList) registry.lookup("list");
            // menus in menu bar
            fileM = (FileM) registry.lookup("fileM");
            editM = (EditM) registry.lookup("editM");
            refactorM = (RefactorM) registry.lookup("refactorM");
            windowM = (WindowM) registry.lookup("windowM");
            sarosM = (SarosM) registry.lookup("sarosM");

            // contextMenu

            team = (TeamC) registry.lookup("team");

            sarosC = (SarosC) registry.lookup("saros");
            openC = (OpenC) registry.lookup("open");

        } catch (java.rmi.ConnectException e) {
            throw new RuntimeException("Could not connect to RMI of bot " + jid
                + ", did you start the Eclipse instance?");
        } catch (NotBoundException e) {
            // TODO Auto-generated catch block

        } catch (RemoteException e1) {
            // TODO Auto-generated catch block

        }

    }

}
