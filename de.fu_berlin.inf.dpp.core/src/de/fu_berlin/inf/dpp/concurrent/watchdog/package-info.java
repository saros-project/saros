/**
 *
 *
 * <h1>Consistency Watchdog Module Overview</h1>
 *
 * The consistency watchdog is one of the most important modules in Saros, because it is responsible
 * to recover from situations such as programming faults or misuse by the user which have caused an
 * inconsistent state between the participants in a Saros session.
 *
 * <p>Def: Consistency == The state of having files and folders equivalent to those at the hosts
 * side.
 *
 * <p>This definition has three important parts:
 *
 * <ol>
 *   <li>Consistency is about files and folders, not about Editors, Viewports or Permissions
 *   <li>The host is the reference point for consistency. The host himself thus can never be in an
 *       inconsistent state.
 *   <li>The word "equivalent" needs some caution. Because Saros allows multiple participants to
 *       write concurrently, consistency is naturally a relative concept. The best way of thinking
 *       about it is, that if everybody stops changing files and folders, the project should
 *       eventually become consistent.
 * </ol>
 *
 * How does it work in Saros?
 *
 * <p>Important components are:
 *
 * <ul>
 *   <li>ConsistencyWatchdogServer --- Generates checksums every 10 seconds for all files which are
 *       opened anywhere (host and clients)
 *   <li>ConsistencyWatchdogClient --- Receives incoming checksums and checks whether the local
 *       files match the checksums
 *   <li>ConsistencyAction --- Action for starting a recovery if the ConsistencyWatchdogClient found
 *       inconsistent files
 *   <li>ConsistencyWatchdogHandler --- Network component which can sent/receive requests for
 *       recoveries from clients to the host.
 * </ul>
 */
package de.fu_berlin.inf.dpp.concurrent.watchdog;
