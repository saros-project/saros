/**
 *
 *
 * <h1>Actions Overview</h1>
 *
 * This package contains all possible actions in the saros view which a user can initiate.
 *
 * <p>This package comprises of the following subpackages:
 *
 * <ul>
 *   <li>the {@link ChangeColorAction} allows the user to change his personal color which is also
 *       shown to all other session users.
 *   <li>the {@link ChangeXMPPAccountAction} allows the user to switch between several accounts
 *   <li>the {@link ConnectionTestAction} tests the data connection to another contact or session
 *       user
 *   <li>the {@link ConsistencyAction} calls {@link
 *       saros.concurrent.watchdog.ConsistencyWatchdogClient} and if an inconsistency is found, the
 *       Action gives you the possibility to resolve it.
 *   <li>the {@link DeleteContactAction} allows the user to delete other contacts from his contact
 *       list.
 *   <li>the {@link FollowModeAction} allows the user to follow the first person in the session
 *       which has {@link User.Permission#WRITE_ACCESS}. Activated through toolbar-button.
 *   <li>the {@link FollowModeThisPersonAction} allow to follow a selected session user.
 *   <li>the {@link GiveWriteAccessAction} is responsible for giving {@link
 *       User.Permission#WRITE_ACCESS} to a non-host session user.
 *   <li>the {@link JumpToUserWithWriteAccessPositionAction} allows the user to jump to a selected
 *       session user current viewpoint
 *   <li>the {@link LeaveSessionAction} allows the user to leave a session.
 *   <li>the {@link NewContactAction} allows to add a new contact to the contact list.
 *   <li>the {@link RenameContactAction} allows to rename a contact in the contact list.
 *   <li>the {@link RestrictToReadOnlyAccessAction} removes {@link User.Permission#WRITE_ACCESS}
 *       from a participant.
 *   <li>the {@link SendFileAction} allows a user to send a file to a session user.
 *   <li>the {@link SkypeAction} calls the {@link saros.communication.SkypeManager} to call a
 *       contact for Skype.
 *   <li>the {@link VideoSharingAction} allows a screensharing with a session user.
 *   <li>the {@link VoIPAction} establishes a VoIP connection to another session user.
 * </ul>
 */
package saros.ui.actions;
