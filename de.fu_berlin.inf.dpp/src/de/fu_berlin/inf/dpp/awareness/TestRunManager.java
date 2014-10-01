package de.fu_berlin.inf.dpp.awareness;

import org.apache.log4j.Logger;
import org.eclipse.jdt.junit.JUnitCore;
import org.eclipse.jdt.junit.TestRunListener;
import org.eclipse.jdt.junit.model.ITestElement.Result;
import org.eclipse.jdt.junit.model.ITestRunSession;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.TestRunActivity;
import de.fu_berlin.inf.dpp.activities.TestRunActivity.State;
import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChatService;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.ui.model.session.SessionContentProvider;

/**
 * This manager is responsible for distributing information about test runs of
 * all session participants.
 * <p>
 * It listens on the {@link org.eclipse.jdt.junit.JUnitCore} for started,
 * running and completed tests and stores the collected information in the
 * {@link AwarenessInformationCollector}, when the current user has started and
 * finished a test run.
 * <p>
 * Note: This class is just instantiated when the JUnitCore bundle is available.
 * */
public class TestRunManager extends AbstractActivityProducer implements
    Startable {

    private static final Logger LOG = Logger.getLogger(TestRunManager.class);

    private final ISarosSession session;
    private final AwarenessInformationCollector awarenessInformationCollector;

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
     * */
    public TestRunManager(ISarosSession session,
        AwarenessInformationCollector awarenessInformationCollector,
        MultiUserChatService mucs) {
        this.session = session;
        this.awarenessInformationCollector = awarenessInformationCollector;
    }

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        @Override
        public void exec(IActivity activity) {
            activity.dispatch(receiver);
        }
    };

    private IActivityReceiver receiver = new AbstractActivityReceiver() {
        @Override
        public void receive(TestRunActivity activity) {
            awarenessInformationCollector.addTestRun(activity.getSource(),
                activity.getName(), activity.getState());
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
}