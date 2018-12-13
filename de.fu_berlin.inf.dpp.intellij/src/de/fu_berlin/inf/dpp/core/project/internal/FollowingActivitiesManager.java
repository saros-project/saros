package de.fu_berlin.inf.dpp.core.project.internal;

import de.fu_berlin.inf.dpp.activities.StartFollowingActivity;
import de.fu_berlin.inf.dpp.activities.StopFollowingActivity;
import de.fu_berlin.inf.dpp.core.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer.Priority;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

/**
 * This manager is responsible for distributing knowledge about changes in follow modes between
 * session participants
 */
public class FollowingActivitiesManager extends AbstractActivityProducer implements Startable {

  private static final Logger LOG = Logger.getLogger(FollowingActivitiesManager.class);

  private final List<IFollowModeChangesListener> listeners =
      new CopyOnWriteArrayList<IFollowModeChangesListener>();

  private final ISarosSession session;

  private final AwarenessInformationCollector collector;

  private final EditorManager editor;

  private final ISharedEditorListener followModeListener =
      new ISharedEditorListener() {
        @Override
        public void followModeChanged(User target, boolean isFollowed) {

          if (isFollowed) {
            fireActivity(new StartFollowingActivity(session.getLocalUser(), target));
          } else {
            fireActivity(new StopFollowingActivity(session.getLocalUser()));
          }
        }
      };

  private final IActivityConsumer consumer =
      new AbstractActivityConsumer() {
        @Override
        public void receive(StartFollowingActivity activity) {
          final User source = activity.getSource();
          final User target = activity.getFollowedUser();

          if (LOG.isDebugEnabled()) {
            LOG.debug("received new follow mode from: " + source + " , followed: " + target);
          }

          collector.setUserFollowing(source, target);
          notifyListeners();
        }

        @Override
        public void receive(StopFollowingActivity activity) {
          User source = activity.getSource();

          if (LOG.isDebugEnabled()) {
            LOG.debug("user " + source + " stopped follow mode");
          }

          collector.setUserFollowing(source, null);
          notifyListeners();
        }
      };

  public FollowingActivitiesManager(
      final ISarosSession session,
      final AwarenessInformationCollector collector,
      final EditorManager editor) {
    this.session = session;
    this.collector = collector;
    this.editor = editor;
  }

  @Override
  public void start() {
    collector.flushFollowModes();
    session.addActivityProducer(this);
    session.addActivityConsumer(consumer, Priority.ACTIVE);
    editor.addSharedEditorListener(followModeListener);
  }

  @Override
  public void stop() {
    session.removeActivityProducer(this);
    session.removeActivityConsumer(consumer);
    editor.removeSharedEditorListener(followModeListener);
    collector.flushFollowModes();
  }

  private void notifyListeners() {
    for (IFollowModeChangesListener listener : listeners) {
      listener.followModeChanged();
    }
  }

  public void addListener(IFollowModeChangesListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(IFollowModeChangesListener listener) {
    this.listeners.remove(listener);
  }
}
