package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseObject;

public class WorkbenchObjectImp extends EclipseObject implements
    WorkbenchObject {

    // public static WorkbenchObjectImp classVariable;

    private static transient WorkbenchObjectImp self;

    /**
     * {@link WorkbenchObjectImp} is a singleton, but inheritance is possible.
     */
    public static WorkbenchObjectImp getInstance() {
        if (self != null)
            return self;
        self = new WorkbenchObjectImp();
        return self;
    }

    public void openSarosViews() throws RemoteException {
        rosterVObject.openRosterView();
        sessonVObject.openSessionView();
        chatVObject.openChatView();
        remoteScreenVObject.openRemoteScreenView();
    }

    public void resetSaros() throws RemoteException {
        rosterVObject.xmppDisconnect();
        stateObject.deleteAllProjects();
    }

    public SWTBotShell getEclipseShell() throws RemoteException {
        SWTBotShell[] shells = bot.shells();
        for (SWTBotShell shell : shells) {
            if (shell.getText().matches(".+? - .+")) {
                log.debug("shell found matching \"" + ".+? - .+" + "\"");
                return shell;
            }
        }
        final String message = "No shell found matching \"" + ".+? - .+"
            + "\"!";
        log.error(message);
        throw new RemoteException(message);
    }

    public void activateEclipseShell() throws RemoteException {
        getEclipseShell().activate().setFocus();
        // return activateShellWithMatchText(".+? - .+");
        // Display.getDefault().syncExec(new Runnable() {
        // public void run() {
        // final IWorkbench wb = PlatformUI.getWorkbench();
        // final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        // win.getShell().setActive();
        // }
        // });

    }

    public void resetWorkbench() throws RemoteException {
        exportedMenuObject.openPerspectiveJava();
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                final IWorkbench wb = PlatformUI.getWorkbench();
                final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
                IWorkbenchPage page = win.getActivePage();
                if (page != null) {
                    page.closeAllEditors(false);
                }
                Shell activateShell = Display.getCurrent().getActiveShell();
                if (activateShell != null && activateShell != win.getShell()) {
                    activateShell.close();
                }
            }
        });
    }

}
