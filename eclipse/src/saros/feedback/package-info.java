/**
 *
 *
 * <h1>Feedback Overview</h1>
 *
 * is responsible for the feedback and statistic processes. The main-classes are {@link
 * saros.feedback.FeedbackManager} and {@link saros.feedback.StatisticManager }
 *
 * <p>This package comprises of the following subpackages:
 *
 * <ul>
 *   <li>the {@link saros.feedback.AbstractFeedbackManager} is the parent class for FeedbackManager
 *       and StatisticManager.
 *   <li>the {@link saros.feedback.AbstractStatisticCollector} is the parent class for all kind of
 *       statistic collectors to get information and send them to the {@link
 *       saros.feedback.StatisticManager}.
 *   <li>the {@link saros.feedback.DataTransferCollector} collects information of data transfers
 *       (e.g. time, size, mode)
 *   <li>the {@link saros.feedback.ErrorLogManager} upload error logs to the server if the user
 *       accepted it.
 *   <li>the {@link saros.feedback.FeedbackManager} is a listener to wait for the end of a session
 *       and then to ask the user for feedback using the feedback dialog.
 *   <li>the {@link saros.feedback.FileSubmitter} has the basic functions to upload files for
 *       feedback to our server.
 *   <li>the {@link saros.feedback.FollowModeCollector} counts amount of times the user enters
 *       follow mode and how long he stays in it.
 *   <li>the {@link saros.feedback.JumpFeatureUsageCollector} counts amount of times the user jumps
 *       to another participant and distinguishes between just {@link
 *       saros.session.User.Permission#READONLY_ACCESS} or {@link
 *       saros.session.User.Permission#WRITE_ACCESS}.
 *   <li>the {@link saros.feedback.Messages} is used for access to messages.properties
 *   <li>the {@link saros.feedback.ParticipantCollector} collects information about user behavior of
 *       participants in a session.
 *   <li>the {@link saros.feedback.PermissionChangeCollector} collects the permission changes and
 *       the time each user spends in each state.
 *   <li>the {@link saros.feedback.SelectionCollector} collects the count of selections made by
 *       remote users with {@link saros.session.User.Permission#READONLY_ACCESS} and also the
 *       selection was modified later.
 *   <li>the {@link saros.feedback.SessionDataCollector} collects information about the session ID,
 *       session time and the amount of total Saros sessions.
 *   <li>the {@link saros.feedback.SessionStatistic} creates a properties file with all information
 *       of the collectors and saves it to disc.
 *   <li>the {@link saros.feedback.StatisticManager} starts the collectors when a session is
 *       initiated. At the end of the session, the Manager starts {@link
 *       saros.feedback.SessionStatistic } to write a summary file and let it upload to server.
 *   <li>the {@link saros.feedback.TextEditCollector} counts the number of characters typed by the
 *       user and also evaluates if copy & paste was used.
 * </ul>
 */
package saros.feedback;
