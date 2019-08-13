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
 *   <li>the {@link saros.ui.actions.ChangeColorAction} allows the user to change his personal color
 *       which is also shown to all other session users.
 *   <li>the {@link saros.ui.actions.ChangeXMPPAccountAction} allows the user to switch between
 *       several accounts
 *   <li>the {@link saros.ui.actions.ConsistencyAction} calls {@link
 *       saros.concurrent.watchdog.ConsistencyWatchdogClient} and if an inconsistency is found, the
 *       Action gives you the possibility to resolve it.
 *   <li>the {@link saros.ui.actions.DeleteContactAction} allows the user to delete other contacts
 *       from his contact list.
 *   <li>the {@link saros.ui.actions.FollowModeAction} allows the user to follow the first person in
 *       the session which has {@link saros.session.User.Permission#WRITE_ACCESS}. Activated through
 *       toolbar-button.
 *   <li>the {@link saros.ui.actions.FollowThisPersonAction} allow to follow a selected session
 *       user.
 *   <li>the {@link saros.ui.actions.ChangeWriteAccessAction} is responsible for grant or revoke
 *       {@link saros.session.User.Permission#WRITE_ACCESS} to a non-host session user.
 *   <li>the {@link saros.ui.actions.JumpToUserWithWriteAccessPositionAction} allows the user to
 *       jump to a selected session user current viewpoint
 *   <li>the {@link saros.ui.actions.LeaveSessionAction} allows the user to leave a session.
 *   <li>the {@link saros.ui.actions.NewContactAction} allows to add a new contact to the contact
 *       list.
 *   <li>the {@link saros.ui.actions.RenameContactAction} allows to rename a contact in the contact
 *       list.
 *   <li>the {@link saros.ui.actions.SendFileAction} allows a user to send a file to a session user.
 *   <li>the {@link saros.ui.actions.SkypeAction} calls the {@link saros.communication.SkypeManager}
 *       to call a contact for Skype.
 * </ul>
 */
package saros.ui.actions;
