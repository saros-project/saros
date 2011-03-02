package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.noFinder;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.Component;
import de.fu_berlin.inf.dpp.util.FileUtils;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;

public class NoBotImp extends Component implements NoBot {

    private static transient NoBotImp self;

    /**
     * {@link NoBotImp} is a singleton, but inheritance is possible.
     */
    public static NoBotImp getInstance() {
        if (self != null)
            return self;
        self = new NoBotImp();

        return self;
    }

    public String getFileContent(String... nodes) throws RemoteException,
        IOException, CoreException {
        IPath path = new Path(getPath(nodes));
        log.info("Checking existence of file \"" + path + "\"");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);

        log.info("Checking full path: \"" + file.getFullPath().toOSString()
            + "\"");
        return ConvertStreamToString(file.getContents());
    }

    public void waitUntilFileContentSame(final String otherClassContent,
        final String... fileNodes) throws RemoteException {

        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return getFileContent(fileNodes).equals(otherClassContent);
            }

            public String getFailureMessage() {
                return "The both contents are not" + " same.";
            }
        });
    }

    public void createAccountNoGUI(String server, String username,
        String password) throws RemoteException {
        xmppAccountStore.createNewAccount(username, password, server);
    }

    public void disableAutomaticReminderNoGUI() throws RemoteException {
        if (!feedbackManager.isFeedbackDisabled()) {
            feedbackManager.setFeedbackDisabled(true);
        }
    }

    public void deleteAccountNoGUI(JID jid) throws RemoteException {
        xmppAccountStore.deleteAccount(getXMPPAccount(jid));
    }

    public void changeAccountNoGUI(JID jid, String newUserName,
        String newPassword, String newServer) throws RemoteException {
        xmppAccountStore.changeAccountData(getXMPPAccount(jid).getId(),
            newUserName, newPassword, newServer);
    }

    public void activateAccountNoGUI(JID jid) throws RemoteException {
        XMPPAccount account = getXMPPAccount(jid);
        xmppAccountStore.setAccountActive(account);
    }

    /**
     * 
     * @param jid
     *            a JID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @return {@link XMPPAccount} of the given jid.
     */
    private XMPPAccount getXMPPAccount(JID jid) {
        for (XMPPAccount account : xmppAccountStore.getAllAccounts()) {
            if (jid.getName().equals(account.getUsername())
                && jid.getDomain().equals(account.getServer())) {
                return account;
            }
        }
        return null;
    }

    /**********************************************
     * 
     * actions without GUI
     * 
     **********************************************/
    public void switchProject(String projectName, String url)
        throws RemoteException {
        switchResource(projectName, url, "HEAD");
    }

    public void switchResource(String fullPath, String url)
        throws RemoteException {
        switchResource(fullPath, url, "HEAD");
    }

    public void switchResource(String fullPath, String url, String revision)
        throws RemoteException {

        final IPath path = new Path(fullPath);
        final IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            throw new RemoteException("Resource \"" + path + "\" not found.");

        final IProject project = resource.getProject();
        VCSAdapter vcs = VCSAdapter.getAdapter(project);
        if (vcs == null) {
            throw new RemoteException("No VCSAdapter found for \""
                + project.getName() + "\".");
        }

        vcs.switch_(resource, url, revision, new NullProgressMonitor());
    }

    public void deleteProjectNoGUI(String projectName) throws RemoteException {
        IPath path = new Path(projectName);
        deleteNoGUI(path);
    }

    public void deleteFolderNoGUI(String... folderNodes) throws RemoteException {
        IPath path = new Path(getPath(folderNodes));
        deleteNoGUI(path);
    }

    public void deletePkgNoGUI(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches("[\\w*\\.]*\\w*")) {
            IPath path = new Path(getPkgPath(projectName, pkg));
            deleteNoGUI(path);
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void deleteClassNoGUI(String projectName, String pkg,
        String className) throws RemoteException {
        IPath path = new Path(getClassPath(projectName, pkg, className));
        deleteNoGUI(path);
    }

    public void deleteAllProjectsNoGUI() throws RemoteException {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = root.getProjects();
        for (int i = 0; i < projects.length; i++) {
            try {
                FileUtils.delete(projects[i]);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete files ", e);
            }
        }
    }

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/

    private void deleteNoGUI(IPath path) {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        if (resource == null) {
            log.debug(" Can't find resource");
            return;
        }
        if (resource.isAccessible()) {
            try {
                FileUtils.delete(resource);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete the resource", e);
            }
        }
    }

}
