package de.fu_berlin.inf.dpp.stf.client.util;

import static de.fu_berlin.inf.dpp.stf.shared.Constants.ACCEPT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.NODE_SAROS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SESSION_INVITATION;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.VIEW_SAROS;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.tester.AbstractTester;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;

public class Util {
    /**
     * Closes the following views:
     * <ol>
     * <li>Problems</li>
     * <li>Javadoc</li>
     * <li>Declaration</li>
     * <li>Task List</li>
     * <li>Outline</li>
     * </ol>
     * 
     * @param tester
     *            the remote tester e.g ALICE
     **/
    public static void closeUnnecessaryViews(AbstractTester tester)
        throws RemoteException {
        if (tester.remoteBot().isViewOpen("Problems"))
            tester.remoteBot().view("Problems").close();

        if (tester.remoteBot().isViewOpen("Javadoc"))
            tester.remoteBot().view("Javadoc").close();

        if (tester.remoteBot().isViewOpen("Declaration"))
            tester.remoteBot().view("Declaration").close();

        if (tester.remoteBot().isViewOpen("Task List"))
            tester.remoteBot().view("Task List").close();

        if (tester.remoteBot().isViewOpen("Outline"))
            tester.remoteBot().view("Outline").close();
    }

    /**
     * Opens the view <b>Saros</b>
     * 
     * @param tester
     *            the remote tester e.g. ALICE
     **/

    public static void openSarosView(AbstractTester tester)
        throws RemoteException {
        if (!tester.remoteBot().isViewOpen(VIEW_SAROS)) {
            tester.superBot().menuBar().window()
                .showViewWithName(NODE_SAROS, VIEW_SAROS);
        }
    }

    /**
     * A convenient function to quickly build a session which share a project
     * and a file. The invitiees are invited concurrently.
     * 
     * @NOTE there is no guarantee the the project and the file are already
     *       shared after this method returns
     * @param projectName
     *            the name of the project
     * @param path
     *            the path of the file e.g. foo/bar/readme.txt
     * @param content
     *            the content of the file
     * @param inviter
     *            e.g. ALICE
     * @param invitees
     *            e.g. BOB, CARL
     * @throws RemoteException
     */
    public static void setUpSessionWithProjectAndFile(String projectName,
        String path, String content, AbstractTester inviter,
        AbstractTester... invitees) throws RemoteException {

        inviter.superBot().internal().createProject(projectName);
        inviter.superBot().internal().createFile(projectName, path, content);
        buildSessionConcurrently(projectName, TypeOfCreateProject.NEW_PROJECT,
            inviter, invitees);
    }

    /**
     * A convenient function to quickly build a session which share a java
     * project with a class. The invitiees are invited concurrently.
     * 
     * @NOTE there is no guarantee the the project and the class are already
     *       shared after this method returns
     * @param projectName
     *            the name of the project
     * @param packageName
     *            the name of the package
     * @param className
     *            the name of the class without .java or .class suffix
     * @param inviter
     *            e.g. ALICE
     * @param invitees
     *            e.g. BOB, CARL
     * @throws RemoteException
     */
    public static void setUpSessionWithJavaProjectAndClass(String projectName,
        String packageName, String className, AbstractTester inviter,
        AbstractTester... invitees) throws RemoteException {

        inviter.superBot().internal().createJavaProject(projectName);
        inviter.superBot().internal()
            .createJavaClass(projectName, packageName, className);

        buildSessionConcurrently(projectName, TypeOfCreateProject.NEW_PROJECT,
            inviter, invitees);
    }

    public static void setUpSessionWithJavaProjects(
        Map<String, List<String>> projectsPkgsClasses, AbstractTester inviter,
        AbstractTester... invitees) {

        throw new UnsupportedOperationException("not yet implemented");
        // List<String> createdProjects = new ArrayList<String>();
        //
        // for (Iterator<String> i = projectsPkgsClasses.keySet().iterator(); i
        // .hasNext();) {
        // String key = i.next();
        // if (!createdProjects.contains(key)) {
        // createdProjects.add(key);
        // inviter.superBot().views().packageExplorerView().tree().newC()
        // .javaProject(key);
        // List<String> pkgAndclass = projectsPkgsClasses.get(key);
        // inviter.superBot().views().packageExplorerView()
        // .selectPkg(key, pkgAndclass.get(0)).newC()
        // .cls(key, pkgAndclass.get(0), pkgAndclass.get(1));
        // }
        // }

        // buildSessionConcurrently(projectName,
        // TypeOfCreateProject.NEW_PROJECT,
        // inviter, invitees);
    }

    /**
     * Creates a project with an empty file for every tester in his workspace.
     * 
     * @param projectName
     *            the name of the project
     * @param path
     *            the path of the file e.g. foo/bar/readme.txt
     * @param testers
     *            e.g. ALICE, CARL
     * @throws RemoteException
     */

    public static void createProjectWithFile(String projectName, String path,
        AbstractTester... testers) throws RemoteException {
        for (AbstractTester tester : testers) {
            tester.superBot().internal().createProject(projectName);
            tester.superBot().internal().createFile(projectName, path, "");
        }
    }

    public static void addProjectToSessionSequentially(String projectName,
        TypeOfCreateProject usingWhichProject, AbstractTester inviter,
        AbstractTester... invitees) throws RemoteException {

        inviter.superBot().menuBar().saros().addProjects(projectName);

        for (AbstractTester invitee : invitees) {
            invitee.superBot().confirmShellAddProjectUsingWhichProject(
                projectName, usingWhichProject);
        }
    }

    public static void buildSessionSequentially(String projectName,
        TypeOfCreateProject usingWhichProject, AbstractTester inviter,
        AbstractTester... invitees) throws RemoteException {
        JID[] inviteesJID = getPeerJID(invitees);
        inviter.superBot().menuBar().saros()
            .shareProjects(projectName, inviteesJID);
        for (AbstractTester invitee : invitees) {
            invitee.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);
            invitee.superBot().confirmShellAddProjectUsingWhichProject(
                projectName, usingWhichProject);
        }
    }

    public static void buildSessionConcurrently(final String projectName,
        final TypeOfCreateProject usingWhichProject, AbstractTester inviter,
        AbstractTester... invitees) throws RemoteException {

        inviter.superBot().menuBar().saros()
            .shareProjects(projectName, Util.getPeerJID(invitees));

        List<Callable<Void>> joinSessionTasks = new ArrayList<Callable<Void>>();
        for (final AbstractTester invitee : invitees) {
            joinSessionTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    invitee.superBot()
                        .confirmShellSessionInvitationAndShellAddProject(
                            projectName, usingWhichProject);
                    return null;
                }
            });
        }

        workAll(joinSessionTasks);
    }

    /**
     * the local user can be concurrently followed by many other users.
     * 
     * @param buddiesTofollow
     *            the list of the buddies who want to follow the local user.
     * @throws RemoteException
     */
    public static void setFollowMode(final AbstractTester followedBuddy,
        AbstractTester... buddiesTofollow) throws RemoteException {
        List<Callable<Void>> followTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < buddiesTofollow.length; i++) {
            final AbstractTester tester = buddiesTofollow[i];
            followTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    tester.superBot().views().sarosView()
                        .selectParticipant(followedBuddy.getJID())
                        .followParticipant();
                    return null;
                }
            });
        }
        workAll(followTasks);
    }

    /**
     * Adds buddies to the contact list of the tester. All buddies will have the
     * tester added to their contact list as well.
     * 
     * @param tester
     *            the tester who wants to add buddies to his contact list eg.
     *            ALICE
     * 
     * @param buddies
     *            the buddies to add, eg. BOB, CARL
     * @throws RemoteException
     * 
     * 
     */
    public static void addBuddiesToContactList(AbstractTester tester,
        AbstractTester... buddies) throws RemoteException {
        for (AbstractTester peer : buddies) {
            if (!tester.superBot().views().sarosView().hasBuddy(peer.getJID())) {
                tester.superBot().views().sarosView()
                    .addNewBuddy(peer.getJID());
                peer.superBot().confirmShellRequestOfSubscriptionReceived();
            }
        }
    }

    /**
     * Removes the given buddies from the contact list of the tester. All
     * buddies will have the tester removed from their contact list as well.
     * 
     * @param tester
     *            the tester who wants to add buddies to his contact list eg.
     *            ALICE
     * 
     * @param buddies
     *            the buddies to add, eg. BOB, CARL
     * @throws RemoteException
     * 
     * 
     */
    public static void removeBuddiesFromContactList(AbstractTester tester,
        AbstractTester... buddies) throws RemoteException {
        for (AbstractTester deletedBuddy : buddies) {
            if (!tester.superBot().views().sarosView()
                .hasBuddy(deletedBuddy.getJID()))
                continue;
            tester.superBot().views().sarosView()
                .selectBuddy(deletedBuddy.getJID()).delete();
            deletedBuddy.superBot().confirmShellRemovelOfSubscription();
        }

    }

    /*
     * public static void shareYourScreen(AbstractTester buddy, AbstractTester
     * selectedBuddy) throws RemoteException {
     * buddy.superBot().views().sarosView()
     * .shareYourScreenWithSelectedBuddy(selectedBuddy.getJID());
     * selectedBuddy.remoteBot().waitUntilShellIsOpen(
     * SHELL_INCOMING_SCREENSHARING_SESSION);
     * selectedBuddy.remoteBot().shell(SHELL_INCOMING_SCREENSHARING_SESSION)
     * .bot().button(YES).click(); }
     */

    /**
     * This method is same as
     * 
     * . The difference to buildSessionConcurrently is that the invitation
     * process is activated by clicking the toolbarbutton
     * "open invitation interface" in the roster view.
     * 
     * @param projectName
     *            the name of the project which is in a session now.
     * @param invitees
     *            the user whom you want to invite to your session.
     * @throws RemoteException
     */
    public static void inviteBuddies(final String projectName,
        final TypeOfCreateProject usingWhichProject, AbstractTester inviter,
        AbstractTester... invitees) throws RemoteException {
        inviter.superBot().menuBar().saros()
            .addBuddies(Util.getPeersBaseJID(invitees));
        List<Callable<Void>> joinSessionTasks = new ArrayList<Callable<Void>>();
        for (final AbstractTester tester : invitees) {
            joinSessionTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    tester.remoteBot().shell(SHELL_SESSION_INVITATION)
                        .confirm(ACCEPT);
                    tester.superBot().confirmShellAddProjectUsingWhichProject(
                        projectName, usingWhichProject);
                    return null;
                }
            });
        }

        Util.workAll(joinSessionTasks);
    }

    public static String[] getPeersBaseJID(AbstractTester... peers) {
        String[] peerBaseJIDs = new String[peers.length];
        for (int i = 0; i < peers.length; i++) {
            peerBaseJIDs[i] = peers[i].getBaseJid();
        }
        return peerBaseJIDs;
    }

    public static JID[] getPeerJID(AbstractTester... peers) {
        JID[] peerBaseJIDs = new JID[peers.length];
        for (int i = 0; i < peers.length; i++) {
            peerBaseJIDs[i] = peers[i].getJID();
        }
        return peerBaseJIDs;
    }

    public static void resetWriteAccess(AbstractTester host,
        AbstractTester... invitees) throws RemoteException {
        for (AbstractTester tester : invitees) {
            if (tester.superBot().views().sarosView().isInSession()
                && host.superBot().views().sarosView()
                    .selectParticipant(tester.getJID()).hasReadOnlyAccess()) {
                host.superBot().views().sarosView()
                    .selectParticipant(tester.getJID()).grantWriteAccess();
            }
        }
    }

    public static void resetFollowModeSequentially(
        AbstractTester... buddiesFollowing) throws RemoteException {
        for (AbstractTester tester : buddiesFollowing) {
            if (tester.superBot().views().sarosView().isInSession()
                && tester.superBot().views().sarosView().isFollowing()) {
                JID followedBuddyJID = tester.superBot().views().sarosView()
                    .getFollowedBuddy();
                tester.superBot().views().sarosView()
                    .selectParticipant(followedBuddyJID).stopFollowing();
            }
        }
    }

    /**
     * stop the follow-mode of the buddies who are following the local user.
     * 
     * @param buddiesFollowing
     *            the list of the buddies who are following the local user.
     * @throws RemoteException
     */
    public static void resetFollowModeConcurrently(
        AbstractTester... buddiesFollowing) throws RemoteException {
        List<Callable<Void>> stopFollowTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < buddiesFollowing.length; i++) {
            final AbstractTester tester = buddiesFollowing[i];
            stopFollowTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    JID followedBuddyJID = tester.superBot().views()
                        .sarosView().getFollowedBuddy();
                    tester.superBot().views().sarosView()
                        .selectParticipant(followedBuddyJID).stopFollowing();
                    return null;
                }
            });
        }
        workAll(stopFollowTasks);
    }

    /**********************************************
     * 
     * often used convenient functions
     * 
     **********************************************/

    public static void reBuildSession(String projectName, AbstractTester host,
        AbstractTester... invitees) throws RemoteException {
        for (AbstractTester tester : invitees)
            if (!tester.superBot().views().sarosView().isInSession())
                tester.superBot().views().sarosView()
                    .connectWith(tester.getJID(), tester.getPassword());

        if (!host.superBot().views().sarosView().isInSession()) {
            host.superBot().views().sarosView()
                .connectWith(host.getJID(), host.getPassword());
            for (AbstractTester tester : invitees) {
                buildSessionSequentially(projectName,
                    TypeOfCreateProject.EXIST_PROJECT, host, tester);
            }
        }
    }

    public static String getClassPath(String projectName, String pkg,
        String className) {
        return projectName + "/src/" + pkg.replace('.', '/') + "/" + className
            + ".java";
    }

    public static <T> List<T> workAll(List<Callable<T>> tasks) {
        if (System.getProperty("os.name").matches("Mac OS X.*"))
            // the menubar is only active on Mac OS on the Window that has the
            // current focus
            return workAll(tasks, 1);
        else
            return workAll(tasks, tasks.size());
    }

    public static <T> List<T> workAll(List<Callable<T>> tasks,
        int numberOfThreads) {

        ExecutorService pool = Executors.newFixedThreadPool(numberOfThreads);

        try {

            List<T> result = new ArrayList<T>();

            for (Future<T> future : pool.invokeAll(tasks))
                result.add(future.get());

            return result;

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            pool.shutdown();
        }
    }

}
