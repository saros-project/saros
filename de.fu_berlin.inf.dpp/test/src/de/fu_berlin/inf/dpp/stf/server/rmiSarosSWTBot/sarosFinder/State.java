package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.project.SharedProject;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.SessionView;

public interface State extends Remote {

    /**
     * 
     * @param prjectName
     *            the name of the project
     * @return <tt>true</tt>, if the given project is under SVN control
     * @throws RemoteException
     */
    public boolean isProjectManagedBySVN(String prjectName)
        throws RemoteException;

    /**
     * 
     * @param fullPath
     *            the full path of the local resource, e.g.
     *            "example_project/src/org/eclipsecon/swtbot/example/MyFirstTest01.java"
     * @return the revision id of the given resource.
     * @throws RemoteException
     */
    public String getRevision(String fullPath) throws RemoteException;

    /**
     * @param fullPath
     *            the full path of the local resource, e.g.
     *            "example_project/src/org/eclipsecon/swtbot/example/MyFirstTest01.java"
     * 
     * @return the VCS specific URL information for the given resource specified
     *         by the passed parameter"fullPath".
     * @throws RemoteException
     */
    public String getURLOfRemoteResource(String fullPath)
        throws RemoteException;

    /**
     * 
     * @param jid
     *            a JID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @return <tt>true</tt> if the acount specified by the given jid is active
     * @throws RemoteException
     * @see XMPPAccount#isActive()
     */
    public boolean isAccountActiveNoGUI(JID jid) throws RemoteException;

    /**
     * @param jid
     *            a JID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @return <tt>true</tt> if the account specified by the given jid and
     *         password exists in preference store
     * @throws RemoteException
     * @see XMPPAccountStore#getAllAccounts()
     */
    public boolean isAccountExistNoGUI(JID jid, String password)
        throws RemoteException;

    /**
     * 
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","myFolder", "myFile.xml"}
     * @return <tt>true</tt>, if the file specified by the node array parameter
     *         exists
     * @throws RemoteException
     */
    // public boolean existsFile(String viewTitle, String... fileNodes)
    // throws RemoteException;

    /**
     * @param folderNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}
     * @return <tt>true</tt>, if the given folder already exists.
     */
    // public boolean existsFolderNoGUI(String... folderNodes)
    // throws RemoteException;

    /**
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @return <tt>true</tt>, if the given project exists
     */
    // public boolean existsProjectNoGUI(String projectName)
    // throws RemoteException;

    /**
     * 
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @return <tt>true</tt>, if the specified package already exists.
     * @throws RemoteException
     */
    // public boolean existsPkgNoGUI(String projectName, String pkg)
    // throws RemoteException;

    /**
     * @param filePath
     *            path of the file, e.g. "Foo_Saros/myFolder/myFile.xml" or path
     *            of a class file, e.g. "Foo_Saros/src/my/pkg/myClass.java
     * @return <tt>true</tt>, if the file specified by the passed parameter
     *         "filePath" exists.
     */
    // public boolean existsFileNoGUI(String filePath) throws RemoteException;

    /**
     * 
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder", "myFile.xml"}
     * @return<tt>true</tt>, if the file specified by the passed array parameter
     *                       exists.
     * @throws RemoteException
     */
    // public boolean existsFileNoGUI(String... fileNodes) throws
    // RemoteException;

    /**
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * @return <tt>true</tt>, if the class file specified by the passed
     *         parameters exists.
     * @throws RemoteException
     */
    // public boolean existsClassNoGUI(String projectName, String pkg,
    // String className) throws RemoteException;

    /**********************************************
     * 
     * States
     * 
     **********************************************/
    /**
     * 
     * @return<tt>true</tt>, if you have write access.
     * @throws RemoteException
     */
    public boolean hasWriteAccess() throws RemoteException;

    /**
     * 
     * @param buddiesJIDs
     *            the list of buddies'JIDs, which write access would be checked
     * @return<tt>true</tt>, if the given buddies have write access.
     * @throws RemoteException
     */
    public boolean hasWriteAccessBy(JID... buddiesJIDs) throws RemoteException;

    public boolean hasWriteAccessBy(String... tableItemTexts)
        throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, if the you have read only access.
     * @throws RemoteException
     */
    public boolean hasReadOnlyAccess() throws RemoteException;

    /**
     * 
     * @param buddiesJIDs
     *            the list of buddies'JIDs, which read access would be checked
     * @return<tt>true</tt>, if the context menu "Restrict to read only access"
     *                       isn't enabled by given participants
     * @throws RemoteException
     */
    public boolean hasReadOnlyAccessBy(JID... buddiesJIDs)
        throws RemoteException;

    public boolean hasReadOnlyAccessBy(String... jids) throws RemoteException;

    /**
     * Test if you are now in a session. <br>
     * This function check if the tool bar button "Leave the session" in the
     * session view is enabled. You can also use another function
     * {@link SessionView#isInSessionNoGUI()}, which test the session state
     * without GUI.
     * 
     * 
     * @return <tt>true</tt> if the tool bar button "Leave the session" is
     *         enabled.
     * 
     * @throws RemoteException
     */
    public boolean isInSession() throws RemoteException;

    /**
     * Test if a participant
     * 
     * 
     * exists in the contact list in the session view.
     * 
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * </ol>
     * 
     * @param contactJID
     *            JID of a contact whose nick name listed in the session view.
     *            e.g. "You" or "bob_stf@saros-con.imp.fu-berlin.de" or
     *            "bob_stf@saros-con.imp.fu-berlin.de" or
     *            "(nickNameOfBob (bob_stf@saros-con.imp.fu-berlin.de)" .
     * @return <tt>true</tt> if the passed contactName is in the contact list in
     *         the session view.
     * @throws RemoteException
     */
    public boolean existsParticipant(JID contactJID) throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, if name of the first tableItem in the saros session
     *                       view is equal with the label infos of the local
     *                       user.
     * @throws RemoteException
     */
    public boolean isHost() throws RemoteException;

    /**
     * 
     * @param jidOfParticipant
     * @return<tt>true</tt>, if name of the first tableItem in the saros session
     *                       view is equal with the label infos of the given
     *                       participant.
     * @throws RemoteException
     */
    public boolean isHost(JID jidOfParticipant) throws RemoteException;

    /**
     * 
     * @return <tt>true</tt>, if the local user is in the session view
     * @throws RemoteException
     */
    public boolean isParticipant() throws RemoteException;

    /**
     * @return <tt>true</tt>, if the participant with the given JID exists in
     *         the session view
     * 
     */
    public boolean isParticipant(JID jid) throws RemoteException;

    /**
     * 
     * @param jidOfParticipants
     * @return <tt>true</tt>, if the participants with the given JIDs exist in
     *         the session view
     * @throws RemoteException
     */
    public boolean areParticipants(List<JID> jidOfParticipants)
        throws RemoteException;

    public boolean isFollowing() throws RemoteException;

    public boolean isFollowingBuddy(JID buddyJID) throws RemoteException;

    /**
     * @param contactJID
     *            the JID of a user, who is sharing a session with you.
     * @return the text of the table item specified with the given parameter
     *         "contactJID" which listed in the session view.
     * @throws RemoteException
     */
    public String getParticipantLabel(JID contactJID) throws RemoteException;

    /**
     * 
     * It get all participant names listed in the session view
     * 
     * @return list, which contain all the contact names.
     * @throws RemoteException
     */
    public List<String> getAllParticipantsInSessionView()
        throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, if there are some label texts existed in the
     *                       session view. You can only see the label texts when
     *                       you are not in a session.
     * @throws RemoteException
     */
    public boolean existsLabelInSessionView() throws RemoteException;

    /**
     * @return the first label text on the session view, which should be showed
     *         if there are no session.
     * @throws RemoteException
     */
    public String getFirstLabelTextInSessionview() throws RemoteException;

    /**
     * Test if you are now in a session. <br>
     * You can also use another function {@link SessionView#isInSession()} ,
     * which test the session state with GUI.
     * 
     * <p>
     * <b>Attention:</b> <br>
     * Try to use the {@link SessionView#isInSession()} and
     * {@link SarosState#isInSessionNoGUI()} together in your junittests.
     * 
     * 
     * @return <tt>true</tt> if {@link SarosSessionManager#getSarosSession()} is
     *         not null.
     * 
     * @throws RemoteException
     * @see SarosSessionManager#getSarosSession()
     */
    public boolean isInSessionNoGUI() throws RemoteException;

    /**
     * @return <tt>true</tt> if the local client has
     *         {@link User.Permission#WRITE_ACCESS} of this shared project.
     *         false otherwise.
     * @throws RemoteException
     */
    public boolean hasWriteAccessNoGUI() throws RemoteException;

    /**
     * @return <tt>true</tt> if the given {@link JID} has
     *         {@link User.Permission#WRITE_ACCESS} in this
     *         {@link SharedProject}.
     * @throws RemoteException
     */
    public boolean hasWriteAccessByNoGUI(JID jid) throws RemoteException;

    /**
     * @return <tt>true</tt>, if all given {@link JID}s have
     *         {@link User.Permission#WRITE_ACCESS} for this
     *         {@link SharedProject}.
     * @throws RemoteException
     */
    public boolean haveWriteAccessByNoGUI(List<JID> jids)
        throws RemoteException;

    /**
     * 
     * @return <tt>true</tt>, if the local user is a host.
     * @throws RemoteException
     */
    public boolean isHostNoGUI() throws RemoteException;

    /**
     * 
     * @param jid
     *            the JID of the user.
     * @return <tt>true</tt>, if the user specified by the given jid is a host.
     * @throws RemoteException
     */
    public boolean isHostNoGUI(JID jid) throws RemoteException;

    /**
     * @return <tt>true</tt>, if the local user has read-only access in this
     *         {@link SharedProject}.
     */
    public boolean hasReadOnlyAccessNoGUI() throws RemoteException;

    /**
     * @return <tt>true</tt>, if the given {@link JID} has read-only access in
     *         this {@link SharedProject}.
     */
    public boolean hasReadOnlyAccessNoGUI(JID jid) throws RemoteException;

    /**
     * @return <tt>true</tt>, if all given {@link JID} have read-only of the
     *         project.
     */
    public boolean haveReadOnlyAccessNoGUI(List<JID> jids)
        throws RemoteException;

    /**
     * 
     * @return <tt>true</tt>, if the local user is a participant of the
     *         {@link SharedProject}.
     * @throws RemoteException
     */
    public boolean isParticipantNoGUI() throws RemoteException;

    /**
     * @return <tt>true</tt>, if the given {@link JID} is a participant of the
     *         {@link SharedProject}.
     */
    public boolean isParticipantNoGUI(JID jid) throws RemoteException;

    /**
     * @return <tt>true</tt>, if all given jids are participants of the project.
     */
    public boolean areParticipantsNoGUI(List<JID> jids) throws RemoteException;

    /**
     * @return <tt>true</tt>, if you are currently following another user.
     * @throws RemoteException
     * @see EditorManager#isFollowing
     */
    public boolean isInFollowModeNoGUI() throws RemoteException;

    /**
     * 
     * @param baseJID
     *            the baseJID of the user, whom you are currently following
     * @return <tt>true</tt>, if you are currently following the given user.
     * @throws RemoteException
     */
    public boolean isFollowingBuddyNoGUI(String baseJID) throws RemoteException;

    /**
     * 
     * @return the JID of the local user
     * @throws RemoteException
     */
    public JID getJID() throws RemoteException;

    /**
     * @return the JID of the followed user or null if currently no user is
     *         followed.
     * 
     */
    public JID getFollowedBuddyJIDNoGUI() throws RemoteException;
}
