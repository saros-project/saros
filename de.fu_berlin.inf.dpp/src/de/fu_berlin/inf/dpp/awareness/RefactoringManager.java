package de.fu_berlin.inf.dpp.awareness;

import org.apache.log4j.Logger;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.RefactoringActivity;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;

/**
 * This manager is responsible for distributing information about refactoring
 * performed by all session participants.
 * 
 * It listens on the {@link IRefactoringHistoryService} for refactoring which
 * are performed and stores the collected information in the
 * {@link AwarenessInformationCollector}, when the current user refactored
 * something. Activities are sent around to all session participants, if a
 * session is active.
 * */
public class RefactoringManager extends AbstractActivityProducer implements
    Startable {

    private static final Logger LOG = Logger
        .getLogger(RefactoringManager.class);

    private final ISarosSession session;
    private final AwarenessInformationCollector awarenessInformationCollector;

    /**
     * Constructs a new {@link RefactoringManager}.
     * 
     * @param session
     *            The session, in which the action awareness information are
     *            distributed.
     * @param awarenessInformationCollector
     *            This class is used to retrieve the updates from the received
     *            information about performed refactorings.
     * */
    public RefactoringManager(ISarosSession session,
        AwarenessInformationCollector awarenessInformationCollector) {
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
        public void receive(RefactoringActivity activity) {
            awarenessInformationCollector.addRefactoring(activity.getSource(),
                activity.getDescription());
        }
    };

    /**
     * Listener for performed refactorings
     * */
    private final IRefactoringExecutionListener refactoringListener = new IRefactoringExecutionListener() {

        @Override
        public void executionNotification(RefactoringExecutionEvent event) {
            if (event.getEventType() == RefactoringExecutionEvent.PERFORMED) {
                String description = event.getDescriptor().getDescription();
                fireActivity(new RefactoringActivity(session.getLocalUser(),
                    description));
            }
        }
    };

    @Override
    public void start() {
        session.addActivityProducer(this);
        session.addActivityConsumer(consumer);

        IRefactoringHistoryService service = RefactoringCore
            .getHistoryService();
        service.addExecutionListener(refactoringListener);
        LOG.debug("Added refactoring listener to the IRefactoringHistoryService");
    }

    @Override
    public void stop() {
        session.removeActivityProducer(this);
        session.removeActivityConsumer(consumer);

        IRefactoringHistoryService service = RefactoringCore
            .getHistoryService();
        service.removeExecutionListener(refactoringListener);
        LOG.debug("Removed refactoring listener from the IRefactoringHistoryService");
    }
}