package de.fu_berlin.inf.dpp.stf.client.util;

import static de.fu_berlin.inf.dpp.stf.shared.Constants.ACCEPT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.NODE_SAROS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SESSION_INVITATION;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SRC;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SUFFIX_JAVA;
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
     *            the remote tester e.g: Alice
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
     * Deletes all projects from the current active workspace
     * 
     * @param tester
     *            the remote tester e.g: Alice
     * 
     * @WARNING calling this method while still <b>in a session</b> can result
     *          in <b>unexpected</b> behavior
     **/
    public static void deleteAllProjects(AbstractTester tester)
        throws RemoteException {
        tester.remoteBot().closeAllEditors();
        List<String> treeItems = tester.superBot().views()
            .packageExplorerView().tree().getTextOfTreeItems();
        for (String treeItem : treeItems) {
            tester.superBot().views().packageExplorerView()
                .selectProject(treeItem).delete();
        }

    }

    /**
     * Opens the view <b>Saros</b>
     * 
     * @param tester
     *            the remote tester e.g: Alice
     **/

    public static void openSarosViews(AbstractTester tester)
        throws RemoteException {
        if (!tester.remoteBot().isViewOpen(VIEW_SAROS)) {
            tester.superBot().menuBar().window()
                .showViewWithName(NODE_SAROS, VIEW_SAROS);
        }
    }

    /**
     * A convenient function to quickly build a session which share a java
     * project with a class.
     * 
     * @param projectName
     *            the name of the project
     * @param packageName
     *            the name of the package
     * @param className
     *            the name of the class without .java suffix
     * @param inviter
     * @param invitees
     * @throws RemoteException
     */
    public static void setUpSessionWithAJavaProjectAndAClass(
        String projectName, String packageName, String className,
        AbstractTester inviter, AbstractTester... invitees)
        throws RemoteException {

        inviter.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(projectName, packageName, className);

        buildSessionConcurrently(projectName, TypeOfCreateProject.NEW_PROJECT,
            inviter, invitees);
    }

    public static void setUpSessionWithJavaProjects(
        Map<String, List<String>> projectsPkgsClasses, AbstractTester inviter,
        AbstractTester... invitees) throws RemoteException {

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

    public static void createProjectWithFileBy(String projectName,
        String fileName, AbstractTester... testers) throws RemoteException {
        for (AbstractTester tester : testers) {
            tester.superBot().views().packageExplorerView().tree().newC()
                .project(projectName);
            tester.superBot().views().packageExplorerView()
                .selectFolder(projectName).newC().file(fileName);
            tester.remoteBot().waitUntilEditorOpen(fileName);
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
     * add buddy with GUI, which should be performed by both the users.
     * 
     * @param peers
     *            the user, which should be added in your contact list
     * @throws RemoteException
     * 
     * 
     */
    public static void addBuddies(AbstractTester host, AbstractTester... peers)
        throws RemoteException {
        for (AbstractTester peer : peers) {
            if (!host.superBot().views().sarosView().hasBuddy(peer.getJID())) {
                host.superBot().views().sarosView().addNewBuddy(peer.getJID());
                peer.superBot().confirmShellRequestOfSubscriptionReceived();
            }
        }
    }

    /**
     * Remove given contact from Roster with GUI, if contact was added before.
     */
    public static void deleteBuddies(AbstractTester buddy,
        AbstractTester... deletedBuddies) throws RemoteException {
        for (AbstractTester deletedBuddy : deletedBuddies) {
            if (!buddy.superBot().views().sarosView()
                .hasBuddy(deletedBuddy.getJID()))
                return;
            buddy.superBot().views().sarosView()
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
     * @throws InterruptedException
     */
    public static void inviteBuddies(final String projectName,
        final TypeOfCreateProject usingWhichProject, AbstractTester inviter,
        AbstractTester... invitees) throws RemoteException,
        InterruptedException {
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

    /**********************************************
     * 
     * inner functions
     * 
     **********************************************/

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
     * @throws InterruptedException
     */
    public static void resetFollowModeConcurrently(
        AbstractTester... buddiesFollowing) throws RemoteException,
        InterruptedException {
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
        if (!host.superBot().views().sarosView().isInSession()) {
            for (AbstractTester tester : invitees) {
                buildSessionSequentially(projectName,
                    TypeOfCreateProject.EXIST_PROJECT, host, tester);
            }
        }
    }

    public static String getClassPath(String projectName, String pkg,
        String className) {
        return projectName + "/src/" + pkg.replaceAll("\\.", "/") + "/"
            + className + ".java";
    }

    public static String changeToRegex(String text) {
        // the name of project in SVN_control contains special characters, which
        // should be filtered.
        String[] names = text.split(" ");
        if (names.length > 1) {
            text = names[0];
        }
        return text + ".*";
    }

    public static String[] changeToRegex(String... texts) {
        String[] matchTexts = new String[texts.length];
        for (int i = 0; i < texts.length; i++) {
            matchTexts[i] = texts[i] + "( .*)?";
        }
        return matchTexts;
    }

    public static String[] getClassNodes(String projectName, String pkg,
        String className) {
        String[] nodes = { projectName, SRC, pkg, className + SUFFIX_JAVA };
        return nodes;
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
