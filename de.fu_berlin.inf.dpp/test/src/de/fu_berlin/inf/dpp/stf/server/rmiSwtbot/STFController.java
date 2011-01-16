package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot;

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
import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosState;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosStateImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosPEViewComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosWorkbenchComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.sarosViewComponents.ChatViewComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.sarosViewComponents.RSViewComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.sarosViewComponents.RosterViewComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.sarosViewComponents.SessionViewComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.BasicComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ConsoleViewComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EditorComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ProgressViewComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ShellComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.menuBar.EditMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.menuBar.FileMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.menuBar.RefactorMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.menuBar.SarosMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.menuBar.WindowMImp;

/**
 * SarosRmiSWTWorkbenchBot controls Eclipse Saros from the GUI perspective. It
 * exports {@link SarosState} via RMI. You should not use this within tests.
 * Have a look at {@link Tester} if you want to write tests.
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
        EclipseComponent.saros = saros;
        EclipseComponent.sessionManager = sessionManager;
        EclipseComponent.dataTransferManager = dataTransferManager;
        EclipseComponent.editorManager = editorManager;
        EclipseComponent.xmppAccountStore = xmppAccountStore;
        EclipseComponent.feedbackManager = feedbackManager;
        try {
            registry = LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            registry = LocateRegistry.getRegistry(port);
        }

        exportObject(ShellComponentImp.getInstance(), "shell");
        exportObject(BasicComponentImp.getInstance(), "basicObject");
        exportObject(SarosPEViewComponentImp.getInstance(),
            "packageExplorerView");
        exportObject(ProgressViewComponentImp.getInstance(), "progressView");

        exportObject(EditorComponentImp.getInstance(), "eclipseEditor");
        exportObject(RosterViewComponentImp.getInstance(), "rosterView");
        exportObject(SessionViewComponentImp.getInstance(), "sessionView");
        exportObject(RSViewComponentImp.getInstance(), "remoteScreenView");
        exportObject(ChatViewComponentImp.getInstance(), "chatView");
        exportObject(SarosWorkbenchComponentImp.getInstance(), "workbench");
        exportObject(SarosStateImp.getInstance(), "state");
        exportObject(ConsoleViewComponentImp.getInstance(), "consoleView");
        exportObject(FileMImp.getInstance(), "fileM");
        exportObject(EditMImp.getInstance(), "editM");
        exportObject(RefactorMImp.getInstance(), "refactorM");
        exportObject(WindowMImp.getInstance(), "windowM");
        exportObject(SarosMImp.getInstance(), "sarosM");
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
