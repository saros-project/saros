package saros.feedback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import saros.annotations.Component;
import saros.editor.FollowModeManager;
import saros.editor.IFollowModeListener;
import saros.session.ISarosSession;
import saros.session.User;

/**
 * A Collector class that collects the number of local follow-mode toggles executed by a user and
 * the time spent in follow-mode (total time as well as a percentage of total session time).
 *
 * <p><code>
 * session.followmode.toggles=7         <br>
 * session.followmode.time.percent=25   <br>
 * session.followmode.time.total=12
 * </code> <br>
 */
@Component(module = "feedback")
public class FollowModeCollector extends AbstractStatisticCollector {

  private static final Logger log = Logger.getLogger(FollowModeCollector.class);

  /** Percentage of time in respect to total session length spent in follow mode */
  private static final String KEY_FOLLOWMODE_PERCENT = "followmode.time.percent";

  /** Total count of follow mode toggles */
  private static final String KEY_FOLLOWMODE_TOGGLES = "followmode.toggles";

  /** Total time spent in follow mode */
  private static final String KEY_FOLLOWMODE_TOTAL = "followmode.time.total";

  /**
   * A FollowModeEvent has a <code>boolean</code> attribute enabled for the state of follow mode and
   * a <code>long</code> which declares the time when the toggle occurred
   */
  private static class FollowModeToggleEvent {
    long time;
    boolean enabled;

    public FollowModeToggleEvent(long time, boolean enabled) {
      this.time = time;
      this.enabled = enabled;
    }
  }

  /** starting time of the session */
  private long sessionStart = 0;

  /** ending time of the session */
  private long sessionEnd = 0;

  /** accumulated time spent in follow mode */
  private long timeInFollowMode = 0;

  /** length of the session */
  private long sessionDuration = 0;

  /** total count of the follow mode toggles */
  private int countFollowModeChanges = 0;

  /** Structure to store local follow mode toggle events */
  private List<FollowModeToggleEvent> followModeChangeEvents =
      Collections.synchronizedList(new ArrayList<FollowModeToggleEvent>());

  private FollowModeManager followModeManager;

  private IFollowModeListener followModeListener =
      new IFollowModeListener() {

        @Override
        public void stoppedFollowing(Reason reason) {
          logEvent(false);
        }

        @Override
        public void startedFollowing(User target) {
          logEvent(true);
        }

        private void logEvent(boolean isFollowed) {
          FollowModeToggleEvent event =
              new FollowModeToggleEvent(System.currentTimeMillis(), isFollowed);

          followModeChangeEvents.add(event);
          ++countFollowModeChanges;
        }
      };

  public FollowModeCollector(
      StatisticManager statisticManager,
      ISarosSession session,
      FollowModeManager followModeManager) {
    super(statisticManager, session);

    this.followModeManager = followModeManager;
  }

  @Override
  protected void processGatheredData() {
    /*
     * local variable where total time where follow mode was enabled is
     * accumulated
     */
    long timeFollowModeEnabled = 0;

    sessionDuration = getDiffTime(sessionStart, sessionEnd);

    /*
     * iterate through the array list and calculate time spent in follow
     */
    for (Iterator<FollowModeToggleEvent> iterator = followModeChangeEvents.iterator();
        iterator.hasNext(); ) {
      FollowModeToggleEvent currentEntry = iterator.next();

      if (currentEntry.enabled) {
        timeFollowModeEnabled = currentEntry.time;
      } else {
        timeInFollowMode += getDiffTime(timeFollowModeEnabled, currentEntry.time);
      }
    }

    data.put(KEY_FOLLOWMODE_TOGGLES, countFollowModeChanges);

    data.put(KEY_FOLLOWMODE_TOTAL, StatisticManager.getTimeInMinutes(timeInFollowMode));

    data.put(KEY_FOLLOWMODE_PERCENT, getPercentage(timeInFollowMode, sessionDuration));
  }

  /** auxiliary function for calculating duration between <code>start and <end>end</code> */
  private long getDiffTime(long start, long end) {
    long diffTime = end - start;
    if (diffTime < 0) {
      log.warn("Time was negative " + diffTime + "ms. The absolute value is being used.");
      diffTime = Math.abs(diffTime);
    }
    return diffTime;
  }

  @Override
  protected void doOnSessionStart(ISarosSession sarosSession) {
    // get starting time of session
    sessionStart = System.currentTimeMillis();

    followModeManager.addListener(followModeListener);
  }

  @Override
  protected void doOnSessionEnd(ISarosSession sarosSession) {
    // get the time the session ended
    sessionEnd = System.currentTimeMillis();

    followModeManager.removeListener(followModeListener);
  }
}
