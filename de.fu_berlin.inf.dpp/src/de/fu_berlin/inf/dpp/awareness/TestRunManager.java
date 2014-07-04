package de.fu_berlin.inf.dpp.awareness;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jdt.junit.JUnitCore;
import org.eclipse.jdt.junit.TestRunListener;
import org.eclipse.jdt.junit.model.ITestElement.Result;
import org.eclipse.jdt.junit.model.ITestRunSession;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.jivesoftware.smack.XMPPException;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.TestRunActivity;
import de.fu_berlin.inf.dpp.activities.TestRunActivity.State;
import de.fu_berlin.inf.dpp.communication.chat.AbstractChat;
import de.fu_berlin.inf.dpp.communication.chat.ChatElement;
import de.fu_berlin.inf.dpp.communication.chat.IChat;
import de.fu_berlin.inf.dpp.communication.chat.IChatServiceListener;
import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChat;
import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChatService;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.model.session.SessionContentProvider;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;

/**
 * This manager is responsible for distributing information about test runs of
 * all session participants.
 * <p>
 * It listens on the {@link org.eclipse.jdt.junit.JUnitCore} for started,
 * running and completed tests and notifies all attached listeners, when the
 * current user has started and finished a test run.
 * <p>
 * Note: This class is just instantiated when the JUnitCore bundle is available.
 * */
public class TestRunManager extends AbstractActivityProducer implements
    Startable {

    private static final Logger LOG = Logger.getLogger(TestRunManager.class);

    private final List<TestRunsListener> listeners = new CopyOnWriteArrayList<TestRunsListener>();
    private final ISarosSession session;
    private final AwarenessInformationCollector awarenessInformationCollector;
    private MultiUserChatService mucs;
    private IChat sessionChat;

    /**
     * Constructor for the {@link TestRunManager}.
     * 
     * @param session
     *            The session, in which the action awareness information are
     *            distributed.
     * @param awarenessInformationCollector
     *            This singleton is used to store and retrieve the collected
     *            information about test runs and is used by the
     *            {@link SessionContentProvider} to retrieve the information and
     *            to display them.
     * @param mucs
     *            The class which manages the creation and destruction of
     *            {@link MultiUserChat}'s. It is used to display the collected
     *            information about test runs in the multi-user chat in the
     *            Saros view.
     * */
    public TestRunManager(ISarosSession session,
        AwarenessInformationCollector awarenessInformationCollector,
        MultiUserChatService mucs) {
        this.session = session;
        this.awarenessInformationCollector = awarenessInformationCollector;
        this.mucs = mucs;
        this.mucs.addChatServiceListener(chatServiceListener);
    }

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        @Override
        public void exec(IActivity activity) {
            if (Boolean
                .getBoolean("de.fu_berlin.inf.dpp.awareness.SESSION_OVERVIEW")) {
                activity.dispatch(receiverSessionOverview);
            }
            if (Boolean.getBoolean("de.fu_berlin.inf.dpp.awareness.CHAT")) {
                activity.dispatch(receiverChat);
            }
            if (Boolean.getBoolean("de.fu_berlin.inf.dpp.awareness.STATUSLINE")) {
                activity.dispatch(receiverStatusLine);
            }
        }
    };

    private IActivityReceiver receiverSessionOverview = new AbstractActivityReceiver() {
        @Override
        public void receive(TestRunActivity activity) {

            User user = activity.getSource();

            // store test run information to display them later
            awarenessInformationCollector.addTestRun(user, activity.getName(),
                activity.getState());
            notifyListeners();

            /*
             * if the received test run activity stands for a finished test run,
             * removes the information after it is displayed
             */
            if (activity.getState() != State.UNDEFINED) {
                awarenessInformationCollector.removeTestRun(user);
            }
        }
    };

    // TODO refactor this if this remains in saros after the user test
    // FIXME In some cases, the status line is empty
    private IActivityReceiver receiverStatusLine = new AbstractActivityReceiver() {
        @Override
        public void receive(TestRunActivity activity) {

            final User user = activity.getSource();
            final String testRunName = activity.getName();
            final State testRunState = activity.getState();

            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    IActionBars actionBars = null;
                    try {
                        IWorkbench workbench = PlatformUI.getWorkbench();
                        IWorkbenchPage workbenchPage = workbench
                            .getActiveWorkbenchWindow().getActivePage();
                        IWorkbenchPart part = workbenchPage.getActivePart();
                        IWorkbenchPartSite site = part.getSite();

                        if (site instanceof IViewSite) {
                            actionBars = ((IViewSite) site).getActionBars();
                        } else if (site instanceof IEditorSite) {
                            actionBars = ((IEditorSite) site).getActionBars();
                        }

                        String message = null;
                        String startMessage = "runs test";
                        String endMessage = "has run test";

                        switch (testRunState) {
                        case UNDEFINED:
                            message = user.getNickname() + startMessage + " '"
                                + testRunName + "'";
                            break;
                        case OK:
                            message = user.getNickname() + endMessage + " '"
                                + testRunName + "', result: SUCCESS";
                            break;
                        case ERROR:
                            message = user.getNickname() + endMessage + " '"
                                + testRunName + "', result: ERROR";
                            break;
                        case FAILURE:
                            message = user.getNickname() + endMessage + " '"
                                + testRunName + "', result: FAILURE";
                            break;
                        default:
                            break;
                        }

                        if (message != null && actionBars != null)
                            actionBars.getStatusLineManager().setMessage(
                                message);
                    } catch (Exception e) {
                        // TODO don't handle everything within a try/catch block
                        LOG.debug(
                            "Could not get action bars for the status line", e);
                        return;
                    }
                }
            });
        }
    };

    // TODO refactor this if this remains in saros after the user test
    private IActivityReceiver receiverChat = new AbstractActivityReceiver() {
        @Override
        public void receive(TestRunActivity activity) {

            String message = null;
            String testRunName = activity.getName();
            State testRunState = activity.getState();

            String startMessage = "... runs test";
            String endMessage = "... has run test";

            switch (testRunState) {
            case UNDEFINED:
                message = startMessage + " '" + testRunName + "'";
                break;
            case OK:
                message = endMessage + " '" + testRunName
                    + "', result: SUCCESS";
                break;
            case ERROR:
                message = endMessage + " '" + testRunName + "', result: ERROR";
                break;
            case FAILURE:
                message = endMessage + " '" + testRunName
                    + "', result: FAILURE";
                break;
            default:
                break;
            }

            if (message != null) {
                sessionChat.addHistoryEntry(new ChatElement(message, activity
                    .getSource().getJID(), new Date()));
                ((AbstractChat) sessionChat).notifyJIDMessageReceived(activity
                    .getSource().getJID(), message);
            }
        }
    };

    /**
     * This listener is for the lifecyle of an {@link IChat}. Here, the
     * lifecycle of a {@link MultiUserChat MUC} is listened to. If the MUC is
     * created, it stores the reference to it. If the MUC is aborted or
     * destroyed, it sets the reference to it to <code>null</code>.
     * */
    private final IChatServiceListener chatServiceListener = new IChatServiceListener() {
        @Override
        public void chatCreated(IChat chat, boolean createdLocally) {
            if (chat instanceof MultiUserChat) {
                sessionChat = chat;
            }
        }

        @Override
        public void chatDestroyed(IChat chat) {
            if (chat instanceof MultiUserChat) {
                sessionChat = null;
            }
        }

        @Override
        public void chatAborted(IChat chat, XMPPException exception) {
            if (chat instanceof MultiUserChat) {
                sessionChat = null;
            }
        }
    };

    /**
     * Listener for started and completed test runs
     * */
    private final TestRunListener testRunListener = new TestRunListener() {

        @Override
        public void sessionStarted(ITestRunSession testRunSession) {
            fireActivity(new TestRunActivity(session.getLocalUser(),
                testRunSession.getTestRunName(), State.UNDEFINED));
        }

        @Override
        public void sessionFinished(ITestRunSession testRunSession) {

            Result testResult = testRunSession.getTestResult(false);
            State currentState = null;

            if (testResult == Result.UNDEFINED)
                currentState = State.UNDEFINED;
            else if (testResult == Result.OK)
                currentState = State.OK;
            else if (testResult == Result.ERROR)
                currentState = State.ERROR;
            else if (testResult == Result.FAILURE)
                currentState = State.FAILURE;
            else {
                // another state may be 'IGNORED' which is not interesting (now)
                return;
            }

            fireActivity(new TestRunActivity(session.getLocalUser(),
                testRunSession.getTestRunName(), currentState));
        }
    };

    @Override
    public void start() {
        session.addActivityProducer(this);
        session.addActivityConsumer(consumer);

        JUnitCore.addTestRunListener(testRunListener);
        LOG.debug("Added test run listener to the JUnitCore");
    }

    @Override
    public void stop() {
        session.removeActivityProducer(this);
        session.removeActivityConsumer(consumer);

        JUnitCore.removeTestRunListener(testRunListener);
        LOG.debug("Removed test run listener from the JUnitCore");
    }

    private void notifyListeners() {
        for (TestRunsListener listener : listeners) {
            listener.testRunChanged();
        }
    }

    /**
     * Adds the given {@link TestRunsListener} to the test run activities
     * manager.
     * 
     * @param listener
     *            The given {@link TestRunsListener} to add
     * */
    public void addTestRunListener(TestRunsListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the given {@link TestRunsListener} from the test run activities
     * manager.
     * 
     * @param listener
     *            The given {@link TestRunsListener} to add
     * */
    public void removeTestRunListener(TestRunsListener listener) {
        listeners.remove(listener);
    }
}
