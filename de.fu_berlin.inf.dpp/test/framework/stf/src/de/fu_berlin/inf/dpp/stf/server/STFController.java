package de.fu_berlin.inf.dpp.stf.server;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotCCombo;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotCTabItem;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotChatLine;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotCheckBox;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotCombo;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotEditor;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotLabel;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotList;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotPerspective;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotRadio;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotStyledText;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotTableItem;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotText;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotToggleButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotToolbarButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotToolbarDropDownButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotToolbarPushButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotToolbarRadioButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotToolbarToggleButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotViewMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.impl.ContextMenusInPEView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl.NewC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl.RefactorC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl.ShareWithC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl.TeamC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.impl.ContextMenusInBuddiesArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.impl.ContextMenusInSessionArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.submenu.impl.WorkTogetherOnC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.impl.MenuBar;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.impl.SarosM;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.impl.WindowM;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.submenu.impl.SarosPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl.ConsoleView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl.PEView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl.ProgressView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.impl.Views;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.impl.Chatroom;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.impl.RSView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.impl.SarosView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;

/**
 * STFController is responsible to register all exported objects.
 * 
 */
public class STFController {

    private static final transient Logger log = Logger
        .getLogger(STFController.class);

    public static int sleepTime = 750;

    /** The RMI registry used, is not exported */
    private static transient Registry registry;

    /*
     * sometimes when connecting to a server i'm getting error:
     * java.rmi.NoSuchObjectException:no Such object in table. This happens when
     * the remote object the stub refers to has been DGC'd and GC's locally. My
     * solution is keeping a static references "classVariable" to the object in
     * the object in the server JVM.
     */
    public static void exportedObjects(int port, Saros saros,
        SarosSessionManager sessionManager,
        DataTransferManager dataTransferManager, EditorManager editorManager,
        XMPPAccountStore xmppAccountStore, FeedbackManager feedbackManager)
        throws RemoteException {

        StfRemoteObject.saros = saros;
        StfRemoteObject.sessionManager = sessionManager;
        StfRemoteObject.dataTransferManager = dataTransferManager;
        StfRemoteObject.editorManager = editorManager;
        StfRemoteObject.xmppAccountStore = xmppAccountStore;
        StfRemoteObject.feedbackManager = feedbackManager;

        try {
            registry = LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            registry = LocateRegistry.getRegistry(port);
        }

        /*
         * bots' family
         */
        // exportObject(RemoteBot.getInstance(), "bot");
        exportObject(RemoteWorkbenchBot.getInstance(), "workbenchBot");
        exportObject(SuperBot.getInstance(), "superBot");

        /*
         * export remoteWidgets
         */
        exportObject(RemoteBotButton.getInstance(), "button");
        exportObject(RemoteBotCCombo.getInstance(), "ccombo");
        exportObject(RemoteBotChatLine.getInstance(), "chatLine");
        exportObject(RemoteBotCheckBox.getInstance(), "checkBox");
        exportObject(RemoteBotCombo.getInstance(), "combo");
        exportObject(RemoteBotCTabItem.getInstance(), "cTabItem");
        exportObject(RemoteBotEditor.getInstance(), "editor");
        exportObject(RemoteBotLabel.getInstance(), "label");
        exportObject(RemoteBotList.getInstance(), "list");
        exportObject(RemoteBotMenu.getInstance(), "menu");
        exportObject(RemoteBotPerspective.getInstance(), "perspective");
        exportObject(RemoteBotRadio.getInstance(), "radio");
        exportObject(RemoteBotShell.getInstance(), "shell");
        exportObject(RemoteBotStyledText.getInstance(), "styledText");
        exportObject(RemoteBotTable.getInstance(), "table");
        exportObject(RemoteBotTableItem.getInstance(), "tableItem");
        exportObject(RemoteBotText.getInstance(), "text");
        exportObject(RemoteBotToggleButton.getInstance(), "toggleButton");
        exportObject(RemoteBotToolbarButton.getInstance(), "toolbarButton");
        exportObject(RemoteBotToolbarDropDownButton.getInstance(),
            "toolbarDropDownButton");
        exportObject(RemoteBotToolbarPushButton.getInstance(),
            "toolbarPushButon");
        exportObject(RemoteBotToolbarRadioButton.getInstance(),
            "toolbarRadioButton");
        exportObject(RemoteBotToolbarToggleButton.getInstance(),
            "toolbarToggleButton");
        exportObject(RemoteBotTree.getInstance(), "tree");
        exportObject(RemoteBotTreeItem.getInstance(), "treeItem");
        exportObject(RemoteBotView.getInstance(), "view");
        exportObject(RemoteBotViewMenu.getInstance(), "viewMenu");

        /*
         * remote eclipse components
         */
        exportObject(PEView.getInstance(), "packageExplorerView");
        exportObject(ProgressView.getInstance(), "progressView");
        exportObject(SarosView.getInstance(), "rosterView");
        exportObject(RSView.getInstance(), "remoteScreenView");
        exportObject(ConsoleView.getInstance(), "consoleView");

        exportObject(NewC.getInstance(), "fileM");
        exportObject(RefactorC.getInstance(), "refactorM");
        exportObject(WindowM.getInstance(), "windowM");
        exportObject(SarosM.getInstance(), "sarosM");

        exportObject(TeamC.getInstance(), "teamC");
        exportObject(ShareWithC.getInstance(), "shareWithC");
        exportObject(ContextMenusInPEView.getInstance(), "contextMenu");

        exportObject(ContextMenusInBuddiesArea.getInstance(),
            "buddiesContextMenu");
        exportObject(ContextMenusInSessionArea.getInstance(),
            "sessionContextMenu");
        exportObject(WorkTogetherOnC.getInstance(), "workTogetherOnC");
        exportObject(Chatroom.getInstance(), "chatroom");

        exportObject(SarosPreferences.getInstance(), "sarosPreferences");

        exportObject(Views.getInstance(), "views");
        exportObject(MenuBar.getInstance(), "menuBar");
    }

    /**
     * Add a shutdown hook to unbind exported Object from registry.
     */
    private static void addShutdownHook(final String name) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    if (registry != null && name != null)
                        registry.unbind(name);
                } catch (RemoteException e) {
                    log.warn("Failed to unbind: " + name, e);
                } catch (NotBoundException e) {
                    log.warn("Failed to unbind: " + name, e);
                }
            }
        });
    }

    /**
     * Export object by given name on our local RMI Registry.
     */
    private static Remote exportObject(Remote exportedObject, String exportName) {
        try {
            Remote remoteObject = UnicastRemoteObject.exportObject(
                exportedObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, remoteObject);
            return remoteObject;
        } catch (RemoteException e) {
            log.error("Could not export the object " + exportName, e);
        } catch (AlreadyBoundException e) {
            log.error("Could not bind the object " + exportName
                + ", because it is bound already.", e);
        }
        return null;
    }

    public static void listRmiObjects() {
        try {
            for (String s : registry.list())
                log.debug("registered Object: " + s);
        } catch (AccessException e) {
            log.error("Failed on access", e);
        } catch (RemoteException e) {
            log.error("Failed", e);
        }
    }
}
