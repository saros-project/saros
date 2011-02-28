package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder;

import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.SarosComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.SessionView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.SessionViewImp;

public interface Wait extends SarosComponent {

    /**
     * waits until the window with the title "Saros running VCS operation" is
     * closed
     * 
     * @throws RemoteException
     */
    public void waitUntilWindowSarosRunningVCSOperationClosed()
        throws RemoteException;

    /**
     * waits until the given project is in SVN control
     * 
     * @param projectName
     *            the name of the project
     * @throws RemoteException
     */
    public void waitUntilProjectInSVN(String projectName)
        throws RemoteException;

    /**
     * waits until the given project is not under SVN control
     * 
     * @param projectName
     *            the name of the project
     * @throws RemoteException
     */
    public void waitUntilProjectNotInSVN(String projectName)
        throws RemoteException;

    /**
     * 
     * @param fullPath
     *            the full path of the local resource, e.g.
     *            "example_project/src/org/eclipsecon/swtbot/example/MyFirstTest01.java"
     * @param revisionID
     *            the expected revision.
     * @throws RemoteException
     */
    public void waitUntilRevisionIsSame(String fullPath, String revisionID)
        throws RemoteException;

    /**
     * 
     * @param fullPath
     *            the full path of the local resource, e.g.
     *            "example_project/src/org/eclipsecon/swtbot/example/MyFirstTest01.java"
     * @param url
     *            the expected URL of the remote resource, e.g.
     *            "http://myhost.com/svn/trunk/.../MyFirstTest01.java".
     * @throws RemoteException
     */
    public void waitUntilUrlIsSame(String fullPath, String url)
        throws RemoteException;

    /**
     * Wait until the specified folder exists
     * 
     * @param folderNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}
     */
    public void waitUntilFolderExists(String... folderNodes)
        throws RemoteException;

    /**
     * wait until the given package exists
     * 
     * @param projectName
     *            name of the java project, e.g. Foo-Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @throws RemoteException
     */
    public void waitUntilPkgExists(String projectName, String pkg)
        throws RemoteException;

    /**
     * Wait until the given package not exists. This method would be used, if
     * you want to check if a shared package exists or not which is deleted by
     * another session_participant.
     * 
     * @param projectName
     *            name of the java project, e.g. Foo-Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @throws RemoteException
     */
    public void waitUntilPkgNotExists(String projectName, String pkg)
        throws RemoteException;

    /**
     * 
     * Wait until the specified class exists. This method would be used, if you
     * want to check if a shared class exists or not which is created by another
     * session_participant.
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * @throws RemoteException
     */
    public void waitUntilClassExists(String projectName, String pkg,
        String className) throws RemoteException;

    /**
     * Wait until the specified class not exists.This method would be used, if
     * you want to check if a shared class exists or not which is deleted by
     * another session_participant.
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * @throws RemoteException
     */
    public void waitUntilClassNotExists(String projectName, String pkg,
        String className) throws RemoteException;

    /**
     * Wait until the file exists.
     * 
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g. {"Foo-saros",
     *            "myFolder", "myFile.xml"}
     */
    public void waitUntilFileExists(String... fileNodes) throws RemoteException;

    public void waitUntilIsInconsistencyDetected() throws RemoteException;

    /**
     * waits until the session is open.
     * 
     * @throws RemoteException
     * @see SarosConditions#isInSession(SarosState)
     */
    public void waitUntilIsInSession() throws RemoteException;

    /**
     * waits until the session by the defined peer is open.
     * 
     * @param sessionV
     *            the {@link SessionView} of the defined peer.
     * @throws RemoteException
     */
    public void waitUntilIsInviteeInSession(SarosBot sarosBot)
        throws RemoteException;

    /**
     * Waits until the {@link SarosSessionManager#getSarosSession()} is null.
     * <p>
     * <b>Attention</b>:<br/>
     * After a action is performed, you immediately try to assert a condition is
     * true/false or perform a following action which based on that the current
     * performed action is completely finished, e.g. alice.state.isInSession is
     * false after alice leave the session by running the
     * {@link SessionViewImp#leaveTheSession()} and confirming the appeared pop
     * up window without this waitUntil. In this case, you may get the
     * AssertException, because alice should not really leave the session yet
     * during asserting the condition or performing a following action. So it is
     * recommended that you wait until the session is completely closed before
     * you run the assertion or perform a following action.
     * 
     * @throws RemoteException
     */
    public void waitUntilIsNotInSession() throws RemoteException;

    /**
     * Waits until the {@link SarosSessionManager#getSarosSession()} is null.
     * <p>
     * <b>Attention</b>:<br/>
     * After a action is performed, you immediately try to assert a condition is
     * true/false or perform a following action which based on that the current
     * performed action is completely finished, e.g.
     * assertFalse(alice.state.hasWriteAccess(bob.jid)) after bob leave the
     * session by running the {@link SessionViewImp#leaveTheSession()} and
     * confirming the appeared pop up window without this waitUntil. In this
     * case, you may get the AssertException, because bob should not really
     * leave the session yet during asserting a condition or performing a
     * following action. So it is recommended that you wait until the session by
     * the defined peer is completely closed before you run a assertion or
     * perform a following action.
     * 
     * @param sessionV
     *            the {@link SessionView} of the user, whose session should be
     *            closed.
     * @throws RemoteException
     */
    public void waitUntilIsInviteeNotInSession(SarosBot sarosBot)
        throws RemoteException;

    /**
     * waits until the local user has {@link User.Permission#WRITE_ACCESS} after
     * host grants him {@link User.Permission#WRITE_ACCESS}. This method should
     * be used after performing the action
     * {@link SessionView#grantWriteAccess(SessionView)} to guarantee the
     * invitee has really got {@link User.Permission#WRITE_ACCESS}.
     * 
     * @throws RemoteException
     */
    public void waitUntilHasWriteAccess() throws RemoteException;

    /**
     * waits until the given user has {@link User.Permission#WRITE_ACCESS} after
     * host grant him {@link User.Permission#WRITE_ACCESS}. This method should
     * be used after performing the action
     * {@link SessionView#grantWriteAccess(SessionView)} to guarantee the
     * invitee has really got the {@link User.Permission#WRITE_ACCESS}.
     * 
     * @throws RemoteException
     */
    public void waitUntilHasWriteAccessBy(final JID jid) throws RemoteException;

    public void waitUntilHasWriteAccessBy(final String tableItemText)
        throws RemoteException;

    /**
     * waits until the local user has no more
     * {@link User.Permission#WRITE_ACCESS} after host has
     * {@link User.Permission#READONLY_ACCESS}. This method should be used after
     * performing the action
     * {@link SessionView#restrictToReadOnlyAccess(SessionView)} or
     * {@link SessionView#restrictInviteesToReadOnlyAccess()} to guarantee the
     * invitee's {@link User.Permission#WRITE_ACCESS} is really removed
     * 
     * @throws RemoteException
     */
    public void waitUntilHasReadOnlyAccess() throws RemoteException;

    public void waitUntilHasReadOnlyAccessBy(final JID jid)
        throws RemoteException;

    public void waitUntilHasReadOnlyAccessBy(final String tableItemText)
        throws RemoteException;

    public void waitUntilIsFollowingBuddy(final JID followedBuddyJID)
        throws RemoteException;

    public void waitUntilIsNotFollowingBuddy(final JID foolowedBuddyJID)
        throws RemoteException;

    public void waitUntilAllPeersLeaveSession(
        final List<JID> jidsOfAllParticipants) throws RemoteException;

}
