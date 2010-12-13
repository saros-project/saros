package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

public class SarosWorkbenchComponentImp extends EclipseComponent implements
    SarosWorkbenchComponent {

    private static transient SarosWorkbenchComponentImp self;

    private final static String VIEW_TITLE_WELCOME = "Welcome";

    /**
     * {@link SarosWorkbenchComponentImp} is a singleton, but inheritance is
     * possible.
     */
    public static SarosWorkbenchComponentImp getInstance() {
        if (self != null)
            return self;
        self = new SarosWorkbenchComponentImp();
        return self;
    }

    public void sleep(long millis) throws RemoteException {
        bot.sleep(millis);
    }

    public void captureScreenshot(String filename) throws RemoteException {
        bot.captureScreenshot(filename);
    }

    public String getPathToScreenShot() throws RemoteException {
        Bundle bundle = saros.getBundle();
        log.debug("screenshot's directory: "
            + bundle.getLocation().substring(16) + SCREENSHOTDIR);
        String osName = System.getProperty("os.name");
        log.debug("Name of the OS: " + osName);
        if (osName.matches("Windows.*"))
            return bundle.getLocation().substring(16) + SCREENSHOTDIR;
        else if (osName.matches("Mac OS X.*")) {
            return "/" + bundle.getLocation().substring(16) + SCREENSHOTDIR;
        }
        return bundle.getLocation().substring(16) + SCREENSHOTDIR;
    }

    public void openSarosViews() throws RemoteException {
        rosterVC.openRosterView();
        sessonVC.openSessionView();
        chatVC.openChatView();
        rsVC.openRemoteScreenView();
    }

    public void closeUnnecessaryViews() throws RemoteException {
        if (viewPart.isViewOpen("Problems"))
            viewPart.closeViewByTitle("Problems");
        if (viewPart.isViewOpen("Javadoc"))
            viewPart.closeViewByTitle("Javadoc");
        if (viewPart.isViewOpen("Declaration"))
            viewPart.closeViewByTitle("Declaration");
        if (viewPart.isViewOpen("Task List"))
            viewPart.closeViewByTitle("Task List");
        if (viewPart.isViewOpen("Outline"))
            viewPart.closeViewByTitle("Outline");
    }

    public void resetSaros() throws RemoteException {
        rosterVC.resetAllBuddyName();
        rosterVC.disconnectGUI();
        state.deleteAllProjects();
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
        mainMenuC.openPerspectiveJava();
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                final IWorkbench wb = PlatformUI.getWorkbench();
                final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
                Shell[] shells = Display.getCurrent().getShells();
                for (Shell shell : shells) {
                    if (shell != null && shell != win.getShell()) {
                        shell.close();
                    }
                }
                IWorkbenchPage page = win.getActivePage();
                if (page != null) {
                    page.closeAllEditors(false);
                }
            }
        });
    }

    public void setUpWorkbench() throws RemoteException {
        resetWorkbench();
        state.deleteAllProjects();
        peVC.deleteAllProjectsWithGUI();

    }

    public void closeWelcomeView() throws RemoteException {
        viewPart.closeViewByTitle(VIEW_TITLE_WELCOME);
    }

}
