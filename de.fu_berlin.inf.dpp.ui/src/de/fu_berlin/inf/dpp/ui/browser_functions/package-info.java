/**
 * Browser functions are Java methods that are presented as JavaScript functions in the browser
 * displaying the GUI elements. They need to be annotated with the {@link
 * de.fu_berlin.inf.dpp.ui.browser_functions.BrowserFunction} annotation.
 *
 * <p>Browser functions may use primitive data types or complex ones (see model package) as argument
 * and return types. They may call core facade methods.
 *
 * <p>Browser function of the "query" type, i.e. that don't change the state of Saros should use
 * return values (e.g. {@link de.fu_berlin.inf.dpp.ui.browser_functions.GetValidJID}. Function of
 * the "command" type, i.e. which do change the state of Saros should rely on an event mechanism to
 * trigger changes in the GUI instead of doing this directly (e.g. {@link
 * de.fu_berlin.inf.dpp.ui.browser_functions.SetActiveAccount}).
 */
package de.fu_berlin.inf.dpp.ui.browser_functions;
