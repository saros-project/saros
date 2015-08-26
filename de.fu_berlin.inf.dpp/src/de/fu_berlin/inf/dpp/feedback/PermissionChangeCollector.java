package de.fu_berlin.inf.dpp.feedback;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.session.AbstractSessionListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISessionListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;

/**
 * Collects the {@link Permission} changes of the local user. It is measured how
 * long the user stayed in each {@link Permission} and how often he changed it.<br>
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
     * The map to hold the local users {@link Permission} changes and times. A
     * LinkedHashMap that preserves insertion order is used to be able to
     * calculate the time differences between two consecutive {@link Permission}
     * changes.
     */
    protected Map<Long, Permission> permissions = new LinkedHashMap<Long, Permission>();

    protected int countLocalPermissionChanges = 0;
    protected long sessionStart;
    protected long sessionTime;

    /**
     * accumulators for the total duration of the
     * {@link Permission#READONLY_ACCESS}
     */
    protected long readOnlyAccessDuration = 0;
    /**
     * accumulator for the total duration of the {@link Permission#WRITE_ACCESS}
     */
    protected long writeAccessDuration = 0;

    protected ISessionListener sessionListener = new AbstractSessionListener() {

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
        ISarosSession session) {
        super(statisticManager, session);
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
            log.warn("the permission of the user could not be determined");
            data.setPermission(0, "none");
        }

        // store total duration and percentage for each possible permission
        data.setTotalPermissionDurationReadOnlyAccess(StatisticManager
            .getTimeInMinutes(readOnlyAccessDuration));
        data.setTotalPermissionDurationWriteAccess(StatisticManager
            .getTimeInMinutes(writeAccessDuration));
        data.setTotalPermissionPercentReadOnlyAccess(getPercentage(
            readOnlyAccessDuration, sessionTime));
        data.setTotalPermissionPercentWriteAccess(getPercentage(
            writeAccessDuration, sessionTime));
    }

    /**
     * Stores the {@link Permission} change in the data map (name and duration)
     * and accumulates the duration per {@link Permission}.
     * 
     * @param start
     *            the start time of the {@link Permission} change event in ms
     * @param end
     *            the end time of the {@link Permission} change event in ms
     * @param permission
     *            the {@link Permission} name
     * @param count
     *            the {@link Permission} number, starting with 1
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
    protected void doOnSessionStart(ISarosSession sarosSession) {
        sessionStart = System.currentTimeMillis();

        sarosSession.addListener(sessionListener);
        permissions.put(sessionStart, sarosSession.getLocalUser()
            .getPermission());
    }

    @Override
    protected void doOnSessionEnd(ISarosSession sarosSession) {
        sarosSession.removeListener(sessionListener);
        sessionTime = Math.max(1, System.currentTimeMillis() - sessionStart);
    }

}
