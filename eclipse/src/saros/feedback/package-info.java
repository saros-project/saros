/**
 *
 *
 * <h1>Feedback Overview</h1>
 *
 * is responsible for the feedback and statistic processes. The main-classes are {@link
 * FeedbackManager} and {@link StatisticManager }
 *
 * <p>This package comprises of the following subpackages:
 *
 * <ul>
 *   <li>the {@link AbstractFeedbackManager} is the parent class for FeedbackManager and
 *       StatisticManager.
 *   <li>the {@link AbstractStatisticCollector} is the parent class for all kind of statistic
 *       collectors to get information and send them to the {@link StatisticManager}.
 *   <li>the {@link DataTransferCollector} collects information of data transfers (e.g. time, size,
 *       mode)
 *   <li>the {@link ErrorLogManager} upload error logs to the server if the user accepted it.
 *   <li>the {@link FeedbackManager} is a listener to wait for the end of a session and then to ask
 *       the user for feedback using the feedback dialog.
 *   <li>the {@link FileSubmitter} has the basic functions to upload files for feedback to our
 *       server.
 *   <li>the {@link FollowModeCollector} counts amount of times the user enters follow mode and how
 *       long he stays in it.
 *   <li>the {@link JumpFeatureUsageCollector} counts amount of times the user jumps to another
 *       participant and distinguishes between just {@link User.Permission#READONLY_ACCESS} or
 *       {@link User.Permission#WRITE_ACCESS}.
 *   <li>the {@link Messages} is used for access to messages.properties
 *   <li>the {@link ParticipantCollector} collects information about user behavior of participants
 *       in a session.
 *   <li>the {@link PermissionChangeCollector} collects the permission changes and the time each
 *       user spends in each state.
 *   <li>the {@link SelectionCollector} collects the count of selections made by remote users with
 *       {@link User.Permission#READONLY_ACCESS} and also the selection was modified later.
 *   <li>the {@link SessionDataCollector} collects information about the session ID, session time
 *       and the amount of total saros sessions.
 *   <li>the {@link SessionStatistic} creates a properties file with all information of the
 *       collectors and saves it to disc.
 *   <li>the {@link StatisticManager} starts the collectors when a session is initiated. At the end
 *       of the session, the Manager starts {@link SessionStatistic } to write a summary file and
 *       let it upload to server.
 *   <li>the {@link StatisticAggregator} brings together several statistic files independent from
 *       their sessions to one big file.
 *   <li>the {@link TextEditCollector} counts the number of characters typed by the user and also
 *       evaluates if copy & paste was used.
 *   <li>the {@link VOIPCollector} collects the amount of usages of VoIP and also the spent time in
 *       a connection.
 *   <li>the {@link messages.properties} is a mapping of strings to variables which can be called by
 *       {@link Messages}
 * </ul>
 */
package saros.feedback;
