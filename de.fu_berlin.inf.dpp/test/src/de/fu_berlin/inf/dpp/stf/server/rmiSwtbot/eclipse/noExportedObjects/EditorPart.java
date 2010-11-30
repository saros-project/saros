package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects;


/**
 * This class contains basic API to find widgets based on the GUI component
 * "editor" in SWTBot and to perform the operations on a editor, which is only
 * used by rmi server side and not exported.
 * 
 * @author lchen
 */
//public class EditorPart extends EclipseComponent {

// /**
// * Sets the text into the given editor and saves the change.
// *
// * @param text
// * the text to set.
// * @param fileName
// * the filename on the editor tab
// */
// public void setTextInEditorWithSave(String text, String fileName) {
// SWTBotEclipseEditor e = getTextEditor(fileName);
// // e.setFocus();
// // e.pressShortcut(Keystrokes.LF);
// e.setText(text);
// e.save();
// }

// /**
// * Sets the text into the given editor without saving the change.
// *
// * @param text
// * the text to set.
// * @param fileName
// * the filename on the editor tab
// */
// public void setTextinEditorWithoutSave(String text, String fileName) {
// SWTBotEclipseEditor e = getTextEditor(fileName);
// e.setText(text);
// }

// /**
// * TODO: This function doesn't work exactly. It should be happen that the
// * text isn't typed in the right editor, When your saros-instances are
// fresh
// * started.
// *
// * @param text
// * the text to set.
// * @param fileName
// * the filename on the editor tab
// */
// public void typeTextInEditor(String text, final String fileName) {
// SWTBotEclipseEditor e = getTextEditor(fileName);
// e.navigateTo(3, 0);
// e.autoCompleteProposal("main", "main - main method");
// e.autoCompleteProposal("sys", "sysout - print to standard out");
// e.typeText("System.currentTimeMillis()");
// // SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
// // SWTBotEclipseEditor e = getTextEditor(fileName);
// // e.navigateTo(3, 0);
// // e.pressShortcut(SWT.CTRL, '.');
// // e.quickfix("Add unimplemented methods");
// // e.navigateTo(7, 0);
// //
// // e.navigateTo(3, 0);
// // e.autoCompleteProposal("main", "main - main method");
// // e.autoCompleteProposal("sys", "sysout - print to standard out");
// // e.typeText("System.currentTimeMillis()");
//
// // e.typeText("thread.start();\n");
// // e.typeText("thread.join();");
// // SWTBotPreferences.KEYBOARD_LAYOUT = "DE_DE";
// // e.quickfix("Add throws declaration");
// // e.pressShortcut(SWT.NONE, (char) 27);
// // e.pressShortcut(SWT.NmainONE, '\n');
// //
// // e.pressShortcut(SWT.CTRL, 's');
// //
// // e.pressShortcut(SWT.ALT | SWT.SHIFT, 'x');
// // e.pressShortcut(SWT.NONE, 'j');
// }

// /**
// *
// * @param fileName
// * the filename on the editor tab
// * @return an extended version of the editor bot which provides methods
// for
// * text editors.
// */
// public SWTBotEclipseEditor getTextEditor(String fileName) {
// SWTBotEditor editor = bot.editorByTitle(fileName);
// return editor.toTextEditor();
// }

// /**
// *
// * @param fileName
// * the filename on the editor tab
// * @return the editor with the specified title
// * @see SWTWorkbenchBot#editorByTitle(String)
// */
// public SWTBotEditor getEditor(String fileName) {
// return bot.editorByTitle(fileName);
// }

// /**
// *
// * @param line
// * the line number to select, 0 based.
// * @param fileName
// * the filename on the editor tab
// */
// public void selectLineInEditor(int line, String fileName) {
// getTextEditor(fileName).selectLine(line);
// }

// /**
// * Activates the tabItem.
// *
// * @param fileName
// * the filename on the editor tab
// */
// public void setFocusOnEditor(String fileName) {
// try {
// bot.editorByTitle(fileName).setFocus();
// // bot.cTabItem(fileName).activate();
// } catch (TimeoutException e) {
// log.warn("The tab" + fileName + " does not activate '", e);
// }
//
// }

// /**
// *
// * @param fileName
// * the filename on the editor tab
// * @return <tt>true</tt>, if there is a active editor and it's name is
// same
// * as the given fileName.
// */
// public boolean isEditorActive(String fileName) {
// try {
// return bot.activeEditor().getTitle().equals(fileName);
// } catch (WidgetNotFoundException e) {
// return false;
// }
// }

// }
