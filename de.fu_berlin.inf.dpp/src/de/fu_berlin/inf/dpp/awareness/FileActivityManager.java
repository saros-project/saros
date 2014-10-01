package de.fu_berlin.inf.dpp.awareness;

import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity.Type;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * This manager is responsible for distributing information about created files
 * by all session participants.
 * 
 * It listens for {@link FileActivity}'s for created elements and stores the
 * collected information in the {@link AwarenessInformationCollector}, when the
 * current user created a new file, independent of if it is a class, an
 * interface or anything else. Activities are sent around to all session
 * participants, if a session is active.
 * */
public class FileActivityManager extends AbstractActivityProducer implements
    Startable {

    private final ISarosSession session;
    private final AwarenessInformationCollector awarenessInformationCollector;

    /**
     * Constructs a new {@link FileActivityManager}.
     * 
     * @param session
     *            The session, in which the action awareness information are
     *            distributed.
     * @param awarenessInformationCollector
     *            This class is used to retrieve the updates from the received
     *            information about created files.
     * */
    public FileActivityManager(ISarosSession session,
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
        public void receive(FileActivity activity) {
            if (activity.getType() == Type.CREATED) {
                User user = activity.getSource();
                String file = activity.getPath().getFile().getName();
                awarenessInformationCollector.addCreatedFileName(user, file);
            }
        }
    };

    @Override
    public void start() {
        session.addActivityProducer(this);
        session.addActivityConsumer(consumer);
    }

    @Override
    public void stop() {
        session.removeActivityProducer(this);
        session.removeActivityConsumer(consumer);
    }
}