/**
 * <h1>Actions Overview</h1>
 * 
 * This package contains all possible actions in the saros view  which a user can initiate.
 * 
 * This package comprises of the following subpackages:
 * 
 * <ul>
 * 
 * <li>the {@link ChangeColorAction} allows the user to change his personal color which is also shown to all other participants.</li>
 * 
 * <li>the {@link ChangeXMPPAccountAction} allows the user to switch between several accounts</li>
 * 
 * <li>the {@link ConnectionTestAction} tests the data connection to another buddy or participant</li>
 * 
 * <li>the {@link ConsistencyAction} calls {@link de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient} and if an inconsistency is found, the Action gives you the possibility to resolve it.</li>
 * 
 * <li>the {@link DeleteContactAction} allows the user to delete other buddies from his buddy-list.</li>
 * 
 * <li>the {@link FollowModeAction} allows the user to follow the first person in the session which has {@link User.Permission#WRITE_ACCESS}. Activated through toolbar-button.</li>
 * 
 * <li>the {@link FollowModeThisPersonAction} allow to follow a selected session participant.</li>
 * 
 * <li>the {@link GiveWriteAccessAction} is responsible for giving {@link User.Permission#WRITE_ACCESS} to a client.</li>
 * 
 * <li>the {@link IMBeepAction} is responsible for beep-notification for new chat messages.</li>
 * 
 * <li>the {@link JumpToUserWithWriteAccessPositionAction} allows the user to jump to a selected participants current viewpoint</li>
 * 
 * <li>the {@link LeaveSessionAction} allows the user to leave a session.</li>
 * 
 * <li>the {@link NewContactAction} allows to add a new Buddy to the Buddy list.</li>
 * 
 * <li>the {@link RenameContactAction} allows to rename a Buddy in the Buddy list.</li>
 * 
 * <li>the {@link RestrictToReadOnlyAccessAction} removes {@link User.Permission#WRITE_ACCESS} from a participant.</li>
 * 
 * <li>the {@link SendFileAction} allows a user to send a file to a buddy. </li>
 * 
 * <li>the {@link SkypeAction} calls the {@link de.fu_berlin.inf.dpp.communication.SkypeManager} to call a buddy for Skype.</li>
 * 
 * <li>the {@link StoppedAction}  blocks all input from other participants. The reason is that you don't want another participant who edits the code while compiling.</li>
 * 
 * <li>the {@link VideoSharingAction} allows a screensharing with a participant.</li>
 * 
 * <li>the {@link VoIPAction} establishes a VoIP connection to another participant.</li>
 *  
 * </ul>
 */

package de.fu_berlin.inf.dpp.ui.actions;
