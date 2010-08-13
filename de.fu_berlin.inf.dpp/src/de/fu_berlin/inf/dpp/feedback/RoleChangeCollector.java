package de.fu_berlin.inf.dpp.feedback;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SessionManager;

/**
 * Collects the role changes of the local user. It is measured how long the user
 * stayed in each role and how often he changed it.<br>
 * <br>
 * <code>
 * role.1=observer               <br>
 * role.1.duration=1.03          <br>
 * role.2=driver                 <br>
 * role.2.duration=6.18          <br>
 * role.observer.percent=14      <br>
 * role.driver.percent=86        <br>
 * role.changes=1
 * </code>
 * 
 * @author Lisa Dohrmann
 */
@Component(module = "feedback")
public class RoleChangeCollector extends AbstractStatisticCollector {

    protected static final Logger log = Logger
        .getLogger(RoleChangeCollector.class.getName());

    /**
     * The map to hold the local users role changes and times. A LinkedHashMap
     * that preserves insertion order is used to be able to calculate the time
     * differences between two consecutive role changes.
     */
    protected Map<Long, UserRole> roles = new LinkedHashMap<Long, UserRole>();

    protected int countLocalRoleChanges = 0;
    protected long sessionStart;
    protected long sessionTime;

    /** accumulators for the total duration of the observer role */
    protected long observerDuration = 0;
    /** accumulator for the total duration of the driver role */
    protected long driverDuration = 0;

    protected ISharedProjectListener projectListener = new AbstractSharedProjectListener() {

        @Override
        public void roleChanged(User user) {
            if (user.isLocal()) {
                roles.put(System.currentTimeMillis(), user.getUserRole());
                ++countLocalRoleChanges;

                assert countLocalRoleChanges == (roles.size() - 1);
            }
        }
    };

    public RoleChangeCollector(StatisticManager statisticManager,
        SessionManager sessionManager) {
        super(statisticManager, sessionManager);
    }

    @Override
    protected void processGatheredData() {
        int count = 0;

        // two consecutive role change events e1, e2
        Entry<Long, UserRole> e1 = null, e2 = null;
        Iterator<Entry<Long, UserRole>> it = roles.entrySet().iterator();

        // store the total amount of role changes
        data.setRoleChanges(countLocalRoleChanges);

        // store the time spent in each role
        if (it.hasNext()) {
            e1 = it.next();

            while (it.hasNext()) {
                e2 = it.next();
                processSingleRoleChange(e1.getKey(), e2.getKey(),
                    e1.getValue(), ++count);
                e1 = e2;
            }
            // the last role lasted until now
            processSingleRoleChange(e1.getKey(), System.currentTimeMillis(),
                e1.getValue(), ++count);

            assert count == roles.size();
        } else {
            log.warn("The role of the user couldn't be determined");
            data.setRole(0, "none");
        }

        // store total duration and percentage for each possible role
        data.setTotalRoleDurationObserver(StatisticManager
            .getTimeInMinutes(observerDuration));
        data.setTotalRoleDurationDriver(StatisticManager
            .getTimeInMinutes(driverDuration));
        data.setTotalRolePercentObserver(getPercentage(observerDuration,
            sessionTime));
        data.setTotalRolePercentDriver(getPercentage(driverDuration,
            sessionTime));
    }

    /**
     * Stores the role change in the data map (name and duration) and
     * accumulates the duration per role.
     * 
     * @param start
     *            the start time of the role change event in ms
     * @param end
     *            the end time of the role change event in ms
     * @param role
     *            the role name
     * @param count
     *            the role number, starting with 1
     */
    protected void processSingleRoleChange(long start, long end, UserRole role,
        int count) {
        long diffTime = getDiffTime(start, end);

        data.setRole(count, role.toString().toLowerCase());
        data.setRoleDuration(count, StatisticManager.getTimeInMinutes(diffTime));

        // add diffTime depending on the role to the right accumulator
        if (role.equals(UserRole.OBSERVER)) {
            observerDuration += diffTime;
        } else {
            driverDuration += diffTime;
        }
    }

    protected long getDiffTime(long start, long end) {
        long diffTime = end - start;
        if (diffTime < 0) {
            log.warn("Time between two consecutive role changes was negative "
                + diffTime + "ms. The Absolute value is used now.");
            diffTime = Math.abs(diffTime);
        }
        return diffTime;
    }

    @Override
    protected void clearPreviousData() {
        roles.clear();
        countLocalRoleChanges = 0;
        observerDuration = 0;
        driverDuration = 0;
        super.clearPreviousData();
    }

    @Override
    protected void doOnSessionStart(ISarosSession sarosSession) {
        sessionStart = System.currentTimeMillis();

        sarosSession.addListener(projectListener);
        roles.put(sessionStart, sarosSession.getLocalUser().getUserRole());
    }

    @Override
    protected void doOnSessionEnd(ISarosSession sarosSession) {
        sarosSession.removeListener(projectListener);
        sessionTime = Math.max(1, System.currentTimeMillis() - sessionStart);
    }

}
