package de.fu_berlin.inf.dpp.stf.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.STFWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotEditor;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFLabel;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFList;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFMenu;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFTable;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFToolbarButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFView;
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
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.PEView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.ProgressView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.sarosViews.ChatView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.sarosViews.RSView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.sarosViews.RosterView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.sarosViews.SessionView;

/**
 * Tester encapsulates a test instance of Saros. It takes use of all RMI
 * interfaces to help testwriters to write their STF tests nicely. STF is short
 * for Sandor's Test Framework.
 */
public class Tester extends STF {
    private static final Logger log = Logger.getLogger(Tester.class);

    public PEView pEV;
    public ProgressView progressV;
    public RosterView sarosBuddiesV;
    public SessionView sarosSessionV;
    public RSView rSV;
    public ChatView chatV;
    public ConsoleView consoleV;

    public STFWorkbenchBot bot;
    public STFTable table;
    public STFTree tree;
    public STFTreeItem treeItem;
    public STFButton button;
    public STFToolbarButton toolbarButton;
    public STFBotShell shell;
    public STFView view;
    public STFMenu menu;
    public STFLabel label;
    public STFText text;
    public STFList list;

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
    public String host;
    public int port;

    public Tester(JID jid, String password, String host, int port) {
        super();
        this.jid = jid;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    /*************** init Methods ******************/

    public void initBot() throws AccessException, RemoteException,
        NotBoundException {
        log.trace("initBot enter, initRmi");
        getRegistriedRmiObject();
    }

    private void getRegistriedRmiObject() throws RemoteException,
        NotBoundException, AccessException {
        Registry registry = LocateRegistry.getRegistry(host, port);
        try {

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

            bot = (STFWorkbenchBot) registry.lookup("bot");
            table = (STFTable) registry.lookup("table");
            tree = (STFTree) registry.lookup("tree");
            treeItem = (STFTreeItem) registry.lookup("treeItem");
            button = (STFButton) registry.lookup("button");
            toolbarButton = (STFToolbarButton) registry.lookup("toolbarButton");
            menu = (STFMenu) registry.lookup("menu");
            view = (STFView) registry.lookup("view");
            label = (STFLabel) registry.lookup("label");
            text = (STFText) registry.lookup("text");
            list = (STFList) registry.lookup("list");
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
        }

    }

    /**
     * @Return the name segment of {@link JID}.
     */
    public String getName() {
        return jid.getName();
    }

    /**
     * @Return the JID without resource qualifier.
     */
    public String getBaseJid() {
        return jid.getBase();
    }

    /**
     * @Return the resource qualified {@link JID}.
     */
    public String getRQjid() {
        return jid.toString();
    }

    public String getXmppServer() {
        return jid.getDomain();
    }

    public STFWorkbenchBot bot() {
        return bot;
    }
}