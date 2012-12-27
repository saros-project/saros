package de.fu_berlin.inf.dpp.net;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.PingPongActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.util.AutoHashMap;
import de.fu_berlin.inf.dpp.util.Function;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.Utils;

@Component(module = "net")
public class PingPongCentral extends AbstractActivityProvider implements
    Startable {

    private static final Logger log = Logger.getLogger(PingPongCentral.class);

    protected final ISarosSession sarosSession;

    protected boolean sendPings = false;

    protected AutoHashMap<User, PingStats> stats = new AutoHashMap<User, PingStats>(
        new Function<User, PingStats>() {
            @Override
            public PingStats apply(User newKey) {
                return new PingStats(newKey);
            }
        });

    public static class PingStats {

        protected Duration slidingAverageOfFive = new Duration(0);

        protected LinkedList<Duration> slidingAverage = new LinkedList<Duration>();

        protected Duration sessionAverage = new Duration(0);

        protected int pingsReceived = 0;

        protected int pingsSent = 0;

        protected User user;

        protected DateTime lastSeen;

        public PingStats(User user) {
            this.user = user;
        }

        public void add(PingPongActivity pingPongActivityDataObject) {

            // This is the reply to a ping the local user sent himself
            Duration rtt = pingPongActivityDataObject.getRoundtripTime();

            lastSeen = new DateTime();

            sessionAverage = sessionAverage.plus(rtt);

            slidingAverage.add(rtt);
            slidingAverageOfFive = slidingAverageOfFive.plus(rtt);
            if (slidingAverage.size() > 5) {
                slidingAverageOfFive = slidingAverageOfFive
                    .minus(slidingAverage.removeFirst());
            }
            pingsReceived++;

            if (log.isTraceEnabled())
                log.trace(this.toString());
        }

        @Override
        public String toString() {

            // TODO Internationalized ?
            String windowText = "";
            int n = slidingAverage.size();
            if (n > 0) {
                windowText = "Last " + n + " == "
                    + (slidingAverageOfFive.getMillis() / n) + "ms - Last == "
                    + slidingAverage.getLast().getMillis() + "ms - ";
            }

            return "Ping Stats "
                + Utils.prefix(user.getJID())
                + "Round trip time: All == "
                + getAverageRoundTripTime()
                + "ms - "
                + windowText
                + "Pings received/sent == "
                + pingsReceived
                + "/"
                + pingsSent
                + " ("
                + String.format("%.1f", getPingSuccessPercentage())
                + "%)"
                + (lastSeen != null ? " - last seen: "
                    + new Duration(lastSeen, new DateTime()).getMillis()
                    + "ms ago" : "");
        }

        protected double getPingSuccessPercentage() {
            if (pingsSent <= 0) {
                return 100.0;
            } else {
                return 100.0 * pingsReceived / pingsSent;
            }
        }

        /**
         * Average round trip time or -1 if no pings have yet received.
         */
        protected long getAverageRoundTripTime() {
            if (pingsReceived <= 0)
                return -1;
            return (sessionAverage.getMillis() / pingsReceived);
        }
    }

    protected ScheduledExecutorService scheduler = Executors
        .newScheduledThreadPool(1, new NamedThreadFactory("PingPongScheduler"));

    protected AtomicReference<ScheduledFuture<?>> pingPongHandle = new AtomicReference<ScheduledFuture<?>>(
        null);

    @Override
    public void stop() {
        sarosSession.removeActivityProvider(this);

        ScheduledFuture<?> pingPongFuture = pingPongHandle.getAndSet(null);

        if (pingPongFuture != null)
            pingPongFuture.cancel(true);
        else
            log.warn("saros session listener error, session ended but has not started at all");

        log.info(toString());
        scheduler.shutdown();
    }

    @Override
    public void start() {
        sarosSession.addActivityProvider(this);
        pingPongHandle.set(scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!sendPings)
                    return;
                SWTUtils.runSafeSWTSync(log, new Runnable() {
                    @Override
                    public void run() {
                        if (log.isDebugEnabled()) {
                            log.debug(toString());
                        }
                        sendPings();
                    }
                });
            }
        }, 10, 10, TimeUnit.SECONDS));
    }

    public PingPongCentral(Saros saros, ISarosSession session,
        PreferenceUtils preferenceUtils) {

        this.sarosSession = session;
        this.sendPings = preferenceUtils.isPingPongActivated();
        /*
         * register a property change listeners to keep sendPings up-to-date
         */
        saros.getPreferenceStore().addPropertyChangeListener(propertyListener);
    }

    protected IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(PreferenceConstants.PING_PONG)) {
                Object value = event.getNewValue();
                // make sure the cast will work
                if (value instanceof Boolean) {
                    sendPings = ((Boolean) value).booleanValue();
                } else {
                    log.warn("Preference value PING_PONG"
                        + " is supposed to be a boolean, but it unexpectedly"
                        + " changed to a different type!");
                }
            }
        }
    };

    protected IActivityReceiver activityDataObjectReceiver = new AbstractActivityReceiver() {
        @Override
        public void receive(PingPongActivity pingPongActivity) {

            User initiator = pingPongActivity.getInitiator();

            if (initiator.isLocal()) {
                User sender = pingPongActivity.getSource();
                stats.get(sender).add(pingPongActivity);
            } else {
                // This is the ping from another user
                fireActivity(initiator,
                    pingPongActivity.createPong(sarosSession.getLocalUser()));
            }
        }
    };

    @Override
    public void exec(IActivity activityDataObject) {
        activityDataObject.dispatch(activityDataObjectReceiver);
    }

    /**
     * @swt
     */
    protected synchronized void sendPings() {
        List<User> remoteUsers = sarosSession.getRemoteUsers();

        for (User remoteUser : remoteUsers) {

            stats.get(remoteUser).pingsSent++;

            fireActivity(remoteUser,
                PingPongActivity.create(sarosSession.getLocalUser()));

        }
    }

    @Override
    public synchronized String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("Ping statistics:\n");

        // First to host
        for (Entry<User, PingStats> entry : stats.entrySet()) {
            if (entry.getKey().isHost())
                sb.append("  ").append(entry.getValue()).append("\n");
        }

        // Next to everybody else
        for (Entry<User, PingStats> entry : stats.entrySet()) {
            if (entry.getKey().isClient())
                sb.append("  ").append(entry.getValue()).append("\n");
        }
        return sb.toString().trim();
    }

}
