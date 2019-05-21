package saros.stf.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import saros.net.xmpp.JID;
import saros.stf.client.tester.AbstractTester;
import saros.stf.client.util.Util;
import saros.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import saros.test.util.EclipseTestThread;

public abstract class StfTestCase {

    private static final Logger LOGGER = Logger
        .getLogger(StfTestCase.class.getName());

    /** JUnit monitor. Do <b>NOT</b> call any method of this instance ! */
    public static ArrayList<String> allFailedTests = new ArrayList<String>();
    @Rule
    public final TestWatcher watcher = new TestWatcher() {
        @Override
        public void failed(Throwable e, Description description) {
            if (!(e instanceof AssumptionViolatedException)) {
                this.logMessage("******* TESTCASE " + description.getClassName()
                    + ":" + description.getMethodName() + " FAILED *******");
                this.captureScreenshot((description.getClassName() + "_"
                    + description.getMethodName()).replace('.', '_'));
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                e.printStackTrace(new PrintStream(out));
                this.logMessage(new String(out.toByteArray()));
                // all names of the test classes are saved in allClassNames, in
                // order to see if the test is run by itself or in a TestSuite
                if (!allFailedTests.contains(description.getClassName())) {
                    allFailedTests.add(description.getClassName());
                }
            }
        }

        @Override
        public void succeeded(Description description) {
            this.logMessage("******* TESTCASE " + description.getClassName()
                + ":" + description.getMethodName() + " SUCCEDED *******");

        }

        @Override
        public void finished(Description description) {
            this.logMessage("******* TESTCASE " + description.getClassName()
                + ":" + description.getMethodName() + " FINISHED *******");
        }

        @Override
        public void starting(Description description) {
            StfTestCase.lastTestClass = description.getClass();
            this.logMessage(
                "******* STARTING TESTCASE " + description.getClassName() + ":"
                    + description.getMethodName() + " *******");
        }

        @Override
        public void skipped(AssumptionViolatedException e,
            Description description) {
            this.logMessage("******* TESTCASE " + description.getClassName()
                + ":" + description.getMethodName() + " SKIPPED BECAUSE OF "
                + e.getMessage() + "*******");
        }

        private void logMessage(String message) {
            Iterator var3 = StfTestCase.currentTesters.iterator();

            while (var3.hasNext()) {
                AbstractTester tester = (AbstractTester) var3.next();

                try {
                    tester.remoteBot().logMessage(message);
                } catch (Exception var5) {
                    StfTestCase.LOGGER.log(Level.WARNING,
                        "could not log message '" + message
                            + "' at remote bot of tester " + tester,
                        var5);
                }
            }

        }

        private void captureScreenshot(String name) {
            Iterator var3 = StfTestCase.currentTesters.iterator();

            while (var3.hasNext()) {
                AbstractTester tester = (AbstractTester) var3.next();

                try {
                    tester.remoteBot().captureScreenshot(name + "_" + tester
                        + "_" + System.currentTimeMillis() + ".jpg");
                } catch (Exception var5) {
                    StfTestCase.LOGGER.log(Level.WARNING,
                        "could capture a screenshot at remote bot of tester "
                            + tester,
                        var5);
                }
            }

        }
    };

    private static List<AbstractTester> currentTesters = new ArrayList<AbstractTester>();

    private static List<EclipseTestThread> currentTestThreads = new ArrayList<EclipseTestThread>();

    private static Class<?> lastTestClass;

    /*
     * Make sure that the JUNIT Ant task does not load every test case with a
     * new class loader or this will NOT work during a regression !
     */

    private static boolean abortAllTests = false;

    @BeforeClass
    public static void checkThreadsBeforeClass() {
        checkAndStopRunningTestThreads(true);
    }

    @Before
    public final void checkThreadsBeforeTest() {
        checkAndStopRunningTestThreads(false);
    }

    /**
     * Calls
     *
     * <ol>
     * <li>reset all contact names by calling
     * {@linkplain #initTesters(AbstractTester, AbstractTester...)}
     * <li>{@linkplain #setUpWorkbench()}
     * <li>{@linkplain #setUpSaros()}
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

    /**
     * This method is called after every test case class. Override this method
     * with care! Internal calls {@linkplain #tearDownSaros}
     */
    @AfterClass
    public static void cleanUpSaros() throws Exception {
        tearDownSaros();
    }

    /**
     * Tries to reset Saros to a stable state for the given tester(s). It does
     * that be performing the following actions
     *
     * <ol>
     * <li>reset all contact names by calling
     * {@linkplain #resetNicknames(AbstractTester)}
     * <li>disconnect from the current session
     * <li>delete the contents of the workspace
     * </ol>
     *
     * @throws Exception
     *             if a (internal) failure occur
     */
    public static void tearDownSaros() throws Exception {

        try {
            terminateTestThreads(60 * 1000);
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE,
                "aborting execution of all tests: " + t.getMessage(), t);
            checkAndStopRunningTestThreads(true);
        }

        Exception exception = null;

        for (AbstractTester tester : currentTesters) {
            try {
                tester.superBot().internal().resetSarosVersion();

                if (tester.superBot().views().sarosView().isConnected())
                    tester.superBot().views().sarosView().disconnect();

                tester.superBot().views().sarosView().disconnect();
                tester.remoteBot().resetWorkbench();

                // TODO clear the chat history, should be done via non-gui
                // access
                if (tester.superBot().views().sarosView().hasOpenChatrooms()) {
                    try {
                        tester.superBot().views().sarosView()
                            .closeChatroomWithRegex(".*");
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING,
                            "chatsrooms were closed lately by Saros", e);
                    }
                }

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
     *
     * <ul>
     * <li>activate Saros instance workbench
     * <li>close all opened popUp windows
     * <li>close all opened editors
     * <li>deletes all projects
     * <li>close welcome view, if it is open
     * <li>opens the default perspective
     * <li>close all unnecessary views
     * </ul>
     *
     * @throws Exception
     *             if a (internal) failure occur
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
     * Brings Saros to a original state before beginning your tests.
     *
     * <ul>
     * <li>make automaticReminder disable
     * <li>open sarosViews
     * <li>connect
     * <li>ensured that all testers are available in each testers contact list
     * </ul>
     *
     * @throws Exception
     *             if a (internal) failure occur
     */
    public static void setUpSaros() throws Exception {
        Exception exception = null;

        for (AbstractTester tester : currentTesters) {
            try {
                tester.superBot().menuBar().saros().preferences()
                    .disableAutomaticReminder();
                tester.superBot().menuBar().saros().preferences()
                    .restoreDefaults();
                Util.openSarosView(tester);

                tester.controlBot().getAccountManipulator()
                    .restoreDefaultAccount(tester.getName(),
                        tester.getPassword(), tester.getDomain());

            } catch (Exception e) {
                exception = e;
            }
        }

        if (exception != null)
            throw exception;

        final List<Callable<Void>> connectTasks = new ArrayList<Callable<Void>>();

        for (final AbstractTester tester : currentTesters) {
            connectTasks.add(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    tester.superBot().views().sarosView().connectWith(
                        tester.getJID(), tester.getPassword(), true);

                    return null;
                }
            });
        }

        Util.workAll(connectTasks);

        resetNicknames();
        resetContacts();
    }

    /**
     * Resets the workbench for every active tester to their original state.
     *
     * @see IRemoteWorkbenchBot#resetWorkbench()
     * @throws Exception
     *             if a (internal) failure occur
     */
    public static void resetWorkbenches() throws Exception {
        for (AbstractTester tester : currentTesters) {
            tester.remoteBot().resetWorkbench();
        }
    }

    /**
     * Closes all editors for all active testers.
     *
     * @see IRemoteWorkbenchBot#closeAllShells()
     * @throws Exception
     *             if a (internal) failure occur
     */
    public static void closeAllEditors() throws Exception {
        for (AbstractTester tester : currentTesters) {
            tester.remoteBot().closeAllEditors();
        }
    }

    /**
     * Closes all shells for all active testers.
     *
     * @see IRemoteWorkbenchBot#closeAllShells()
     * @throws Exception
     *             if a (internal) failure occur
     */
    public static void closeAllShells() throws Exception {
        for (AbstractTester tester : currentTesters) {
            tester.remoteBot().closeAllShells();
        }
    }

    /**
     * Resets the account of all testers.
     *
     * <ul>
     * <li>Add the account of the tester to the account store
     * <li>Activate the account of the tester
     * <li>Deletes all non active accounts
     * </ul>
     *
     * @throws Exception
     *             if a (internal) failure occur
     */
    public static void resetDefaultAccount() throws Exception {
        for (AbstractTester tester : currentTesters)
            tester.controlBot().getAccountManipulator().restoreDefaultAccount(
                tester.getName(), tester.getPassword(), tester.getDomain());
    }

    /**
     * Resets the nicknames in the contact list for all active testers by
     * sequentially calling {@link #resetNicknames(AbstractTester)}.
     *
     * @throws IllegalStateException
     *             if one of the current testers is not connected
     * @throws Exception
     *             for any other (internal) failure
     */
    public static void resetNicknames() throws Exception {
        for (AbstractTester tester : currentTesters)
            resetNicknames(tester);
    }

    /**
     * Resets the nicknames of all contacts in the contact list of the tester to
     * their original states (base JID) as defined in the configuration file.
     *
     * @param tester
     *            the tester
     * @throws IllegalStateException
     *             if the tester is not connected
     * @throws Exception
     *             for any other (internal) failure
     */
    public static void resetNicknames(AbstractTester tester) throws Exception {

        if (!tester.superBot().views().sarosView().isConnected())
            throw new IllegalStateException(tester + " is not connected");

        for (int i = 0; i < currentTesters.size(); i++) {
            if (tester == currentTesters.get(i))
                continue;

            if (tester.superBot().views().sarosView()
                .isInContactList(currentTesters.get(i).getJID())) {
                tester.superBot().views().sarosView()
                    .selectContact(currentTesters.get(i).getJID())
                    .rename(currentTesters.get(i).getBaseJid());
            }
        }
    }

    /**
     * Resets the contacts of the testers involved in this test case to their
     * original states.
     *
     * <p>
     * This method will only add connections between testers, but not remove
     * them. Example: If the test case involves ALICE, BOB, and CARL, this
     * method will make sure that all three of them are pair-wise contacts, but
     * it will neither create nor cut any connection to DAVE.
     *
     * @throws IllegalStateException
     *             if one of the current testers is not connected
     * @throws Exception
     *             for any other (internal) failure
     */
    public static void resetContacts() throws Exception {
        // Create all unique pairs of distinct testers
        for (int first = 0; first < currentTesters.size(); first++) {
            for (int second = first + 1; second < currentTesters
                .size(); second++) {
                Util.addTestersToContactList(currentTesters.get(first),
                    currentTesters.get(second));
            }
        }
    }

    /**
     * Deletes all projects from all active testers by sequentially removing all
     * projects from the workspace. @NOTE this method does not invoke any GUI
     * action, instead it uses the Eclipse API. Unexpected results can occur
     * when there are still open unsaved editor windows before this method is
     * called.
     *
     * @return <code>true</code> if all workspaces could be cleared,
     *         <code>false</code> otherwise
     * @throws Exception
     *             if a (internal) failure occurs
     */
    public static boolean clearWorkspaces() throws Exception {
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
     * @throws Exception
     *             if a (internal) failure occurs
     */
    public static void disconnectAllActiveTesters() throws Exception {
        for (AbstractTester tester : currentTesters) {
            if (tester != null) {
                tester.superBot().views().sarosView().disconnect();
            }
        }
    }

    /**
     * Stops the current session for all testers. The host is leaving the
     * session first.
     *
     * @param host
     *            the host of the current session
     * @throws IllegalStateException
     *             if the host is not host of the current session
     * @throws Exception
     *             if a (internal) failure occurs
     */
    public static void leaveSessionHostFirst(AbstractTester host)
        throws Exception {

        if (!host.superBot().views().sarosView().isHost())
            throw new IllegalStateException(
                host + " is not host of the current session");

        host.superBot().views().sarosView().leaveSession();
        for (final AbstractTester tester : currentTesters) {
            if (tester != host) {
                tester.superBot().views().sarosView().waitUntilIsNotInSession();
            }
        }
    }

    /**
     * Stops the current session for all testers. The host is the last one who
     * is leaving the session.
     *
     * <ol>
     * <li>All non-host session users leave the session first.(concurrently)
     * <li>Host waits until all remote users are no longer in the session.
     * <li>Host leaves the session.
     * </ol>
     *
     * @param host
     *            the host of the current session
     * @throws IllegalStateException
     *             if the host is not host of the current session
     * @throws Exception
     *             if a (internal) failure occurs
     */
    public static void leaveSessionPeersFirst(AbstractTester host)
        throws Exception {

        if (!host.superBot().views().sarosView().isHost())
            throw new IllegalStateException(
                host + " is not host of the current session");

        List<JID> peerJIDs = new ArrayList<JID>();
        List<Callable<Void>> leaveTasks = new ArrayList<Callable<Void>>();
        for (final AbstractTester tester : currentTesters) {

            if (tester != host) {
                peerJIDs.add(tester.getJID());
                leaveTasks.add(new Callable<Void>() {
                    @Override
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

        host.superBot().views().sarosView().leaveSession();
        host.superBot().views().sarosView().waitUntilIsNotInSession();
    }

    /**
     * Creates a new {@link EclipseTestThread}. The test thread can be
     * terminated by calling {@link #terminateTestThreads}. The test thread is
     * automatically terminated after the <b>last</b> test of the test class is
     * executed.
     *
     * @param runnable
     *            the runnable that should be executed in this test thread
     * @return a new, not yet started, test thread
     */
    public static EclipseTestThread createTestThread(
        EclipseTestThread.Runnable runnable) {
        EclipseTestThread thread = new EclipseTestThread(runnable);
        currentTestThreads.add(thread);
        return thread;
    }

    /**
     * Terminates all test threads that were created with
     * {@link #createTestThread}.
     *
     * @param timeout
     *            timeout in milliseconds to wait for the termination of all
     *            test threads
     * @throws IllegalStateException
     *             if there are still test threads running
     */
    public static void terminateTestThreads(long timeout) {
        for (EclipseTestThread thread : currentTestThreads)
            thread.interrupt();

        if (Util.joinAll(timeout,
            currentTestThreads.toArray(new EclipseTestThread[0]))) {
            currentTestThreads.clear();
            return;
        }

        for (Iterator<EclipseTestThread> it = currentTestThreads.iterator(); it
            .hasNext();) {

            EclipseTestThread thread = it.next();

            if (!thread.isAlive())
                it.remove();
        }

        if (!currentTestThreads.isEmpty())
            throw new IllegalStateException(
                currentTestThreads.size() + " test threads are still alive");
    }

    @SuppressWarnings("deprecation")
    private static void checkAndStopRunningTestThreads(boolean abortAllTests) {

        if (currentTestThreads.isEmpty() && !StfTestCase.abortAllTests)
            return;

        assert lastTestClass != null;

        IllegalStateException exception = new IllegalStateException(
            "could not terminate all test threads for test class "
                + lastTestClass.getName());

        if (!abortAllTests)
            throw exception;

        // do it the hard way and give up

        StfTestCase.abortAllTests = true;

        for (EclipseTestThread thread : currentTestThreads)
            thread.stop();

        currentTestThreads.clear();

        throw exception;
    }

    /**
     * Checks if the test for establishing a session between two users passed
     */
    public static boolean checkIfShare2UsersSequentiallySucceeded() {
        for (String failedTest : allFailedTests) {
            if (failedTest.contains("Share2UsersSequentiallyTest")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the test for establishing a session (concurrently) among three
     * users passed
     */
    public static boolean checkIfShare3UsersConcurrentlySucceeded() {
        for (String failedTest : allFailedTests) {
            if (failedTest.contains("Share3UsersConcurrentlyTest")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the test for establishing a session (concurrently) between two
     * users passed and if ConcurrentEditingTest passed
     */
    public static boolean checkIfShare2UsersSequentiallyandConcurrentEditingSucceeded() {
        for (String failedTest : allFailedTests) {
            if (failedTest.contains("Share2UsersSequentiallyTest")
                || failedTest.contains("ConcurrentEditingTest")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the test for establishing a session (concurrently) between two
     * users passed and SimpleFollowModeIITest passed
     */

    public static boolean checkIfShare2UsersSequentiallyandSimpleFollowModeIISucceeded() {
        for (String failedTest : allFailedTests) {
            if (failedTest.contains("Share2UsersSequentiallyTest")
                || failedTest.contains("SimpleFollowModeIITest")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the test for establishing a session (concurrently) between two
     * users passed and EditDuringInvitationTest passed
     */
    public static boolean checkIfShare2UsersSequentiallyandEditDuringInvitationSucceeded() {
        for (String failedTest : allFailedTests) {
            if (failedTest.contains("Share2UsersSequentiallyTest")
                || failedTest.contains("EditDuringInvitationTest")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the test for establishing a session (concurrently) among three
     * users passed and ConcurrentEditingTest passed
     */
    public static boolean checkIfShare3UsersConcurrentlyandConcurrentEditingSucceeded() {
        for (String failedTest : allFailedTests) {
            if (failedTest.contains("Share3UsersConcurrentlyTest")
                || failedTest.contains("ConcurrentEditingTest")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the test for establishing a session (concurrently) among three
     * users passed and EditDuringInvitationTest passed
     */
    public static boolean Share3UsersConcurrentlyandEditDuringInvitationSucceeded() {

        for (String failedTest : allFailedTests) {
            if (failedTest.contains("Share3UsersConcurrentlyTest")
                || failedTest.contains("EditDuringInvitationTest")) {
                return false;
            }
        }
        return true;
    }
}
