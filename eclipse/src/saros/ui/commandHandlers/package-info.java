/**
 * This package provides instances of {@link org.eclipse.core.commands.AbstractHandler} (from
 * Eclipse Platform Command Framework), called command handlers and which are referenced in
 * plugin.xml (extension point org.eclipse.ui.handlers) in order to be responsible for the execution
 * of a command under defined prerequisites.
 *
 * <dl>
 *   <dt><b>Example:</b>
 *   <dd>A command "copy" is defined by Eclipse with the ID org.eclipse.ui.copy. What you can do now
 *       is to define a command handlers for the copy command that becomes instantiated and executed
 *       (method {@link org.eclipse.core.commands.AbstractHandler#execute(ExecutionEvent)}) if
 *       <ol>
 *         <li>The user clicks on a button invoking the copy command.
 *         <li>The command handler is the ONLY active one for the copy command.
 *         <li>The command handler is enabled.
 *       </ol>
 * </dl>
 *
 * @see <a href="http://wiki.eclipse.org/index.php/Platform_Command_Framework">Platform Command
 *     Framework</a>
 * @see <a
 *     href="http://help.eclipse.org/indigo/topic/org.eclipse.platform.doc.isv/guide/workbench_cmd_handlers.htm">plugin.xml
 *     - org.eclipse.ui.handlers extension point</a>
 */
package saros.ui.commandHandlers;
