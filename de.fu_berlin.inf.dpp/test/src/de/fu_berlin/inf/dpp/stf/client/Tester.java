package de.fu_berlin.inf.dpp.stf.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.client.wrapper.CommonWidgetsWrapper;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.Workbench;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Bot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Button;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Label;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.ListW;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Menu;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Shell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Table;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Text;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.ToolbarButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Tree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.TreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.View;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.contextMenu.OpenC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.contextMenu.SarosC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.contextMenu.TeamC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.editor.Editor;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar.EditM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar.FileM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar.RefactorM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar.SarosM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar.WindowM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.ConsoleView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.PEView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.ProgressView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.ChatView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.RSView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.RosterView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.SessionView;

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

    public Bot bot;
    public Table table;
    public Tree tree;
    public TreeItem treeItem;
    public Button button;
    public ToolbarButton toolbarButton;
    public Shell shell;
    public View view;
    public Menu menu;
    public Label label;
    public Text text;
    public ListW list;

    public Workbench workbench;

    public Editor editor;

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
            shell = (Shell) registry.lookup("shell");
            editor = (Editor) registry.lookup("eclipseEditor");

            bot = (Bot) registry.lookup("bot");
            table = (Table) registry.lookup("table");
            tree = (Tree) registry.lookup("tree");
            treeItem = (TreeItem) registry.lookup("treeItem");
            button = (Button) registry.lookup("button");
            toolbarButton = (ToolbarButton) registry.lookup("toolbarButton");
            menu = (Menu) registry.lookup("menu");
            view = (View) registry.lookup("view");
            label = (Label) registry.lookup("label");
            text = (Text) registry.lookup("text");
            list = (ListW) registry.lookup("list");
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

    public CommonWidgetsWrapper commonWidgets() {
        return new CommonWidgetsWrapper(this);
    }
}