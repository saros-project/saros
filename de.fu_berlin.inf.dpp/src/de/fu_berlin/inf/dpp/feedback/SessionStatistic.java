/*
 * DPP - Serious Distributed Pair Programming
 * (c) Lisa Dohrmann, Freie Universität Berlin 2009
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
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;

import de.fu_berlin.inf.dpp.session.User.Permission;

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

    protected static final String SAROS_VERSION = "saros.version";
    protected static final String JAVA_VERSION = "java.version";
    protected static final String OS_NAME = "os.name";
    protected static final String PLATFORM_VERSION = "platform.version";
    // TODO platform name (runtime, e.g Eclipse, Netbeans etc.)
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

    // Keys for shared project information
    protected static final String KEY_COMPLETE_SHARED_PROJECTS = "session.shared.project.complete.count";
    protected static final String KEY_PARTIAL_SHARED_PROJECTS = "session.shared.project.partial.count";
    protected static final String KEY_PARTIAL_SHARED_PROJECTS_FILES = "session.shared.project.partial.files.count";

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
     * Count of jumps to a user with {@link Permission#READONLY_ACCESS} position
     */
    protected static final String KEY_JUMPED_TO_USER_WITH_READONLY_ACCESS = "jumped.observer.count";

    /**
     * Count of jumps to a user with {@link Permission#WRITE_ACCESS} position
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
     * Key for total users with {@link Permission#READONLY_ACCESS} selection
     * count
     */
    protected static final String KEY_TOTAL_USER_WITH_READONLY_ACCESS_SELECTION_COUNT = "observer.selection.count";

    /**
     * Key for witnessed users with {@link Permission#READONLY_ACCESS}
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
     * Adds the contents of the given SessionStatistic to this SessionStatistic.
     * 
     * @param statistic
     */
    public void addAll(SessionStatistic statistic) {
        data.putAll(statistic.data);
    }

    @Override
    public String toString() {
        StringWriter out = new StringWriter(512);

        try {
            data.store(out, "Saros session data");
        } catch (IOException e) {
            // cannot happen
        } finally {
            IOUtils.closeQuietly(out);
        }
        return out.toString();
    }

    /**
     * Writes the session data to a file.
     * 
     * @param file
     *            the file to save the current session statistic into
     */
    public void toFile(File file) throws IOException {

        FileOutputStream fos = null;

        // write the statistic to the file
        try {
            fos = new FileOutputStream(file);
            data.store(fos, "Saros session data");
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    /*------------------------------------------------------*
     * Access methods to set the value for each key *
     *------------------------------------------------------*/

    public void setSarosVersion(String version) {
        data.setProperty(SAROS_VERSION, version);
    }

    public void setJavaVersion(String version) {
        data.setProperty(JAVA_VERSION, version);
    }

    public void setOSName(String osName) {
        data.setProperty(OS_NAME, osName);
    }

    public void setPlatformVersion(String version) {
        data.setProperty(PLATFORM_VERSION, version);
    }

    public void setFeedbackDisabled(boolean disabled) {
        data.setProperty(KEY_FEEDBACK_DISABLED, String.valueOf(disabled));
    }

    public void setFeedbackInterval(int interval) {
        data.setProperty(KEY_FEEDBACK_INTERVAL, String.valueOf(interval));
    }

    public void setSessionCount(long count) {
        data.setProperty(KEY_SESSION_COUNT, String.valueOf(count));
    }

    public void setLocalSessionDuration(double time) {
        data.setProperty(KEY_SESSION_LOCAL_DURATION, String.valueOf(time));
    }

    /**
     * Sets the time the given number of users were together in the session.
     */
    public void setSessionTimeForUsers(int numberOfUsers, double time) {
        data.setProperty(appendToKey(KEY_SESSION_TIME_USERS, numberOfUsers),
            String.valueOf(time));
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

    public void setSessionID(String sessionID) {
        data.setProperty(KEY_SESSION_ID, sessionID);
    }

    public String getSessionID() {
        return data.getProperty(KEY_SESSION_ID);
    }

    /**
     * Sets the total number of users that participated in the session, they
     * didn't had to be present at the same time.
     */
    public void setSessionUsersTotal(int numberOfUsers) {
        data.setProperty(KEY_SESSION_USERS_TOTAL, String.valueOf(numberOfUsers));
    }

    /**
     * Sets the n-th permission of the user.
     */
    public void setPermission(int n, String permission) {
        data.setProperty(appendToKey(KEY_PERMISSION, n), permission);
    }

    /**
     * Sets the time the user executed the n-th permission.
     */
    public void setPermissionDuration(int n, double time) {
        data.setProperty(appendToKey(KEY_PERMISSION, n, KEY_DURATION),
            String.valueOf(time));
    }

    /**
     * Sets the total amount of the local user's permission changes.
     */
    public void setPermissionChanges(int changes) {
        data.setProperty(KEY_PERMISSION_CHANGES, String.valueOf(changes));
    }

    public void setTotalPermissionDurationReadOnlyAccess(double time) {
        data.setProperty(appendToKey(KEY_PERMISSION_READONLY, KEY_DURATION),
            String.valueOf(time));
    }

    public void setTotalPermissionPercentReadOnlyAccess(int percent) {
        data.setProperty(appendToKey(KEY_PERMISSION_READONLY, KEY_PERCENT),
            String.valueOf(percent));
    }

    public void setTotalPermissionDurationWriteAccess(double time) {
        data.setProperty(appendToKey(KEY_PERMISSION_WRITE, KEY_DURATION),
            String.valueOf(time));
    }

    public void setTotalPermissionPercentWriteAccess(int percent) {
        data.setProperty(appendToKey(KEY_PERMISSION_WRITE, KEY_PERCENT),
            String.valueOf(percent));
    }

    /**
     * Sets the number of characters the local user has written.
     */
    public void setTextEditChars(long chars) {
        data.setProperty(KEY_TEXTEDIT_CHARS, String.valueOf(chars));
    }

    /**
     * Sets the number of text edits the local user performed.
     */
    public void setTextEditsCount(int count) {
        data.setProperty(KEY_TEXTEDIT_COUNT, String.valueOf(count));
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
     * Sets the percentage of the simultaneously written characters in the given
     * interval.
     */
    public void setParallelTextEditsPercent(int interval, int percent) {
        data.setProperty(
            appendToKey(KEY_PARALLEL_TEXT_EDITS, interval, KEY_PERCENT),
            String.valueOf(percent));
    }

    public void setParallelTextEditsCount(int interval, int count) {
        data.setProperty(
            appendToKey(KEY_PARALLEL_TEXT_EDITS, interval, KEY_COUNT),
            String.valueOf(count));
    }

    public void setNonParallelTextEdits(long chars) {
        data.setProperty(appendToKey(KEY_NON_PARALLEL_TEXT_EDITS, KEY_CHARS),
            String.valueOf(chars));
    }

    public void setNonParallelTextEditsPercent(int percent) {
        data.setProperty(appendToKey(KEY_NON_PARALLEL_TEXT_EDITS, KEY_PERCENT),
            String.valueOf(percent));
    }

    public void setUserID(String userID) {
        data.setProperty(KEY_USER_ID, userID);
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

    public void setLocalSessionStartTime(Date localSessionStart) {
        data.setProperty(KEY_SESSION_LOCAL_START,
            toISO8601UTCTimeFormat(localSessionStart));
    }

    public void setLocalSessionEndTime(Date localSessionEnd) {
        data.setProperty(KEY_SESSION_LOCAL_START,
            toISO8601UTCTimeFormat(localSessionEnd));
    }

    /**
     * Sets the number of jumps to the position of a user with
     * {@link Permission#READONLY_ACCESS}
     */
    public void setJumpedToUserWithReadOnlyAccessCount(
        int jumpedToUserWithReadOnlyAccess) {
        data.setProperty(appendToKey(KEY_JUMPED_TO_USER_WITH_READONLY_ACCESS),
            String.valueOf(jumpedToUserWithReadOnlyAccess));
    }

    /**
     * Sets the number of jumps to the position of a user with
     * {@link Permission#WRITE_ACCESS}
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
     * Sets the count of selections made by users with
     * {@link Permission#READONLY_ACCESS}
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
     * Sets the count of user with {@link Permission#READONLY_ACCESS} selections
     * that were witnessed by another user
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
     * Sets the number of user with {@link Permission#READONLY_ACCESS}
     * selections where an edit occurred.
     * 
     * @param userWithWriteAccessSelectionCount
     */
    public void setGestureCount(int userWithWriteAccessSelectionCount) {
        data.setProperty(appendToKey(KEY_GESTURE_COUNT),
            String.valueOf(userWithWriteAccessSelectionCount));
    }

    /**
     * Sets the characters edited by each user.
     * 
     * @param userNumber
     * @param charCount
     */
    public void setRemoteUserCharCount(int userNumber, Integer charCount) {
        data.setProperty(appendToKey(KEY_REMOTE_USER, userNumber, KEY_CHARS),
            String.valueOf(charCount));
    }

    /**
     * Sets the number of remote paste events for each user.
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

    public void setSharedProjectStatistic(int completeSharedProjectCount,
        int partialSharedProjectCount, int partialSharedFileCount) {
        data.setProperty(KEY_COMPLETE_SHARED_PROJECTS,
            String.valueOf(completeSharedProjectCount));

        data.setProperty(KEY_PARTIAL_SHARED_PROJECTS,
            String.valueOf(partialSharedProjectCount));

        data.setProperty(KEY_PARTIAL_SHARED_PROJECTS_FILES,
            String.valueOf(partialSharedFileCount));
    }

    // need to be Java 6 compatible !
    private static String toISO8601UTCTimeFormat(Date date) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }
}