package de.fu_berlin.inf.dpp.project;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.joda.time.Duration;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.AbstractActivityDataObjectReceiver;
import de.fu_berlin.inf.dpp.activities.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.IActivityDataObjectReceiver;
import de.fu_berlin.inf.dpp.activities.serializable.PingPongActivityDataObject;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.util.AutoHashMap;
import de.fu_berlin.inf.dpp.util.Function;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.Util;

@Component(module = "net")
public class PingPongCentral extends AbstractActivityProvider {

    private static final Logger log = Logger.getLogger(PingPongCentral.class);

    protected ISharedProject sharedProject;

    protected SessionManager sessionManager;

    protected boolean sendPings = false;

    protected AutoHashMap<User, PingStats> stats = new AutoHashMap<User, PingStats>(
        new Function<User, PingStats>() {
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

        public PingStats(User user) {
            this.user = user;
        }

        public void add(PingPongActivityDataObject pingPongActivityDataObject) {

            // This is the reply to a ping the local user sent himself
            Duration rtt = pingPongActivityDataObject.getRoundtripTime();

            sessionAverage = sessionAverage.plus(rtt);

            slidingAverage.add(rtt);
            slidingAverageOfFive = slidingAverageOfFive.plus(rtt);
            if (slidingAverage.size() > 5) {
                slidingAverageOfFive = slidingAverageOfFive
                    .minus(slidingAverage.removeFirst());
            }
            pingsReceived++;

            if (log.isDebugEnabled())
                log.debug(this.toString());
        }

        @Override
        public String toString() {

            String windowText = "";
            int n = slidingAverage.size();
            if (n > 0) {
                windowText = "Last " + n + " == "
                    + (slidingAverageOfFive.getMillis() / n) + "ms - Last == "
                    + slidingAverage.getLast().getMillis() + "ms - ";
            }

            return "Ping Stats " + Util.prefix(user.getJID())
                + "Round trip time: All == "
                + (sessionAverage.getMillis() / pingsReceived) + "ms - "
                + windowText + "Pings recieved/sent == " + pingsReceived + "/"
                + pingsSent + " ("
                + String.format("%.1f", 100.0 * pingsReceived / pingsSent)
                + "%)";
        }
    }

    protected ScheduledExecutorService scheduler = Executors
        .newScheduledThreadPool(1, new NamedThreadFactory("PingPongScheduler"));

    protected ScheduledFuture<?> pingPongHandle = null;

    protected ISessionListener sessionListener = new AbstractSessionListener() {
        @Override
        public void sessionEnded(ISharedProject newSharedProject) {

            newSharedProject.removeActivityProvider(PingPongCentral.this);

            pingPongHandle.cancel(true);
            sharedProject = null;

            // Print results

            synchronized (PingPongCentral.this) {

                // First to host
                for (Entry<User, PingStats> entry : stats.entrySet()) {
                    if (entry.getKey().isHost())
                        log.info(entry.getValue());
                }

                // Next to everybody else
                for (Entry<User, PingStats> entry : stats.entrySet()) {
                    if (entry.getKey().isClient())
                        log.info(entry.getValue());
                }
            }

        }

        @Override
        public void sessionStarted(ISharedProject newSharedProject) {
            sharedProject = newSharedProject;

            stats.clear();

            newSharedProject.addActivityProvider(PingPongCentral.this);

            pingPongHandle = scheduler.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    if (!sendPings)
                        return;
                    Util.runSafeSWTSync(log, new Runnable() {
                        public void run() {
                            sendPings();
                        }
                    });
                }
            }, 10, 10, TimeUnit.SECONDS);
        }
    };

    public PingPongCentral(Saros saros, SessionManager manager,
        PreferenceUtils preferenceUtils) {
        this.sessionManager = manager;
        this.sessionManager.addSessionListener(sessionListener);

        this.sendPings = preferenceUtils.isPingPongActivated();
        /*
         * register a property change listeners to keep sendPings up-to-date
         */
        saros.getPreferenceStore().addPropertyChangeListener(propertyListener);
    }

    protected IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
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

    public void dispose() {
        this.sessionManager.removeSessionListener(sessionListener);
    }

    protected IActivityDataObjectReceiver activityDataObjectReceiver = new AbstractActivityDataObjectReceiver() {
        @Override
        public void receive(PingPongActivityDataObject pingPongActivityDataObject) {

            User initiator = sharedProject.getUser(pingPongActivityDataObject
                .getInitiator());
            User sender = sharedProject.getUser(pingPongActivityDataObject.getSource());

            if (initiator.isLocal()) {

                stats.get(sender).add(pingPongActivityDataObject);

            } else {
                // This is the ping from another user
                sharedProject.sendActivity(initiator, pingPongActivityDataObject
                    .createPong(sharedProject.getLocalUser()));
            }
        }
    };

    @Override
    public void exec(IActivityDataObject activityDataObject) {
        activityDataObject.dispatch(activityDataObjectReceiver);
    }

    /**
     * @swt
     */
    protected synchronized void sendPings() {

        if (sharedProject == null)
            return;

        List<User> remoteUsers = sharedProject.getRemoteUsers();

        for (User remoteUser : remoteUsers) {

            stats.get(remoteUser).pingsSent++;

            sharedProject.sendActivity(remoteUser, PingPongActivityDataObject
                .create(sharedProject.getLocalUser()));

        }
    }
}
