package de.fu_berlin.inf.dpp.feedback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;

/**
 * The SessionStatistic class wraps a Properties object in which the gathered
 * statistical data can be stored as simple key/value pairs. This data can then
 * be saved to disk as a file.
 * 
 * @author Lisa Dohrmann
 */
public class SessionStatistic {

    protected static final Logger log = Logger.getLogger(SessionStatistic.class
        .getName());

    protected static final String SAROS_VERSION = "saros.version";
    protected static final String JAVA_VERSION = "java.version";
    protected static final String OS_NAME = "os.name";
    protected static final String ECLIPSE_VERSION = "eclipse.version";

    protected static final String KEY_USER_ID = "random.user.id";

    protected static final String KEY_FEEDBACK_DISABLED = "feedback.disabled";
    protected static final String KEY_FEEDBACK_INTERVAL = "feedback.survey.interval";

    protected static final String KEY_SESSION_COUNT = "session.count";
    protected static final String KEY_SESSION_TIME = "session.time";
    protected static final String KEY_SESSION_TIME_USERS = "session.time.users";
    protected static final String KEY_SESSION_TIME_PERCENT_USERS = "session.time.percent.users";
    protected static final String KEY_SESSION_ID = "session.id";
    protected static final String KEY_SESSION_USERS_TOTAL = "session.users.total";

    protected static final String KEY_ROLE = "role";
    protected static final String KEY_ROLE_TIME = "role.time";
    protected static final String KEY_ROLE_TIME_PERCENT = "role.time.percent";
    protected static final String KEY_ROLE_CHANGES = "role.changes";

    protected static final String KEY_TEXTEDIT_CHARS = "textedit.chars";
    protected static final String KEY_TEXTEDIT_COUNT = "textedit.count";
    protected static final String KEY_PARALLEL_TEXT_EDITS = "textedits.parallel.chars.interval";
    protected static final String KEY_PARALLEL_TEXT_EDITS_PERCENT = "textedits.parallel.percent.interval";

    // Keys for DataTransferCollector
    protected static final String KEY_TRANSFER_STATS = "data_transfer";
    protected static final Object TRANSFER_STATS_EVENT_SUFFIX = "number_of_events";
    // Total size in KB
    protected static final Object TRANSFER_STATS_SIZE_SUFFIX = "total_size_kb";
    // Total size for transfers in milliseconds
    protected static final Object TRANSFER_STATS_TIME_SUFFIX = "total_time_ms";
    // Convenience value of total_size / total_time in KB/s
    protected static final Object TRANSFER_STATS_THROUGHPUT_SUFFIX = "average_throughput_kbs";

    /**
     * This is the {@link Properties} object to hold our statistical data.
     * Properties are supposed to store only strings for both keys and values,
     * otherwise it can't be written to disk using Properties.store().
     */
    protected Properties data;

    public SessionStatistic() {
        data = new Properties();
    }

    protected String appendToKey(String key, Object suffix) {
        return key + "." + suffix;
    }

    /**
     * Returns the properties object of this SessionStatistic that holds the
     * statistical data.
     * 
     * @return the wrapped {@link #data} properties object
     */
    public Properties getData() {
        return data;
    }

    /**
     * Clears all gathered statistical data.
     */
    public void clear() {
        data.clear();
    }

    /**
     * Adds the contents of the given SessionStaistic to this SessionStatistic.
     * 
     * @param statistic
     */
    public void addAll(SessionStatistic statistic) {
        data.putAll(statistic.getData());
    }

    @Override
    public String toString() {
        ArrayList<String> lines = new ArrayList<String>();

        for (Entry<Object, Object> e : data.entrySet()) {
            lines.add("  " + e.getKey() + "=" + e.getValue());
        }

        Collections.sort(lines);

        return "Session statistic:\n" + StringUtils.join(lines, '\n');
    }

    /**
     * Writes the session data to a file with the given filename in the given
     * directory path.
     * 
     * @param path
     * @param filename
     * @return an existing statistic file or null if an IOExeption occurred on
     *         writing
     */
    public File toFile(IPath path, String filename) {
        String pathString = path.append(filename).toString();
        File file = new File(pathString);
        FileOutputStream fos = null;
        log.debug("Writing statistic data to " + pathString);

        try {
            fos = new FileOutputStream(file);
            data.store(fos, "Saros session data");
        } catch (IOException e) {
            log.error("Couldn't write session data to file " + path, e);
            return null;
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                log.warn("An IOException occurred while trying to"
                    + " close FileWriter.");
            }
        }

        return file;
    }

    /*
     * ------------------------------------------------------ Access methods to
     * get and set the value for each key
     * ------------------------------------------------------
     */

    public String getSarosVersion() {
        return data.getProperty(SAROS_VERSION);
    }

    public void setSarosVersion(String version) {
        data.setProperty(SAROS_VERSION, version);
    }

    public String getJavaVersion() {
        return data.getProperty(JAVA_VERSION);
    }

    public void setJavaVersion(String version) {
        data.setProperty(JAVA_VERSION, version);
    }

    public String getOSName() {
        return data.getProperty(OS_NAME);
    }

    public void setOSName(String osName) {
        data.setProperty(OS_NAME, osName);
    }

    public String getEclipseVersion() {
        return data.getProperty(ECLIPSE_VERSION);
    }

    public void setEclipseVersion(String version) {
        data.setProperty(ECLIPSE_VERSION, version);
    }

    public boolean getFeedbackDisabled(boolean disabled) {
        return Boolean.parseBoolean(data.getProperty(KEY_FEEDBACK_DISABLED));
    }

    public void setFeedbackDisabled(boolean disabled) {
        data.setProperty(KEY_FEEDBACK_DISABLED, String.valueOf(disabled));
    }

    public int getFeedbackInterval() {
        return Integer.parseInt(data.getProperty(KEY_FEEDBACK_INTERVAL));
    }

    public void setFeedbackInterval(int interval) {
        data.setProperty(KEY_FEEDBACK_INTERVAL, String.valueOf(interval));
    }

    public long getSessionCount() {
        return Long.parseLong(data.getProperty(KEY_SESSION_COUNT));
    }

    public void setSessionCount(long count) {
        data.setProperty(KEY_SESSION_COUNT, String.valueOf(count));
    }

    public double getSessionTime() {
        return Double.parseDouble(data.getProperty(KEY_SESSION_TIME));
    }

    public void setSessionTime(double time) {
        data.setProperty(KEY_SESSION_TIME, String.valueOf(time));
    }

    /**
     * Returns the time the given number of users were together in the session.
     */
    public double getSessionTimeForUsers(int numberOfUsers) {
        return Double.parseDouble(data.getProperty(appendToKey(
            KEY_SESSION_TIME_USERS, numberOfUsers)));
    }

    /**
     * Sets the time the given number of users were together in the session.
     */
    public void setSessionTimeForUsers(int numberOfUsers, double time) {
        data.setProperty(appendToKey(KEY_SESSION_TIME_USERS, numberOfUsers),
            String.valueOf(time));
    }

    /**
     * Returns for the given number of users the percentage of the total session
     * time they were together in the session.
     */
    public int getSessionTimePrecentForUsers(int numberOfUsers) {
        return Integer.parseInt(data.getProperty(appendToKey(
            KEY_SESSION_TIME_PERCENT_USERS, numberOfUsers)));
    }

    /**
     * Sets for the given number of users the percentage of the total session
     * time they were together in the session.
     */
    public void setSessionTimePercentForUsers(int numberOfUsers, int percent) {
        data.setProperty(appendToKey(KEY_SESSION_TIME_PERCENT_USERS,
            numberOfUsers), String.valueOf(percent));
    }

    public String getSessionID() {
        return data.getProperty(KEY_SESSION_ID);
    }

    public void setSessionID(String sessionID) {
        data.setProperty(KEY_SESSION_ID, sessionID);
    }

    /**
     * Returns the total number of users that participated in the session, they
     * didn't had to be present at the same time.
     */
    public int getSessionUsersTotal() {
        return Integer.parseInt(data.getProperty(KEY_SESSION_USERS_TOTAL));
    }

    /**
     * Sets the total number of users that participated in the session, they
     * didn't had to be present at the same time.
     */
    public void setSessionUsersTotal(int numberOfUsers) {
        data
            .setProperty(KEY_SESSION_USERS_TOTAL, String.valueOf(numberOfUsers));
    }

    /**
     * Returns the n-th role of the user.
     */
    public String getRole(int n) {
        return data.getProperty(appendToKey(KEY_ROLE, n));
    }

    /**
     * Sets the n-th role of the user.
     */
    public void setRole(int n, String role) {
        data.setProperty(appendToKey(KEY_ROLE, n), role);
    }

    /**
     * Returns the time the user executed the n-th role.
     */
    public double getRoleTime(int n) {
        return Double.parseDouble(data
            .getProperty(appendToKey(KEY_ROLE_TIME, n)));
    }

    /**
     * Sets the time the user executed the n-th role.
     */
    public void setRoleTime(int n, double time) {
        data.setProperty(appendToKey(KEY_ROLE_TIME, n), String.valueOf(time));
    }

    /**
     * Returns the percentage of the session time the user executed the n-th
     * role.
     */
    public int getRoleTimePercent(int n) {
        return Integer.parseInt(data.getProperty(appendToKey(
            KEY_ROLE_TIME_PERCENT, n)));
    }

    /**
     * Sets the percentage of the session time the user executed the n-th role.
     */
    public void setRoleTimePercent(int number, int percent) {
        data.setProperty(appendToKey(KEY_ROLE_TIME_PERCENT, number), String
            .valueOf(percent));
    }

    /**
     * Returns the total amount of the local user's role changes.
     */
    public int getRoleChanges() {
        return Integer.parseInt(data.getProperty(KEY_ROLE_CHANGES));
    }

    /**
     * Sets the total amount of the local user's role changes.
     */
    public void setRoleChanges(int changes) {
        data.setProperty(KEY_ROLE_CHANGES, String.valueOf(changes));
    }

    /**
     * Returns the number of characters the local user has written.
     */
    public long getTextEditChars() {
        return Long.parseLong(data.getProperty(KEY_TEXTEDIT_CHARS));
    }

    /**
     * Sets the number of characters the local user has written.
     */
    public void setTextEditChars(long chars) {
        data.setProperty(KEY_TEXTEDIT_CHARS, String.valueOf(chars));
    }

    /**
     * Returns the number of text edits the local user performed.
     */
    public long getTextEditsCount() {
        return Long.parseLong(data.getProperty(KEY_TEXTEDIT_COUNT));
    }

    /**
     * Sets the number of text edits the local user performed.
     */
    public void setTextEditsCount(int count) {
        data.setProperty(KEY_TEXTEDIT_COUNT, String.valueOf(count));
    }

    /**
     * Returns the number of characters the user has written simultaneously in
     * the given interval with other users.
     */
    public long getParallelTextEdits(int interval) {
        return Long.parseLong(data.getProperty(appendToKey(
            KEY_PARALLEL_TEXT_EDITS, interval)));
    }

    /**
     * Sets the number of characters the user has written simultaneously in the
     * given interval with other users.
     */
    public void setParallelTextEdits(int interval, long chars) {
        data.setProperty(appendToKey(KEY_PARALLEL_TEXT_EDITS, interval), String
            .valueOf(chars));
    }

    /**
     * Returns the percentage of the simultaneously written characters in the
     * given interval.
     */
    public int getParallelTextEditsPercent(int interval) {
        return Integer.parseInt(data
            .getProperty(KEY_PARALLEL_TEXT_EDITS_PERCENT));
    }

    /**
     * Sets the percentage of the simultaneously written characters in the given
     * interval.
     */
    public void setParallelTextEditsPercent(int interval, int percent) {
        data.setProperty(
            appendToKey(KEY_PARALLEL_TEXT_EDITS_PERCENT, interval), String
                .valueOf(percent));
    }

    public String getUserID() {
        return data.getProperty(KEY_USER_ID);
    }

    public void setUserID(String userID) {
        data.setProperty(KEY_USER_ID, userID);
    }

    public void setTransferStatistic(String transferMode, int transferEvents,
        long totalSize, long totalTransferTime, double throughput) {

        String key = appendToKey(KEY_TRANSFER_STATS, transferMode);

        data.setProperty(appendToKey(key, TRANSFER_STATS_EVENT_SUFFIX), String
            .valueOf(transferEvents));
        data.setProperty(appendToKey(key, TRANSFER_STATS_SIZE_SUFFIX), String
            .valueOf(totalSize));
        data.setProperty(appendToKey(key, TRANSFER_STATS_TIME_SUFFIX), String
            .valueOf(totalTransferTime));
        data.setProperty(appendToKey(key, TRANSFER_STATS_THROUGHPUT_SUFFIX),
            String.valueOf(Math.round(throughput * 10.0) / 10.0));
    }
}