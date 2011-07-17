package de.fu_berlin.inf.dpp.stf.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.tester.AbstractTester;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteWorkbenchBot;

public abstract class StfTestCase {

    private static final Logger LOGGER = Logger.getLogger(StfTestCase.class
        .getName());

    @Rule
    public TestWatchman watchman = new TestWatchman() {
        @Override
        public void failed(Throwable e, FrameworkMethod method) {
            logMessage("******* " + "TESTCASE "
                + method.getMethod().getDeclaringClass().getName() + ":"
                + method.getName() + " FAILED *******");
            captureScreenshot((method.getMethod().getDeclaringClass().getName()
                + "_" + method.getName()).replace('.', '_'));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out));
            logMessage(new String(out.toByteArray()));
        }

        @Override
        public void succeeded(FrameworkMethod method) {
            logMessage("******* " + "TESTCASE "
                + method.getMethod().getDeclaringClass().getName() + ":"
                + method.getName() + " SUCCEDED *******");
        }

        @Override
        public void finished(FrameworkMethod method) {
            logMessage("******* " + "TESTCASE "
                + method.getMethod().getDeclaringClass().getName() + ":"
                + method.getName() + " FINISHED *******");
        }

        @Override
        public void starting(FrameworkMethod method) {
            logMessage("******* " + "STARTING TESTCASE "
                + method.getMethod().getDeclaringClass().getName() + ":"
                + method.getName() + " *******");

        }

        private void logMessage(String message) {
            for (AbstractTester tester : currentTesters) {
                try {
                    tester.remoteBot().logMessage(message);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "could not log message '"
                        + message + "' at remote bot of tester " + tester, e);
                }
            }
        }

        private void captureScreenshot(String name) {

            for (AbstractTester tester : currentTesters) {
                try {
                    tester.remoteBot().captureScreenshot(
                        name + "_" + tester + "_" + System.currentTimeMillis()
                            + ".jpg");
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING,
                        "could capture a screenshot at remote bot of tester "
                            + tester, e);
                }
            }
        }
    };

    private static List<AbstractTester> currentTesters = new ArrayList<AbstractTester>();

    /**
     * Calls
     * <ol>
     * <li>reset all buddy names by calling
     * {@linkplain #initTesters(AbstractTester, AbstractTester...)}</li>
     * <li>{@linkplain #setUpWorkbench()}</li>
     * <li>{@linkplain #setUpSaros()}</li>
     * </ol>
     * 
     * @param tester
     *            a tester e.g ALICE
     * @param testers
     *            additional testers e.g BOB, CARL
     * @throws Exception
     */
    public static void select(AbstractTester tester, AbstractTester... testers)
        throws Exception {
        initTesters(tester, testers);
        setUpWorkbench();
        setUpSaros();
    }

    /**
     * Registers the given testers to be participants of the current test case.
     * <b>THIS METHOD SHOULD ALWAYS BE THE FIRST METHOD CALLED IN THE
     * <tt>BEFORECLASS</tt> METHOD.</b>
     * 
     * @param tester
     *            a tester e.g ALICE
     * @param testers
     *            additional testers e.g BOB, CARL
     */
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

    @AfterClass
    public static void cleanUpSaros() throws Exception {
        tearDownSaros();
    }

    /**
     * Tries to reset Saros to a stable state for the given tester(s). It does
     * that be performing the following actions
     * <ol>
     * <li>reset all buddy names by calling
     * {@linkplain #resetBuddyNames(AbstractTester)}</li>
     * <li>disconnect from the current session</li>
     * <li>delete the contents of the workspace</li>
     * </ol>
     * 
     * 
     * @throws Exception
     */
    public static void tearDownSaros() throws Exception {

        Exception exception = null;

        for (AbstractTester tester : currentTesters) {
            try {
                if (!tester.superBot().views().sarosView().isConnected()) {
                    tester.superBot().views().sarosView()
                        .connectWith(tester.getJID(), tester.getPassword());
                }
                resetBuddyNames(tester);
                tester.superBot().views().sarosView().disconnect();
                tester.remoteBot().resetWorkbench();

                // Consistency watch dog seems to lock some files from time to
                // time
                int timeout = 0;
                boolean cleared = false;
                do {
                    cleared = tester.superBot().internal().clearWorkspace();
                    if (cleared)
                        break;
                    Thread.sleep(10000);
                } while (++timeout < 6);

                if (!cleared)
                    throw new IOException(
                        "could not clear the workspace of tester: "
                            + tester.toString());
            } catch (Exception e) {
                exception = e;
            }
        }
        currentTesters.clear();

        if (exception != null)
            throw exception;

    }

    /**
     * Brings workbench to a original state before beginning your tests
     * <ul>
     * <li>activate Saros instance workbench</li>
     * <li>close all opened popUp windows</li>
     * <li>close all opened editors</li>
     * <li>deletes all projects</li>
     * <li>close welcome view, if it is open</li>
     * <li>opens the default perspective</li>
     * <li>close all unnecessary views</li>
     * </ul>
     * 
     * @throws Exception
     */
    public static void setUpWorkbench() throws Exception {

        Exception exception = null;

        for (AbstractTester tester : currentTesters) {
            try {
                tester.remoteBot().activateWorkbench();
                if (tester.remoteBot().isViewOpen("Welcome"))
                    tester.remoteBot().view("Welcome").close();
                tester.superBot().menuBar().window().openPerspective();
                Util.closeUnnecessaryViews(tester);
                tester.superBot().internal().clearWorkspace();
            } catch (Exception e) {
                exception = e;
            }
        }

        if (exception != null)
            throw exception;
    }

    /**
     * Brings Saros to a original state before beginning your tests
     * <ul>
     * <li>make automaticReminder disable</li>
     * <li>open sarosViews</li>
     * <li>connect</li>
     * <li>check buddy lists, if all active testers are in contact</li>
     * </ul>
     * 
     * @throws Exception
     */
    public static void setUpSaros() throws Exception {
        Exception exception = null;

        for (AbstractTester tester : currentTesters) {
            try {
                tester.superBot().menuBar().saros().preferences()
                    .disableAutomaticReminder();
                Util.openSarosView(tester);
                tester.superBot().views().sarosView()
                    .connectWith(tester.getJID(), tester.getPassword());
            } catch (Exception e) {
                exception = e;
            }
        }
        for (AbstractTester tester : currentTesters) {
            try {
                resetBuddies(tester);
            } catch (Exception e) {
                exception = e;
            }
        }

        if (exception != null)
            throw exception;
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
     * Closes all editors for all active testers
     * 
     * @see IRemoteWorkbenchBot#closeAllShells()
     * 
     * @throws RemoteException
     */
    public static void closeAllEditors() throws RemoteException {
        for (AbstractTester tester : currentTesters) {
            tester.remoteBot().closeAllEditors();
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

    /**
     * Resets the account of all testers.
     * <ul>
     * <li>Add the account of the tester to the account store</li>
     * <li>Activate the account of the tester</li>
     * <li>Deletes all non active accounts</li>
     * </ul>
     * 
     * @throws RemoteException
     */
    public static void resetDefaultAccount() throws RemoteException {
        for (AbstractTester tester : currentTesters) {

            if (!tester.superBot().menuBar().saros().preferences()
                .existsAccount(tester.getJID())) {

                tester.superBot().menuBar().saros().preferences()
                    .addAccount(tester.getJID(), tester.getPassword());
            }

            if (!tester.superBot().menuBar().saros().preferences()
                .isAccountActive(tester.getJID())) {

                tester.superBot().menuBar().saros().preferences()
                    .activateAccount(tester.getJID());
            }

            tester.superBot().menuBar().saros().preferences()
                .deleteAllNonActiveAccounts();

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

            if (tester.superBot().views().sarosView()
                .hasBuddy(currentTesters.get(i).getJID())) {
                tester.superBot().views().sarosView()
                    .selectBuddy(currentTesters.get(i).getJID())
                    .rename(currentTesters.get(i).getBaseJid());
            }
        }
    }

    /**
     * Resets the buddies for all active testers to their original by
     * sequentially calling {@link #resetBuddies(AbstractTester)}
     * 
     * @throws RemoteException
     */
    public static void resetBuddies() throws RemoteException {
        for (AbstractTester tester : currentTesters)
            resetBuddies(tester);
    }

    /**
     * Resets the buddies of the tester to their original states as defined in
     * the configuration file. E.g if the test case included ALICE, BOB and
     * CARL, and ALICE removed BOB from her roster then after the method returns
     * ALICE will have BOB again in her roster and also BOB will have ALICE back
     * in his roster.
     * 
     * @param tester
     *            the tester
     * @throws RemoteException
     */

    public static void resetBuddies(AbstractTester tester)
        throws RemoteException {
        for (int i = 0; i < currentTesters.size(); i++) {
            if (tester == currentTesters.get(i))
                continue;
            Util.addBuddiesToContactList(tester, currentTesters.get(i));
        }
    }

    /**
     * Deletes all projects from all active testers by sequentially removing all
     * projects from the workspace.
     * 
     * @NOTE this method does not invoke any GUI action, instead it uses the
     *       Eclipse API. Unexpected results can occur when there are still open
     *       unsaved editor windows before this method is called.
     * @return <code>true</code> if all workspaces could be cleared,
     *         <code>false</code> otherwise
     * @throws RemoteException
     */
    public static boolean clearWorkspaces() throws RemoteException {
        boolean cleared = true;
        for (AbstractTester tester : currentTesters) {
            cleared &= tester.superBot().internal().clearWorkspace();
        }
        return cleared;
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
     * <li>Host waits until all invitees are no longer in the session.</li>
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

        host.superBot().views().sarosView().disconnect();

        host.superBot().views().sarosView().waitUntilIsNotInSession();
    }

}
