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
import de.fu_berlin.inf.dpp.stf.client.util.Constants;
import de.fu_berlin.inf.dpp.stf.client.util.Util;

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
     **********************************************/

    @Before
    public void setUp() throws Exception {
        for (AbstractTester tester : currentTesters) {
            tester.remoteBot().logMessage(
                "******* " + "STARTING TESTCASE " + this.getClass().getName()
                    + ":" + currentTestName.getMethodName() + " *******");
        }
        closeAllShells();
    }

    @After
    public void tearDown() throws RemoteException {
        resetWorkbenches();
    }

    @AfterClass
    public static void tearDownAfterClass() throws RemoteException {
        resetSaros();
    }

    /**********************************************
     * 
     * often used to define preconditions
     * 
     **********************************************/

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
        }
        resetBuddies();
    }

    /**********************************************
     * 
     * often used to define afterConditions
     * 
     **********************************************/
    /**
     * For all active testers, reset buddy names, disconnect, delete all
     * projects.
     * 
     * @throws RemoteException
     */
    public static void resetSaros() throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            tester.superBot().views().sarosView().disconnect();
            Util.deleteAllProjects(tester);
        }

        currentTesters.clear();
    }

    public static void resetSaros(AbstractTester... tester)
        throws RemoteException {
        resetBuddiesName();

        for (AbstractTester t : tester) {
            t.superBot().views().sarosView().disconnect();
            Util.deleteAllProjects(t);
            currentTesters.remove(t);
        }
    }

    public static void resetWorkbenches() throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            tester.remoteBot().resetWorkbench();
        }
    }

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

    public static void resetBuddiesName() throws RemoteException {
        for (int i = 0; i < currentTesters.size(); i++) {
            for (int j = 0; j < currentTesters.size(); j++) {
                if (i == j)
                    continue;

                currentTesters.get(i).superBot().views().sarosView()
                    .selectBuddy(currentTesters.get(j).getJID())
                    .rename(currentTesters.get(j).getBaseJid());
            }
        }
    }

    public static void resetBuddies() throws RemoteException {
        // check buddy lists.
        for (int i = 0; i < currentTesters.size(); i++) {
            for (int j = 0; j < currentTesters.size(); j++) {
                if (i == j)
                    continue;

                Util.addBuddies(currentTesters.get(i), currentTesters.get(j));
            }
        }
    }

    public static void createSameJavaProjectByActiveTesters()
        throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            tester
                .superBot()
                .views()
                .packageExplorerView()
                .tree()
                .newC()
                .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1,
                    Constants.CLS1);
        }
    }

    public static void deleteAllProjectsByActiveTesters()
        throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            Util.deleteAllProjects(tester);
        }
    }

    public static void disconnectByActiveTesters() throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            if (tester != null) {
                tester.superBot().views().sarosView().disconnect();
            }
        }
    }

    public static void deleteFoldersByActiveTesters(String... folders)
        throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            for (String folder : folders) {
                if (tester.superBot().views().packageExplorerView()
                    .selectProject(Constants.PROJECT1).existsWithRegex(folder))
                    tester.superBot().views().packageExplorerView()
                        .selectFolder(Constants.PROJECT1, folder).delete();
            }
        }
    }

    /**
     * Define the leave session with the following steps.
     * <ol>
     * <li>The host(alice) leave session first.</li>
     * <li>Then other invitees confirm the windonws "Closing the Session"
     * concurrently</li>
     * </ol>
     * 
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
     * <li>The musicians bob and carl leave the session first.(concurrently)</li>
     * <li>wait until bob and carl are really not in the session using
     * "waitUntilAllPeersLeaveSession", then leave the host alice.</li>
     * </ol>
     * make sure,
     * 
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    public static void leaveSessionPeersFirst() throws RemoteException,
        InterruptedException {
        AbstractTester host = null;
        List<JID> peerJIDs = new ArrayList<JID>();
        List<Callable<Void>> leaveTasks = new ArrayList<Callable<Void>>();
        for (final AbstractTester tester : currentTesters) {

            if (tester.superBot().views().sarosView().isHost()) {
                host = tester;
            } else {
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
        if (host != null) {
            host.superBot().views().sarosView()
                .waitUntilAllPeersLeaveSession(peerJIDs);
            host.remoteBot().view(VIEW_SAROS).toolbarButton(TB_STOP_SESSION)
                .click();
            host.superBot().views().sarosView().waitUntilIsNotInSession();
        }

    }
}
