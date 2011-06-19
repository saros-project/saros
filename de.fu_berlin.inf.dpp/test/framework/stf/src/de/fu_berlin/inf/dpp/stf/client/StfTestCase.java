package de.fu_berlin.inf.dpp.stf.client;

import static de.fu_berlin.inf.dpp.stf.shared.Constants.TB_STOP_SESSION;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.VIEW_SAROS;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.tester.AbstractTester;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteWorkbenchBot;

public abstract class StfTestCase {

    @Rule
    public TestName currentTestName = new TestName();

    private static List<AbstractTester> currentTesters = new ArrayList<AbstractTester>();

    public static void initTesters(AbstractTester tester,
        AbstractTester... testers) {
        currentTesters.clear();
        currentTesters.add(tester);
        currentTesters.addAll(Arrays.asList(testers));

        for (AbstractTester t : currentTesters)
            if (t == null)
                throw new NullPointerException(
                    "got null reference for a tester object");
    }

    public static List<AbstractTester> getCurrentTesters() {
        return Collections.unmodifiableList(currentTesters);
    }

    /**********************************************
     * 
     * Before/After conditions
     * 
     * @throws RemoteException
     * 
     **********************************************/

    @Before
    public void before() throws RemoteException {
        announceTestCaseStart();
    }

    @After
    public void after() throws RemoteException {
        announceTestCaseEnd();
        resetWorkbenches();
    }

    @AfterClass
    public static void afterClass() throws RemoteException {
        resetSaros();
    }

    public void announceTestCaseStart() throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            tester.remoteBot().logMessage(
                "******* " + "STARTING TESTCASE " + this.getClass().getName()
                    + ":" + currentTestName.getMethodName() + " *******");
        }
    }

    public void announceTestCaseEnd() throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            tester.remoteBot().logMessage(
                "******* " + "ENDING TESTCASE " + this.getClass().getName()
                    + ":" + currentTestName.getMethodName() + " *******");
        }
    }

    /**
     * bring workbench to a original state before beginning your tests
     * <ul>
     * <li>activate saros-instance workbench</li>
     * <li>close all opened popUp windows</li>
     * <li>close all opened editors</li>
     * <li>delete all existed projects</li>
     * <li>close welcome view, if it is open</li>
     * <li>open java perspective</li>
     * <li>close all unnecessary views</li>
     * </ul>
     * 
     * @throws RemoteException
     */
    public static void setUpWorkbench() throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            tester.remoteBot().activateWorkbench();
            if (tester.remoteBot().isViewOpen("Welcome"))
                tester.remoteBot().view("Welcome").close();
            tester.superBot().menuBar().window().openPerspective();
            Util.closeUnnecessaryViews(tester);
            tester.remoteBot().resetWorkbench();
            Util.deleteAllProjects(tester);

        }
    }

    /**
     * bring Saros to a original state before beginning your tests
     * <ul>
     * <li>make automaticReminder disable</li>
     * <li>open sarosViews</li>
     * <li>connect</li>
     * <li>check buddy lists, if all active testers are in contact</li>
     * </ul>
     * 
     * @throws RemoteException
     */
    public static void setUpSaros() throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            tester.superBot().menuBar().saros().preferences()
                .disableAutomaticReminder();
            Util.openSarosViews(tester);
            tester.superBot().views().sarosView()
                .connectWith(tester.getJID(), tester.getPassword());
            resetBuddies(tester);
        }
    }

    /**
     * Tries to reset Saros to a stable state for the given tester(s). It does
     * that be performing the following actions
     * <ol>
     * <li>reset all buddy names by calling
     * {@linkplain #resetBuddyNames(AbstractTester)}</li>
     * <li>disconnect from the current session</li>
     * <li>delete all projects by calling
     * {@linkplain Util#deleteAllProjects(AbstractTester)}</li>
     * </ol>
     * 
     * 
     * @throws RemoteException
     */
    public static void resetSaros() throws RemoteException {
        try {
            for (AbstractTester tester : currentTesters) {
                resetBuddyNames(tester);
                tester.superBot().views().sarosView().disconnect();
                Util.deleteAllProjects(tester);
            }
        } finally {
            currentTesters.clear();
        }

    }

    /**
     * Resets the workbench for every active tester to their original state
     * 
     * @see IRemoteWorkbenchBot#resetWorkbench()
     * 
     * @throws RemoteException
     */

    public static void resetWorkbenches() throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            tester.remoteBot().resetWorkbench();
        }
    }

    /**
     * Closes all shells for all active testers
     * 
     * @see IRemoteWorkbenchBot#closeAllShells()
     * 
     * @throws RemoteException
     */

    public static void closeAllShells() throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            tester.remoteBot().closeAllShells();
        }
    }

    public static void resetDefaultAccount() throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            if (tester != null) {
                if (!tester.superBot().menuBar().saros().preferences()
                    .existsAccount(tester.getJID()))
                    tester.superBot().menuBar().saros().preferences()
                        .addAccount(tester.getJID(), tester.getPassword());
                if (!tester.superBot().menuBar().saros().preferences()
                    .isAccountActive(tester.getJID()))
                    tester.superBot().menuBar().saros().preferences()
                        .activateAccount(tester.getJID());
                tester.superBot().menuBar().saros().preferences()
                    .deleteAllNoActiveAccounts();

            }
        }
    }

    /**
     * Resets the buddy names for all active testers to their original by
     * sequentially calling {@link #resetBuddyNames(AbstractTester)}
     * 
     * @throws RemoteException
     */

    public static void resetBuddyNames() throws RemoteException {
        for (AbstractTester tester : currentTesters)
            resetBuddyNames(tester);
    }

    /**
     * Resets the names of all buddies for the tester to their original states
     * as defined in the configuration file
     * 
     * @param tester
     *            the tester
     * @throws RemoteException
     */

    public static void resetBuddyNames(AbstractTester tester)
        throws RemoteException {
        for (int i = 0; i < currentTesters.size(); i++) {
            if (tester == currentTesters.get(i))
                continue;

            tester.superBot().views().sarosView()
                .selectBuddy(currentTesters.get(i).getJID())
                .rename(currentTesters.get(i).getBaseJid());
        }
    }

    public static void resetBuddies() throws RemoteException {
        for (AbstractTester tester : currentTesters)
            resetBuddies(tester);
    }

    public static void resetBuddies(AbstractTester tester)
        throws RemoteException {
        for (int i = 0; i < currentTesters.size(); i++) {
            if (tester == currentTesters.get(i))
                continue;
            Util.addBuddies(tester, currentTesters.get(i));
        }
    }

    /**
     * Deletes all projects from all active testers by sequentially calling
     * {@link Util#deleteAllProjects(AbstractTester tester)}
     * 
     * @throws RemoteException
     */
    public static void deleteAllProjectsByActiveTesters()
        throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            Util.deleteAllProjects(tester);
        }
    }

    /**
     * Disconnects all active testers in the order the were initialized by
     * {@link #initTesters(AbstractTester tester, AbstractTester... testers)}
     * 
     * @throws RemoteException
     */
    public static void disconnectAllActiveTesters() throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            if (tester != null) {
                tester.superBot().views().sarosView().disconnect();
            }
        }
    }

    public static void deleteFoldersByActiveTesters(String project,
        String... folders) throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            for (String folder : folders) {
                if (tester.superBot().views().packageExplorerView()
                    .selectProject(project).existsWithRegex(folder))
                    tester.superBot().views().packageExplorerView()
                        .selectFolder(project, folder).delete();
            }
        }
    }

    /**
     * Define the leave session with the following steps.
     * <ol>
     * <li>The host leave session first.</li>
     * <li>Then other invitees confirm the window "Closing the Session"
     * concurrently</li>
     * </ol>
     * 
     * @param host
     *            the host of the current session
     * @throws RemoteException
     */
    public static void leaveSessionHostFirst(AbstractTester host)
        throws RemoteException {

        host.superBot().views().sarosView().leaveSession();
        for (final AbstractTester tester : currentTesters) {
            if (tester != host) {
                tester.superBot().views().sarosView().waitUntilIsNotInSession();

            }
        }
    }

    /**
     * Define the leave session with the following steps.
     * <ol>
     * <li>All invitees leave the session first.(concurrently)</li>
     * <li>Host waits until all invitees are no longer in the session using
     * "waitUntilAllPeersLeaveSession"</li>
     * <li>Host leaves the session</li>
     * </ol>
     * 
     * @param host
     *            the host of the current session
     * @throws RemoteException
     */

    public static void leaveSessionPeersFirst(AbstractTester host)
        throws RemoteException {
        List<JID> peerJIDs = new ArrayList<JID>();
        List<Callable<Void>> leaveTasks = new ArrayList<Callable<Void>>();
        for (final AbstractTester tester : currentTesters) {

            if (tester != host) {
                peerJIDs.add(tester.getJID());
                leaveTasks.add(new Callable<Void>() {
                    public Void call() throws Exception {
                        tester.superBot().views().sarosView().leaveSession();
                        return null;
                    }
                });
            }
        }

        Util.workAll(leaveTasks);

        host.superBot().views().sarosView()
            .waitUntilAllPeersLeaveSession(peerJIDs);

        host.remoteBot().view(VIEW_SAROS).toolbarButton(TB_STOP_SESSION)
            .click();
        host.superBot().views().sarosView().waitUntilIsNotInSession();
    }

}
