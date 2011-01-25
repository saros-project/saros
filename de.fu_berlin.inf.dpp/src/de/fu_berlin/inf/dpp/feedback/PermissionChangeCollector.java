package de.fu_berlin.inf.dpp.feedback;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;

/**
 * Collects the {@link User.Permission} changes of the local user. It is
 * measured how long the user stayed in each {@link User.Permission} and how
 * often he changed it.<br>
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
public class PermissionChangeCollector extends AbstractStatisticCollector {

    protected static final Logger log = Logger
        .getLogger(PermissionChangeCollector.class.getName());

    /**
     * The map to hold the local users {@link User.Permission} changes and
     * times. A LinkedHashMap that preserves insertion order is used to be able
     * to calculate the time differences between two consecutive
     * {@link User.Permission} changes.
     */
    protected Map<Long, Permission> permissions = new LinkedHashMap<Long, Permission>();

    protected int countLocalPermissionChanges = 0;
    protected long sessionStart;
    protected long sessionTime;

    /**
     * accumulators for the total duration of the
     * {@link User.Permission#READONLY_ACCESS}
     */
    protected long readOnlyAccessDuration = 0;
    /**
     * accumulator for the total duration of the
     * {@link User.Permission#WRITE_ACCESS}
     */
    protected long writeAccessDuration = 0;

    protected ISharedProjectListener projectListener = new AbstractSharedProjectListener() {

        @Override
        public void permissionChanged(User user) {
            if (user.isLocal()) {
                permissions.put(System.currentTimeMillis(),
                    user.getPermission());
                ++countLocalPermissionChanges;

                assert countLocalPermissionChanges == (permissions.size() - 1);
            }
        }
    };

    public PermissionChangeCollector(StatisticManager statisticManager,
        SarosSessionManager sessionManager) {
        super(statisticManager, sessionManager);
    }

    @Override
    protected void processGatheredData() {
        int count = 0;

        // two consecutive permission change events e1, e2
        Entry<Long, Permission> e1 = null, e2 = null;
        Iterator<Entry<Long, Permission>> it = permissions.entrySet()
            .iterator();

        // store the total amount of permission changes
        data.setPermissionChanges(countLocalPermissionChanges);

        // store the time spent in each permission
        if (it.hasNext()) {
            e1 = it.next();

            while (it.hasNext()) {
                e2 = it.next();
                processSinglePermissionChange(e1.getKey(), e2.getKey(),
                    e1.getValue(), ++count);
                e1 = e2;
            }
            // the last permission lasted until now
            processSinglePermissionChange(e1.getKey(),
                System.currentTimeMillis(), e1.getValue(), ++count);

            assert count == permissions.size();
        } else {
            log.warn("The permission of the buddy couldn't be determined");
            data.setPermission(0, "none");
        }

        // store total duration and percentage for each possible permission
        data.setTotalPermissionDurationReadOnlyAccess(StatisticManager
            .getTimeInMinutes(readOnlyAccessDuration));
        data.setTotalPermissionDurationWriteAccess(StatisticManager
            .getTimeInMinutes(writeAccessDuration));
        data.setTotalPermissionPercentReadOnlyAccess(getPercentage(
            readOnlyAccessDuration, sessionTime));
        data.setTotalPermissionPercentWriteAccess(getPercentage(writeAccessDuration,
            sessionTime));
    }

    /**
     * Stores the {@link User.Permission} change in the data map (name and
     * duration) and accumulates the duration per {@link User.Permission}.
     * 
     * @param start
     *            the start time of the {@link User.Permission} change event in
     *            ms
     * @param end
     *            the end time of the {@link User.Permission} change event in ms
     * @param permission
     *            the {@link User.Permission} name
     * @param count
     *            the {@link User.Permission} number, starting with 1
     */
    protected void processSinglePermissionChange(long start, long end,
        Permission permission, int count) {
        long diffTime = getDiffTime(start, end);

        data.setPermission(count, permission.toString().toLowerCase());
        data.setPermissionDuration(count,
            StatisticManager.getTimeInMinutes(diffTime));

        // add diffTime depending on the permission to the right accumulator
        if (permission.equals(Permission.READONLY_ACCESS)) {
            readOnlyAccessDuration += diffTime;
        } else {
            writeAccessDuration += diffTime;
        }
    }

    protected long getDiffTime(long start, long end) {
        long diffTime = end - start;
        if (diffTime < 0) {
            log.warn("Time between two consecutive permission changes was negative "
                + diffTime + "ms. The Absolute value is used now.");
            diffTime = Math.abs(diffTime);
        }
        return diffTime;
    }

    @Override
    protected void clearPreviousData() {
        permissions.clear();
        countLocalPermissionChanges = 0;
        readOnlyAccessDuration = 0;
        writeAccessDuration = 0;
        super.clearPreviousData();
    }

    @Override
    protected void doOnSessionStart(ISarosSession sarosSession) {
        sessionStart = System.currentTimeMillis();

        sarosSession.addListener(projectListener);
        permissions.put(sessionStart, sarosSession.getLocalUser()
            .getPermission());
    }

    @Override
    protected void doOnSessionEnd(ISarosSession sarosSession) {
        sarosSession.removeListener(projectListener);
        sessionTime = Math.max(1, System.currentTimeMillis() - sessionStart);
    }

}
