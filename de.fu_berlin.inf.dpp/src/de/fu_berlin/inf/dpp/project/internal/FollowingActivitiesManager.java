package de.fu_berlin.inf.dpp.project.internal;

import de.fu_berlin.inf.dpp.activities.StartFollowingActivity;
import de.fu_berlin.inf.dpp.activities.StopFollowingActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.editor.FollowModeManager;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer.Priority;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISessionListener;
import de.fu_berlin.inf.dpp.session.User;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

// TODO evaluate what logic can moved to the core. The AwarenessInformationCollector is a singleton
// Application Runtime component, only used for the Eclipse UI. It is not the best choice to move it
// also to the core.
/**
 * This manager is responsible for collection changes in follow modes between session participants.
 * It consumes activities.
 *
 * @author Alexander Waldmann (contact@net-corps.de)
 */
@Component(module = "core")
public class FollowingActivitiesManager extends AbstractActivityProducer implements Startable {

  private static final Logger LOG = Logger.getLogger(FollowingActivitiesManager.class);

  private final List<IFollowModeChangesListener> listeners =
      new CopyOnWriteArrayList<IFollowModeChangesListener>();

  private final ISarosSession session;

  private final AwarenessInformationCollector collector;

  private final IActivityConsumer consumer =
      new AbstractActivityConsumer() {
        @Override
        public void receive(StartFollowingActivity activity) {
          final User source = activity.getSource();
          final User target = activity.getFollowedUser();

          if (LOG.isDebugEnabled())
            LOG.debug("received new follow mode from: " + source + " , followed: " + target);

          collector.setUserFollowing(source, target);
          notifyListeners();
        }

        @Override
        public void receive(StopFollowingActivity activity) {
          User source = activity.getSource();

          if (LOG.isDebugEnabled()) LOG.debug("user " + source + " stopped follow mode");

          collector.setUserFollowing(source, null);
          notifyListeners();
        }
      };

  private final ISessionListener sessionListener =
      new ISessionListener() {
        @Override
        public void userLeft(final User user) {
          collector.setUserFollowing(user, null);
          notifyListeners();
        }
      };

  public FollowingActivitiesManager(
      final ISarosSession session,
      final AwarenessInformationCollector collector,
      final FollowModeManager followModeManager) {
    this.session = session;
    this.collector = collector;
  }

  @Override
  public void start() {
    collector.flushFollowModes();
    session.addActivityProducer(this);
    session.addActivityConsumer(consumer, Priority.ACTIVE);
    session.addListener(sessionListener);
  }

  @Override
  public void stop() {
    session.removeActivityProducer(this);
    session.removeActivityConsumer(consumer);
    session.removeListener(sessionListener);
    collector.flushFollowModes();
  }

  private void notifyListeners() {
    for (IFollowModeChangesListener listener : listeners) listener.followModeChanged();
  }

  public void addListener(IFollowModeChangesListener listener) {
    listeners.add(listener);
  }

  public void removeListener(IFollowModeChangesListener listener) {
    listeners.remove(listener);
  }
}
