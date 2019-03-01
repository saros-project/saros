/**
 *
 *
 * <h1>Undo Module Overview</h1>
 *
 * The undo module takes care of undoing the last operation made by the user within a Saros session.
 * In order to not destroy the work made by another user in Saros it is important to only undo the
 * last own operation made by the user. Because Eclipse only provides a linear undo mechanism this
 * module is used to keep track of all operations made by the user for undo/redo purposes.
 *
 * <ul>
 *   <li>the {@link OperationHistory} of the user's own operations is stored
 *   <li>the {@link UndoManager} only works during a running Saros session
 * </ul>
 */
package saros.concurrent.undo;
