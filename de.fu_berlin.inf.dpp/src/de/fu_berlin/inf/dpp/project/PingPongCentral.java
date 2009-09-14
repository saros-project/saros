package de.fu_berlin.inf.dpp.project;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.joda.time.Duration;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.PingPongActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.AutoHashMap;
import de.fu_berlin.inf.dpp.util.Function;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.Util;

public class PingPongCentral extends AbstractActivityProvider {

    private static final Logger log = Logger.getLogger(PingPongCentral.class);

    protected ISharedProject sharedProject;

    protected SessionManager sessionManager;

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

        public void add(PingPongActivity pingPongActivity) {

            // This is the reply to a ping the local user sent himself
            Duration rtt = pingPongActivity.getRoundtripTime();

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
                    Util.runSafeSWTSync(log, new Runnable() {
                        public void run() {
                            sendPings();
                        }
                    });
                }
            }, 10, 10, TimeUnit.SECONDS);
        }
    };

    public PingPongCentral(SessionManager manager) {
        this.sessionManager = manager;
        this.sessionManager.addSessionListener(sessionListener);
    }

    public void dispose() {
        this.sessionManager.removeSessionListener(sessionListener);
    }

    protected IActivityReceiver activityReceiver = new AbstractActivityReceiver() {
        @Override
        public void receive(PingPongActivity pingPongActivity) {

            User initiator = sharedProject.getUser(pingPongActivity
                .getInitiator());
            User sender = sharedProject.getUser(new JID(pingPongActivity
                .getSource()));

            if (initiator.isLocal()) {

                stats.get(sender).add(pingPongActivity);

            } else {
                // This is the ping from another user
                sharedProject.sendActivity(initiator, pingPongActivity
                    .createPong(sharedProject.getLocalUser()));
            }
        }
    };

    @Override
    public void exec(IActivity activity) {
        activity.dispatch(activityReceiver);
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

            sharedProject.sendActivity(remoteUser, PingPongActivity
                .create(sharedProject.getLocalUser()));

        }
    }
}
