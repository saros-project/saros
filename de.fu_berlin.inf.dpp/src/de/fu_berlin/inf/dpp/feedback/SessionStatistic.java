/*
 * DPP - Serious Distributed Pair Programming
 * (c) Lisa Dohrmann, Freie Universitaet Berlin 2009
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package de.fu_berlin.inf.dpp.feedback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * The SessionStatistic class wraps a Properties object in which the gathered
 * statistical data can be stored as simple key/value pairs. This data can then
 * be saved to disk as a file.<br>
 * <br>
 * 
 * TODO Add a field user.saros.team to the SessionStatistic which is set to true
 * if:<br>
 * - the associated button on the FeedbackPreferencePage is true <br>
 * - the Saros version number does not end with rXXXX <br>
 * - the version number is just [X]X.[X]X.[X]X (with [X] being optional)<br>
 * The value should be determined by the SessionDataCollector and written to the
 * statistic on the end of a session.
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
    protected static final String KEY_USER_IS_HOST = "user.is.host";

    protected static final String KEY_FEEDBACK_DISABLED = "feedback.disabled";
    protected static final String KEY_FEEDBACK_INTERVAL = "feedback.survey.interval";

    protected static final String KEY_SESSION_COUNT = "session.count";
    protected static final String KEY_SESSION_TIME_USERS = "session.time.users";
    protected static final String KEY_SESSION_ID = "session.id";
    protected static final String KEY_SESSION_USERS_TOTAL = "session.users.total";

    /**
     * The duration of the session for the local user in minutes with two digits
     * precision.
     */
    protected static final String KEY_SESSION_LOCAL_DURATION = "session.time";

    /**
     * ISO DateTime (UTC) of the time the session started for the local user.
     * 
     * This might not be equal to the start of the whole session, because the
     * local user might not be host.
     * 
     * @since 9.9.11
     */
    protected static final String KEY_SESSION_LOCAL_START = "session.local.start";

    /**
     * ISO DateTime (UTC) of the time the session ended for the local user.
     * 
     * This might not be equal to the end of the whole session, because the
     * local user might not be host.
     * 
     * @since 9.9.11
     */
    protected static final String KEY_SESSION_LOCAL_END = "session.local.end";

    // Keys for permission changes
    protected static final String KEY_PERMISSION = "role";
    protected static final String KEY_PERMISSION_READONLY = "role.observer";
    protected static final String KEY_PERMISSION_WRITE = "role.driver";
    protected static final String KEY_PERMISSION_CHANGES = "role.changes";

    protected static final String KEY_DURATION = "duration";
    protected static final String KEY_PERCENT = "percent";
    protected static final String KEY_CHARS = "chars";
    protected static final String KEY_COUNT = "count";
    protected static final String KEY_PASTES = "pastes";

    // Keys for textedits and pastes
    protected static final String KEY_TEXTEDIT_CHARS = "textedits.chars";
    protected static final String KEY_TEXTEDIT_COUNT = "textedits.count";
    protected static final String KEY_TEXTEDIT_PASTES = "textedits.pastes";
    protected static final String KEY_PARALLEL_TEXT_EDITS = "textedits.parallel.interval";
    protected static final String KEY_NON_PARALLEL_TEXT_EDITS = "textedits.nonparallel";
    protected static final String KEY_REMOTE_USER = "textedits.remote.user";

    // Keys for DataTransferCollector
    protected static final String KEY_TRANSFER_STATS = "data_transfer";
    protected static final String TRANSFER_STATS_EVENT_SUFFIX = "number_of_events";
    // Total size in KB
    protected static final String TRANSFER_STATS_SIZE_SUFFIX = "total_size_kb";
    // Total size for transfers in milliseconds
    protected static final String TRANSFER_STATS_TIME_SUFFIX = "total_time_ms";
    // Convenience value of total_size / total_time in KB/s
    protected static final String TRANSFER_STATS_THROUGHPUT_SUFFIX = "average_throughput_kbs";

    /**
     * A pseudonym set by the user in the preferences to identify himself. This
     * can be used to track the randomly generated
     * {@link SessionStatistic#KEY_USER_ID} to a "real" person if the user
     * chooses to do so.
     * 
     * @since 9.9.11
     */
    protected static final String KEY_PSEUDONYM = "user.pseudonym";

    // Keys for jumpFeatureUsageCollector
    /**
     * Count of jumps to a user with {@link User.Permission#READONLY_ACCESS}
     * position
     */
    protected static final String KEY_JUMPED_TO_USER_WITH_READONLY_ACCESS = "jumped.observer.count";

    /**
     * Count of jumps to a user with {@link User.Permission#WRITE_ACCESS}
     * position
     */
    protected static final String KEY_JUMPED_TO_USER_WITH_WRITE_ACCESS = "jumped.driver.count";

    /** Total count of jumps */
    protected static final String KEY_TOTAL_JUMP_COUNT = "jumped.count";

    // Keys for followModeCollector
    /** Total time spent in follow mode */
    protected static final String KEY_FOLLOWMODE_TOTAL = "followmode.time.total";

    /**
     * Percentage of time in respect to total session length spent in follow
     * mode
     */
    protected static final String KEY_FOLLOWMODE_PERCENT = "followmode.time.percent";

    /** Total count of follow mode toggles */
    protected static final String KEY_FOLLOWMODE_TOGGLES = "followmode.toggles";

    // Further Keys for SessionDataCollector

    /** Preference setting of auto follow mode */
    protected static final String KEY_AUTO_FOLLOW_MODE_ENABLED = "auto.followmode.enabled";

    // Keys for selections and gestures
    /**
     * Key for total users with {@link User.Permission#READONLY_ACCESS}
     * selection count
     */
    protected static final String KEY_TOTAL_USER_WITH_READONLY_ACCESS_SELECTION_COUNT = "observer.selection.count";

    /**
     * Key for witnessed users with {@link User.Permission#READONLY_ACCESS}
     * selections
     */
    protected static final String KEY_WITNESSED_USER_WITH_READONLY_ACCESS_SELECTION_COUNT = "observer.selection.count.witnessed";

    /** Key for gesture count */
    protected static final String KEY_GESTURE_COUNT = "gesture.count";

    // Keys for VoIP Collector
    /** Key for VoIP total time */
    protected static final String KEY_VOIP_TOTAL = "voip.time.total";

    /** Key for VoIP percentage */
    protected static final String KEY_VOIP_PERCENT = "voip.time.percent";

    /** Key for VoIP sessions established */
    protected static final String KEY_VOIP_COUNT = "voip.session.count";

    /**
     * This is the {@link Properties} object to hold our statistical data.
     * Properties are supposed to store only strings for both keys and values,
     * otherwise it can't be written to disk using Properties.store().
     */
    protected Properties data;

    public SessionStatistic() {
        data = new Properties();
    }

    protected String appendToKey(String key, Object... suffixes) {
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        for (Object suffix : suffixes) {
            sb.append(".").append(suffix);
        }
        return sb.toString();
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
     * directory path with a subfolder of the current date (format:
     * <code>statistic_yyyy-MM-dd</code>).
     * 
     * @param path
     * @param filename
     * @return an existing statistic file or null if an IOExeption occurred on
     *         writing
     */
    public File toFile(IPath path, String filename) {
        // create a subfolder with the current date, if nonexistent
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dirName = "statistic_" + dateFormat.format(new Date());
        IPath filePath = path.append(dirName).append(filename);

        if (!Utils.mkdirs(filePath.toOSString())) {
            log.error("Could not create the necessary directories to save"
                + " the statistic as a file. Therefore the statistic could"
                + " not be saved.");
            return null;
        }

        File file = filePath.toFile();
        FileOutputStream fos = null;
        log.info("Writing statistic data to " + file.getPath());

        // write the statistic to the file
        try {
            fos = new FileOutputStream(file);
            data.store(fos, "Saros session data");
        } catch (IOException e) {
            log.error("Couldn't write session data to file " + path, e);
            return null;
        } finally {
            IOUtils.closeQuietly(fos);
        }

        return file;
    }

    /*------------------------------------------------------*
     * Access methods to get and set the value for each key *
     *------------------------------------------------------*/

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

    public double getLocalSessionDuration() {
        return Double.parseDouble(data.getProperty(KEY_SESSION_LOCAL_DURATION));
    }

    public void setLocalSessionDuration(double time) {
        data.setProperty(KEY_SESSION_LOCAL_DURATION, String.valueOf(time));
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
            KEY_SESSION_TIME_USERS, numberOfUsers, KEY_PERCENT)));
    }

    /**
     * Sets for the given number of users the percentage of the total session
     * time they were together in the session.
     */
    public void setSessionTimePercentForUsers(int numberOfUsers, int percent) {
        data.setProperty(
            appendToKey(KEY_SESSION_TIME_USERS, numberOfUsers, KEY_PERCENT),
            String.valueOf(percent));
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
        data.setProperty(KEY_SESSION_USERS_TOTAL, String.valueOf(numberOfUsers));
    }

    /**
     * Returns the n-th permission of the user.
     */
    public String getPermission(int n) {
        return data.getProperty(appendToKey(KEY_PERMISSION, n));
    }

    /**
     * Sets the n-th permission of the user.
     */
    public void setPermission(int n, String permission) {
        data.setProperty(appendToKey(KEY_PERMISSION, n), permission);
    }

    /**
     * Returns the time the user executed the n-th permission.
     */
    public double getPermissionDuration(int n) {
        return Double.parseDouble(data.getProperty(appendToKey(KEY_PERMISSION,
            n, KEY_DURATION)));
    }

    /**
     * Sets the time the user executed the n-th permission.
     */
    public void setPermissionDuration(int n, double time) {
        data.setProperty(appendToKey(KEY_PERMISSION, n, KEY_DURATION),
            String.valueOf(time));
    }

    /**
     * Returns the total amount of the local user's permission changes.
     */
    public int getPermissionChanges() {
        return Integer.parseInt(data.getProperty(KEY_PERMISSION_CHANGES));
    }

    /**
     * Sets the total amount of the local user's permission changes.
     */
    public void setPermissionChanges(int changes) {
        data.setProperty(KEY_PERMISSION_CHANGES, String.valueOf(changes));
    }

    public double getTotalPermissionDurationReadOnlyAccess() {
        return Double.parseDouble(data.getProperty(appendToKey(
            KEY_PERMISSION_READONLY, KEY_DURATION)));
    }

    public void setTotalPermissionDurationReadOnlyAccess(double time) {
        data.setProperty(appendToKey(KEY_PERMISSION_READONLY, KEY_DURATION),
            String.valueOf(time));
    }

    public int getTotalPermissionPercentReadOnlyAcccess() {
        return Integer.parseInt(data.getProperty(appendToKey(
            KEY_PERMISSION_READONLY, KEY_PERCENT)));
    }

    public void setTotalPermissionPercentReadOnlyAccess(int percent) {
        data.setProperty(appendToKey(KEY_PERMISSION_READONLY, KEY_PERCENT),
            String.valueOf(percent));
    }

    public double getTotalPermissionDurationWriteAccess() {
        return Double.parseDouble(data.getProperty(appendToKey(
            KEY_PERMISSION_WRITE, KEY_DURATION)));
    }

    public void setTotalPermissionDurationWriteAccess(double time) {
        data.setProperty(appendToKey(KEY_PERMISSION_WRITE, KEY_DURATION),
            String.valueOf(time));
    }

    public int getTotalPermissionPercentWriteAccess() {
        return Integer.parseInt(data.getProperty(appendToKey(
            KEY_PERMISSION_WRITE, KEY_PERCENT)));
    }

    public void setTotalPermissionPercentWriteAccess(int percent) {
        data.setProperty(appendToKey(KEY_PERMISSION_WRITE, KEY_PERCENT),
            String.valueOf(percent));
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
            KEY_PARALLEL_TEXT_EDITS, interval, KEY_CHARS)));
    }

    /**
     * Sets the number of characters the user has written simultaneously in the
     * given interval with other users.
     */
    public void setParallelTextEdits(int interval, long chars) {
        data.setProperty(
            appendToKey(KEY_PARALLEL_TEXT_EDITS, interval, KEY_CHARS),
            String.valueOf(chars));
    }

    /**
     * Returns the percentage of the simultaneously written characters in the
     * given interval.
     */
    public int getParallelTextEditsPercent(int interval) {
        return Integer.parseInt(data.getProperty(appendToKey(
            KEY_PARALLEL_TEXT_EDITS, interval, KEY_PERCENT)));
    }

    /**
     * Sets the percentage of the simultaneously written characters in the given
     * interval.
     */
    public void setParallelTextEditsPercent(int interval, int percent) {
        data.setProperty(
            appendToKey(KEY_PARALLEL_TEXT_EDITS, interval, KEY_PERCENT),
            String.valueOf(percent));
    }

    public int getParallelTextEditsCount(int interval) {
        return Integer.parseInt(data.getProperty(appendToKey(
            KEY_PARALLEL_TEXT_EDITS, interval, KEY_COUNT)));
    }

    public void setParallelTextEditsCount(int interval, int count) {
        data.setProperty(
            appendToKey(KEY_PARALLEL_TEXT_EDITS, interval, KEY_COUNT),
            String.valueOf(count));
    }

    public long getNonParallelTextEdits() {
        return Long.parseLong(data.getProperty(appendToKey(
            KEY_NON_PARALLEL_TEXT_EDITS, KEY_CHARS)));
    }

    public void setNonParallelTextEdits(long chars) {
        data.setProperty(appendToKey(KEY_NON_PARALLEL_TEXT_EDITS, KEY_CHARS),
            String.valueOf(chars));
    }

    public int getNonParallelTextEditsPercent() {
        return Integer.parseInt(data.getProperty(appendToKey(
            KEY_NON_PARALLEL_TEXT_EDITS, KEY_PERCENT)));
    }

    public void setNonParallelTextEditsPercent(int percent) {
        data.setProperty(appendToKey(KEY_NON_PARALLEL_TEXT_EDITS, KEY_PERCENT),
            String.valueOf(percent));
    }

    public String getUserID() {
        return data.getProperty(KEY_USER_ID);
    }

    public void setUserID(String userID) {
        data.setProperty(KEY_USER_ID, userID);
    }

    public boolean getIsHost() {
        return Boolean.parseBoolean(data.getProperty(KEY_USER_IS_HOST));
    }

    public void setIsHost(boolean isHost) {
        data.setProperty(KEY_USER_IS_HOST, String.valueOf(isHost));
    }

    public void setTransferStatistic(String transferMode, int transferEvents,
        long totalSize, long totalTransferTime, double throughput) {

        String key = appendToKey(KEY_TRANSFER_STATS, transferMode);

        data.setProperty(appendToKey(key, TRANSFER_STATS_EVENT_SUFFIX),
            String.valueOf(transferEvents));
        data.setProperty(appendToKey(key, TRANSFER_STATS_SIZE_SUFFIX),
            String.valueOf(totalSize));
        data.setProperty(appendToKey(key, TRANSFER_STATS_TIME_SUFFIX),
            String.valueOf(totalTransferTime));
        data.setProperty(appendToKey(key, TRANSFER_STATS_THROUGHPUT_SUFFIX),
            String.valueOf(Math.round(throughput * 10.0) / 10.0));
    }

    public void setPseudonym(String pseudonym) {
        data.setProperty(KEY_PSEUDONYM, pseudonym);
    }

    public void setLocalSessionStartTime(DateTime localSessionStart) {
        data.setProperty(KEY_SESSION_LOCAL_START,
            localSessionStart.toDateTime(DateTimeZone.UTC).toString());
    }

    public void setLocalSessionEndTime(DateTime localSessionEnd) {
        data.setProperty(KEY_SESSION_LOCAL_START,
            localSessionEnd.toDateTime(DateTimeZone.UTC).toString());
    }

    /**
     * Sets the number of jumps to the position of a user with
     * {@link User.Permission#READONLY_ACCESS}
     */
    public void setJumpedToUserWithReadOnlyAccessCount(
        int jumpedToUserWithReadOnlyAccess) {
        data.setProperty(appendToKey(KEY_JUMPED_TO_USER_WITH_READONLY_ACCESS),
            String.valueOf(jumpedToUserWithReadOnlyAccess));
    }

    /**
     * Sets the number of jumps to the position of a user with
     * {@link User.Permission#WRITE_ACCESS}
     */
    public void setJumpedToUserWithWriteAccessCount(
        int jumpedToUserWithWriteAccess) {
        data.setProperty(appendToKey(KEY_JUMPED_TO_USER_WITH_WRITE_ACCESS),
            String.valueOf(jumpedToUserWithWriteAccess));
    }

    /**
     * Sets the total count for jumps performed
     */
    public void setJumpedToCount(int jumpedToTotal) {
        data.setProperty(appendToKey(KEY_TOTAL_JUMP_COUNT),
            String.valueOf(jumpedToTotal));
    }

    /**
     * Returns the number of jumps to the position of a user with
     * {@link User.Permission#READONLY_ACCESS}
     */
    public int getJumpedToUserWithReadOnlyAccessCount() {
        return Integer.parseInt(data
            .getProperty(KEY_JUMPED_TO_USER_WITH_READONLY_ACCESS));
    }

    /**
     * Returns the number of jumps to the position of a user with
     * {@link User.Permission#WRITE_ACCESS}
     */
    public int getJumpedToUserWithWriteAccessCount() {
        return Integer.parseInt(data
            .getProperty(KEY_JUMPED_TO_USER_WITH_WRITE_ACCESS));
    }

    /**
     * Returns total number of jumps performed
     */
    public int getJumpedToCount() {
        return Integer.parseInt(data.getProperty(KEY_TOTAL_JUMP_COUNT));
    }

    /**
     * Returns the number of follow mode toggles
     */
    public int getFollowModeTogglesCount() {
        return Integer.parseInt(data.getProperty(KEY_FOLLOWMODE_TOGGLES));
    }

    /**
     * Returns the percentage of time spent in follow mode
     */
    public int getFollowModeTimePercentage() {
        return Integer.parseInt(data.getProperty(KEY_FOLLOWMODE_PERCENT));
    }

    /**
     * Returns the total time spent in follow mode
     */
    public double getFollowModeTimeTotal() {
        return Double.parseDouble(data.getProperty(KEY_FOLLOWMODE_TOTAL));
    }

    /**
     * Sets the total number of follow mode toggles
     */
    public void setFollowModeTogglesCount(int count) {
        data.setProperty(KEY_FOLLOWMODE_TOGGLES, String.valueOf(count));
    }

    /**
     * Sets the percentage of time spent in follow mode in respect to total
     * session length
     */
    public void setFollowModeTimePercentage(int percentage) {
        data.setProperty(KEY_FOLLOWMODE_PERCENT, String.valueOf(percentage));
    }

    /**
     * Sets the total time of being in follow mode
     */
    public void setFollowModeTimeTotal(double time) {
        data.setProperty(KEY_FOLLOWMODE_TOTAL, String.valueOf(time));
    }

    /**
     * Sets the state of the auto follow mode from configuration settings
     */
    public void setAutoFollowModeEnabled(boolean autoFollowEnabled) {
        data.setProperty(KEY_AUTO_FOLLOW_MODE_ENABLED,
            String.valueOf(autoFollowEnabled));
    }

    /**
     * Returns the state as <code>boolean</code> of the auto follow mode feature
     * from the configuration settings
     */
    public boolean getAutoFollowModeEnabled() {
        return Boolean.parseBoolean(data
            .getProperty(KEY_AUTO_FOLLOW_MODE_ENABLED));
    }

    /**
     * Sets the count of selections made by users with
     * {@link User.Permission#READONLY_ACCESS}
     * 
     * @param userWithReadOnlyAccessSelectionCount
     */
    public void setTotalOberserverSelectionCount(
        int userWithReadOnlyAccessSelectionCount) {
        data.setProperty(
            appendToKey(KEY_TOTAL_USER_WITH_READONLY_ACCESS_SELECTION_COUNT),
            String.valueOf(userWithReadOnlyAccessSelectionCount));
    }

    /**
     * Sets the count of user with {@link User.Permission#READONLY_ACCESS}
     * selections that were witnessed by another user
     * 
     * @param numberOfWitnessedUserWithReadOnlyAccessSelections
     */
    public void setWitnessedUserWithReadOnlyAccessSelections(
        int numberOfWitnessedUserWithReadOnlyAccessSelections) {
        data.setProperty(
            appendToKey(KEY_WITNESSED_USER_WITH_READONLY_ACCESS_SELECTION_COUNT),
            String.valueOf(numberOfWitnessedUserWithReadOnlyAccessSelections));
    }

    /**
     * Sets the number of user with {@link User.Permission#READONLY_ACCESS}
     * selections where an edit occured.
     * 
     * @param userWithWriteAccessSelectionCount
     */
    public void setGestureCount(int userWithWriteAccessSelectionCount) {
        data.setProperty(appendToKey(KEY_GESTURE_COUNT),
            String.valueOf(userWithWriteAccessSelectionCount));
    }

    /**
     * Returns the number of user with {@link User.Permission#READONLY_ACCESS}
     * selections made
     */
    public int getTotalOberserverSelectionCount() {
        return Integer.parseInt(data
            .getProperty(KEY_TOTAL_USER_WITH_READONLY_ACCESS_SELECTION_COUNT));
    }

    /**
     * Returns the number of witnessed user with {@link User.Permission#READONLY_ACCESS} selections
     */
    public int getWitnessedUserWithReadOnlyAccessSelections() {
        return Integer
            .parseInt(data
                .getProperty(KEY_WITNESSED_USER_WITH_READONLY_ACCESS_SELECTION_COUNT));
    }

    /**
     * Returns the gesture count
     */
    public int getGestureCount() {
        return Integer.parseInt(data.getProperty(KEY_GESTURE_COUNT));
    }

    /**
     * Sets the characters edited by each buddy
     * 
     * @param userNumber
     * @param charCount
     */
    public void setRemoteUserCharCount(int userNumber, Integer charCount) {
        data.setProperty(appendToKey(KEY_REMOTE_USER, userNumber, KEY_CHARS),
            String.valueOf(charCount));
    }

    /**
     * Sets the number of remote paste events for each buddy
     * 
     * @param userNumber
     * @param pasteCount
     */
    public void setRemoteUserPastes(int userNumber, int pasteCount) {
        data.setProperty(appendToKey(KEY_REMOTE_USER, userNumber, KEY_PASTES),
            String.valueOf(pasteCount));
    }

    /**
     * Sets the number of local paste events
     * 
     * @param pasteCount
     */
    public void setLocalUserPastes(int pasteCount) {
        data.setProperty(appendToKey(KEY_TEXTEDIT_PASTES),
            String.valueOf(pasteCount));
    }

    /**
     * Sets the number of chars that were added through a remote paste
     * 
     * @param userNumber
     * @param pasteChars
     */
    public void setRemoteUserPasteChars(int userNumber, Integer pasteChars) {
        data.setProperty(
            appendToKey(KEY_REMOTE_USER, userNumber, KEY_PASTES, KEY_CHARS),
            String.valueOf(pasteChars));
    }

    /**
     * Sets the number of chars that were added through a paste
     * 
     * @param pasteChars
     */
    public void setLocalUserPasteChars(Integer pasteChars) {
        data.setProperty(appendToKey(KEY_TEXTEDIT_PASTES, KEY_CHARS),
            String.valueOf(pasteChars));
    }

    /**
     * Returns the remote chars for user with number n
     */
    public int getRemoteUserCharCount(int n) {
        return Integer.parseInt(data.getProperty(appendToKey(KEY_CHARS, n,
            KEY_REMOTE_USER)));
    }

    /**
     * Returns the number of pastes conducted by user n
     */
    public int getRemoteUserPastes(int n) {
        return Integer.parseInt(data.getProperty(appendToKey(KEY_PASTES, n,
            KEY_REMOTE_USER)));
    }

    /**
     * Returns the total pastes made by the local user
     */
    public int getLocalUserPastes() {
        return Integer.parseInt(data.getProperty(KEY_TEXTEDIT_PASTES));
    }

    /**
     * Returns the number of chars contained within the pastes of user n
     */
    public int getRemoteUserPasteChars(int n) {
        return Integer.parseInt(data.getProperty(appendToKey(KEY_PASTES,
            KEY_CHARS, n, KEY_REMOTE_USER)));
    }

    public int getLocalUserPasteChars() {
        return Integer.parseInt(data.getProperty(appendToKey(
            KEY_TEXTEDIT_PASTES, KEY_CHARS)));
    }

    /**
     * Sets the time spent in VoIP sessions in minutes
     * 
     * @param timeInMinutes
     */
    public void setVoIPTime(double timeInMinutes) {
        data.setProperty(KEY_VOIP_TOTAL, String.valueOf(timeInMinutes));
    }

    /**
     * Sets the percentage spent in VoIP sessions in respect to the session
     * length
     * 
     * @param percentage
     */
    public void setVoIPPercentage(int percentage) {
        data.setProperty(appendToKey(KEY_VOIP_PERCENT),
            String.valueOf(percentage));
    }

    /**
     * Sets the number of established VoIP sessions
     * 
     * @param numberVoIPSessions
     */
    public void setVoIPSessionCount(int numberVoIPSessions) {
        data.setProperty(appendToKey(KEY_VOIP_COUNT),
            String.valueOf(numberVoIPSessions));
    }

    /**
     * Returns the time spent in VoIP sessions
     */
    public double getVoIPTime() {
        return Double.parseDouble(data.getProperty(KEY_VOIP_TOTAL));
    }

    /**
     * Returns the percentage of time of a session spent in VoIP sessions
     */
    public int getVoIPPercentage() {
        return Integer.parseInt(data.getProperty(KEY_VOIP_PERCENT));
    }

    /**
     * Returns the total number of VoIP sessions in a session
     */
    public int getVoIPSessionCount() {
        return Integer.parseInt(data.getProperty(KEY_VOIP_COUNT));
    }
}