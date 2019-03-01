package saros.feedback;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import saros.annotations.Component;
import saros.session.ISarosSession;
import saros.session.ISessionListener;
import saros.session.User;

/**
 * A Collector class that collects for each number of users the time they were present in the same
 * session, as well as the total number of users. The times each number of users were present are
 * accumulated.<br>
 * <br>
 * <code>
 * session.time.users.1=0.11            <br>
 * session.time.users.2=23.86           <br>
 * session.time.percent.users.1=1       <br>
 * session.time.percent.users.2=99      <br>
 * session.users.total=3
 * </code><br>
 * <br>
 * The example shows that there might have been three different users, but they were never present
 * altogether at the same time.
 *
 * @author Lisa Dohrmann
 */
@Component(module = "feedback")
public class ParticipantCollector extends AbstractStatisticCollector {

  private static final String KEY_PERCENT = "percent";

  private static final String KEY_SESSION_TIME_USERS = "session.time.users";
  private static final String KEY_SESSION_USERS_TOTAL = "session.users.total";

  /** a map to contain the number of participants and the associated times */
  private Map<Integer, Long> participantTimes = new HashMap<Integer, Long>();

  /** a set to contain all users that participated in the session */
  private Set<User> users = new HashSet<User>();

  private long timeOfLastEvent;

  /** contains the current number of participants of the session at each point in time */
  private int currentNumberOfParticipants = 0;

  private long sessionStart;
  private long sessionTime;

  private final ISessionListener sessionListener =
      new ISessionListener() {

        @Override
        public void userJoined(User user) {
          currentNumberOfParticipants++;
          handleUserEvent(currentNumberOfParticipants - 1);

          users.add(user);
        }

        @Override
        public void userLeft(User user) {
          currentNumberOfParticipants--;

          handleUserEvent(currentNumberOfParticipants + 1);
        }
      };

  public ParticipantCollector(StatisticManager statisticManager, ISarosSession session) {
    super(statisticManager, session);
  }

  /**
   * Handles events in which the number of participants changed (session started, user joined, user
   * left, session ended). The time difference to the last event is stored, together with the number
   * of participants that were present in this time frame.
   *
   * <p>If the same number of participants were together earlier in the session, the time difference
   * is added to the existing time frame.
   */
  private void handleUserEvent(int numberOfParticipants) {
    Long timeDiff = System.currentTimeMillis() - timeOfLastEvent;
    Long time = participantTimes.get(numberOfParticipants);

    time = (time == null) ? timeDiff : time + timeDiff;
    participantTimes.put(numberOfParticipants, time);

    // store the time of this event
    timeOfLastEvent = System.currentTimeMillis();
  }

  @Override
  protected void processGatheredData() {

    // store the number of users and associated times
    for (Entry<Integer, Long> e : participantTimes.entrySet())
      storeSessionTimeForUsers(e.getKey(), e.getValue(), sessionTime);

    data.put(KEY_SESSION_USERS_TOTAL, users.size());
  }

  @Override
  protected void doOnSessionStart(ISarosSession sarosSession) {
    sarosSession.addListener(sessionListener);

    sessionStart = System.currentTimeMillis();
    timeOfLastEvent = sessionStart;

    users.addAll(sarosSession.getUsers());
    currentNumberOfParticipants = users.size();

    handleUserEvent(currentNumberOfParticipants);
  }

  @Override
  protected void doOnSessionEnd(ISarosSession sarosSession) {
    sarosSession.removeListener(sessionListener);

    sessionTime = Math.max(1, System.currentTimeMillis() - sessionStart);
    handleUserEvent(currentNumberOfParticipants);
  }

  private void storeSessionTimeForUsers(
      int numberOfUsers, long duration, long totalSessionDurationTime) {

    data.put(KEY_SESSION_TIME_USERS, StatisticManager.getTimeInMinutes(duration), numberOfUsers);

    data.put(
        KEY_SESSION_TIME_USERS,
        getPercentage(duration, totalSessionDurationTime),
        numberOfUsers,
        KEY_PERCENT);
  }
}
