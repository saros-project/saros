package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.Component;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

public class StateImp extends Component implements State {

    private static transient StateImp self;

    /**
     * {@link StateImp} is a singleton, but inheritance is possible.
     */
    public static StateImp getInstance() {
        if (self != null)
            return self;
        self = new StateImp();
        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/
    public boolean isProjectManagedBySVN(String projectName)
        throws RemoteException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName);
        final VCSAdapter vcs = VCSAdapter.getAdapter(project);
        if (vcs == null)
            return false;
        return true;
    }

    public String getRevision(String fullPath) throws RemoteException {
        IPath path = new Path(fullPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            throw new RemoteException("Resource \"" + fullPath
                + "\" not found.");
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs == null)
            return null;
        VCSResourceInfo info = vcs.getCurrentResourceInfo(resource);
        String result = info != null ? info.revision : null;
        return result;
    }

    public String getURLOfRemoteResource(String fullPath)
        throws RemoteException {
        IPath path = new Path(fullPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            throw new RemoteException("Resource not found at \"" + fullPath
                + "\"");
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs == null)
            return null;
        final VCSResourceInfo info = vcs.getResourceInfo(resource);
        return info.url;
    }

    // states
    public boolean isAccountActiveNoGUI(JID jid) throws RemoteException {
        XMPPAccount account = getXMPPAccount(jid);
        if (account == null)
            return false;
        return account.isActive();
    }

    public boolean isAccountExistNoGUI(JID jid, String password)
        throws RemoteException {
        for (XMPPAccount account : xmppAccountStore.getAllAccounts()) {
            log.debug("account id: " + account.getId());
            log.debug("account username: " + account.getUsername());
            log.debug("account password: " + account.getPassword());
            log.debug("account server: " + account.getServer());
            if (jid.getName().equals(account.getUsername())
                && jid.getDomain().equals(account.getServer())
                && password.equals(account.getPassword())) {
                return true;
            }
        }
        return false;
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

    public boolean existsFile(String viewTitle, String... fileNodes)
        throws RemoteException {

        return bot().view(viewTitle).bot().tree()
            .selectTreeItem(getParentNodes(fileNodes))
            .existsSubItemWithRegex(getLastNode(fileNodes + ".*"));
    }

    public boolean existsProjectNoGUI(String projectName)
        throws RemoteException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName);
        return project.exists();
    }

    public boolean existsFolderNoGUI(String... folderNodes)
        throws RemoteException {
        IPath path = new Path(getPath(folderNodes));
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            return false;
        return true;
    }

    public boolean existsPkgNoGUI(String projectName, String pkg)
        throws RemoteException {
        IPath path = new Path(getPkgPath(projectName, pkg));
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource != null)
            return true;
        return false;
    }

    public boolean existsFileNoGUI(String filePath) throws RemoteException {
        IPath path = new Path(filePath);
        log.info("Checking existence of file \"" + path + "\"");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);
        return file.exists();
    }

    public boolean existsFileNoGUI(String... nodes) throws RemoteException {
        return existsFileNoGUI(getPath(nodes));
    }

    public boolean existsClassNoGUI(String projectName, String pkg,
        String className) throws RemoteException {
        return existsFileNoGUI(getClassPath(projectName, pkg, className));
    }

    public boolean isInSession() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_LEAVE_THE_SESSION);
    }

    public boolean existsParticipant(JID participantJID) throws RemoteException {
        precondition();
        String participantLabel = getParticipantLabel(participantJID);

        STFBotTable table = bot().view(VIEW_SAROS_SESSION).bot().table();

        for (int i = 0; i < table.rowCount(); i++) {
            if (table.getTableItem(i).getText().equals(participantLabel))
                return true;
        }
        return false;
    }

    public boolean existsLabelInSessionView() throws RemoteException {
        precondition();
        return bot().view(VIEW_SAROS_SESSION).bot().existsLabel();
    }

    public boolean hasWriteAccess() throws RemoteException {
        precondition();
        return !getParticipantLabel(localJID).contains(PERMISSION_NAME);
    }

    public boolean hasWriteAccessBy(JID... jids) throws RemoteException {
        precondition();
        boolean result = true;
        for (JID jid : jids) {
            result &= !bot().view(VIEW_SAROS_SESSION).bot().table()
                .getTableItem(getParticipantLabel(jid))
                .contextMenu(CM_GRANT_WRITE_ACCESS).isEnabled()
                && !getParticipantLabel(jid).contains(PERMISSION_NAME);
        }
        return result;
    }

    public boolean hasWriteAccessBy(String... tableItemTexts)
        throws RemoteException {
        precondition();
        boolean result = true;
        for (String text : tableItemTexts) {
            result &= !bot().view(VIEW_SAROS_SESSION).bot().table()
                .getTableItem(text).contextMenu(CM_GRANT_WRITE_ACCESS)
                .isEnabled()
                && !text.contains(PERMISSION_NAME);
        }
        return result;
    }

    public boolean hasReadOnlyAccess() throws RemoteException {
        precondition();
        return getParticipantLabel(localJID).contains(PERMISSION_NAME);
    }

    public boolean hasReadOnlyAccessBy(JID... jids) throws RemoteException {
        precondition();
        boolean result = true;
        for (JID jid : jids) {
            boolean isEnabled = bot().view(VIEW_SAROS_SESSION).bot().table()
                .getTableItem(getParticipantLabel(jid))
                .contextMenu(CM_RESTRICT_TO_READ_ONLY_ACCESS).isEnabled();
            result &= !isEnabled
                && getParticipantLabel(jid).contains(PERMISSION_NAME);
        }
        return result;
    }

    public boolean hasReadOnlyAccessBy(String... jids) throws RemoteException {
        precondition();
        boolean result = true;
        for (String jid : jids) {
            boolean isEnabled = bot().view(VIEW_SAROS_SESSION).bot().table()
                .getTableItem(jid).contextMenu(CM_RESTRICT_TO_READ_ONLY_ACCESS)
                .isEnabled();
            result &= !isEnabled && jid.contains(PERMISSION_NAME);
        }
        return result;
    }

    public boolean isHost() throws RemoteException {
        precondition();
        String ownLabelsInSessionView = getParticipantLabel(localJID);

        String talbeItem = bot().view(VIEW_SAROS_SESSION).bot().table()
            .getTableItem(0).getText();
        if (ownLabelsInSessionView.equals(talbeItem))
            return true;
        return false;
    }

    public boolean isHost(JID jidOfParticipant) throws RemoteException {
        precondition();
        String participantLabelsInSessionView = getParticipantLabel(jidOfParticipant);
        String talbeItem = bot().view(VIEW_SAROS_SESSION).bot().table()
            .getTableItem(0).getText();
        if (participantLabelsInSessionView.equals(talbeItem))
            return true;
        return false;
    }

    public boolean isParticipant() throws RemoteException {
        precondition();
        return existsParticipant(getJID());
    }

    public boolean isParticipant(JID jid) throws RemoteException {
        precondition();
        return existsParticipant(jid);
    }

    public boolean areParticipants(List<JID> jidOfParticipants)
        throws RemoteException {
        precondition();
        boolean result = true;
        for (JID jid : jidOfParticipants) {
            result &= existsParticipant(jid);
        }
        return result;
    }

    public boolean isFollowing() throws RemoteException {
        JID followedBuddy = getFollowedBuddyJIDNoGUI();
        if (followedBuddy == null)
            return false;
        return isFollowingBuddyNoGUI(followedBuddy.getBase());

    }

    public boolean isFollowingBuddy(JID buddyJID) throws RemoteException {
        // STFBotTableItem tableItem = bot().view(VIEW_SAROS_SESSION).bot_()
        // .table().getTableItem(getParticipantLabel(buddyJID));
        // return tableItem.existsContextMenu(CM_STOP_FOLLOWING_THIS_BUDDY);
        return isFollowingBuddyNoGUI(buddyJID.getBase());
    }

    public String getFirstLabelTextInSessionview() throws RemoteException {
        if (existsLabelInSessionView())
            return bot().view(VIEW_SAROS_SESSION).bot().label().getText();
        return null;
    }

    public String getParticipantLabel(JID participantJID)
        throws RemoteException {
        String contactLabel;
        if (localJID.equals(participantJID)) {
            if (hasWriteAccessNoGUI())
                contactLabel = OWN_PARTICIPANT_NAME;
            else
                contactLabel = OWN_PARTICIPANT_NAME + " " + PERMISSION_NAME;
        } else if (sarosBot().views().buddiesView()
            .hasBuddyNickNameNoGUI(participantJID)) {
            if (hasWriteAccessByNoGUI(participantJID))
                contactLabel = sarosBot().views().buddiesView()
                    .getBuddyNickNameNoGUI(participantJID)
                    + " (" + participantJID.getBase() + ")";
            else
                contactLabel = sarosBot().views().buddiesView()
                    .getBuddyNickNameNoGUI(participantJID)
                    + " ("
                    + participantJID.getBase()
                    + ")"
                    + " "
                    + PERMISSION_NAME;
        } else {
            if (hasWriteAccessByNoGUI(participantJID))
                contactLabel = participantJID.getBase();
            else
                contactLabel = participantJID.getBase() + " " + PERMISSION_NAME;
        }
        return contactLabel;
    }

    public List<String> getAllParticipantsInSessionView()
        throws RemoteException {
        precondition();
        List<String> allParticipantsName = new ArrayList<String>();
        STFBotTable table = bot().view(VIEW_SAROS_SESSION).bot().table();
        for (int i = 0; i < table.rowCount(); i++) {
            allParticipantsName.add(table.getTableItem(i).getText());
        }
        return allParticipantsName;
    }

    public boolean hasWriteAccessNoGUI() throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        return sarosSession.hasWriteAccess();
    }

    public boolean hasWriteAccessByNoGUI(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        User user = sarosSession.getUser(jid);
        if (user == null)
            return false;
        log.debug("isDriver(" + jid.toString() + ") == "
            + sarosSession.getUsersWithWriteAccess().contains(user));
        return sarosSession.getUsersWithWriteAccess().contains(user);
    }

    public boolean haveWriteAccessByNoGUI(List<JID> jids) {
        boolean result = true;
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        for (JID jid : jids) {
            try {
                User user = sarosSession.getUser(jid);
                result &= sarosSession.getUsersWithWriteAccess().contains(user);
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public boolean isInFollowModeNoGUI() throws RemoteException {
        return editorManager.isFollowing();
    }

    public boolean isInSessionNoGUI() {
        log.debug("isInSession() == " + sessionManager.getSarosSession() != null);
        return sessionManager.getSarosSession() != null;
    }

    public boolean isHostNoGUI() throws RemoteException {
        return isHostNoGUI(getJID());
    }

    public boolean isHostNoGUI(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        User user = sarosSession.getUser(jid);
        if (user == null)
            return false;
        final boolean result = user == sarosSession.getHost();
        log.debug("isHost(" + jid.toString() + ") == " + result);
        return result;
    }

    public boolean hasReadOnlyAccessNoGUI() throws RemoteException {
        return hasReadOnlyAccessNoGUI(getJID());
    }

    public boolean hasReadOnlyAccessNoGUI(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        User user = sarosSession.getUser(jid);
        if (user == null)
            return false;
        log.debug("hasReadOnlyAccess(" + jid.toString() + ") == "
            + sarosSession.getUsersWithReadOnlyAccess().contains(user));
        return sarosSession.getUsersWithReadOnlyAccess().contains(user);
    }

    public boolean haveReadOnlyAccessNoGUI(List<JID> jids)
        throws RemoteException {
        boolean result = true;
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        for (JID jid : jids) {
            try {
                User user = sarosSession.getUser(jid);
                result &= sarosSession.getUsersWithReadOnlyAccess().contains(
                    user);
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public boolean isParticipantNoGUI() throws RemoteException {
        return isParticipantNoGUI(getJID());
    }

    public boolean isParticipantNoGUI(JID jid) throws RemoteException {
        try {
            ISarosSession sarosSession = sessionManager.getSarosSession();
            if (sarosSession == null)
                return false;
            User user = sarosSession.getUser(jid);
            if (user == null)
                return false;
            log.debug("isParticipant(" + jid.toString() + ") == "
                + sarosSession.getParticipants().contains(user));
            return sarosSession.getParticipants().contains(user);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areParticipantsNoGUI(List<JID> jids) throws RemoteException {
        boolean result = true;
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        for (JID jid : jids) {
            try {
                result &= sarosSession.getParticipants().contains(
                    sarosSession.getUser(jid));
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public boolean isFollowingBuddyNoGUI(String baseJID) throws RemoteException {
        if (getFollowedBuddyJIDNoGUI() == null)
            return false;
        else
            return getFollowedBuddyJIDNoGUI().getBase().equals(baseJID);
    }

    public JID getJID() throws RemoteException {
        return localJID;
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    protected void precondition() throws RemoteException {
        bot().openViewById(VIEW_SAROS_SESSION_ID);
        bot().view(VIEW_SAROS_SESSION).show();
    }

    private List<String> getToolbarButtons() throws RemoteException {
        return bot().view(VIEW_SAROS_SESSION).getToolTipTextOfToolbarButtons();
    }

    /**
     * @return the JID of the followed user or null if currently no user is
     *         followed.
     * 
     */
    public JID getFollowedBuddyJIDNoGUI() throws RemoteException {
        if (editorManager.getFollowedUser() != null)
            return editorManager.getFollowedUser().getJID();
        else
            return null;
    }

    private boolean isToolbarButtonEnabled(String tooltip)
        throws RemoteException {
        return bot().view(VIEW_SAROS_SESSION)
            .toolbarButtonWithRegex(tooltip + ".*").isEnabled();
    }

}
