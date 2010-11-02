package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.swtbot.saros.finder.SarosSWTBot;

public class EditorObject {
    private static final transient Logger log = Logger
        .getLogger(EditorObject.class);
    private RmiSWTWorkbenchBot rmiBot;
    private WaitUntilObject wUntil;
    private static SarosSWTBot bot = new SarosSWTBot();

    public EditorObject(RmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
        this.wUntil = rmiBot.wUntilObject;
    }

    public void setTextinEditorWithSave(String contents, String fileName) {
        SWTBotEclipseEditor e = getTextEditor(fileName);
        e.setFocus();
        e.typeText("hallo wie geht es dir ");
        bot.sleep(2000);
        e.pressShortcut(Keystrokes.LF);
        bot.sleep(2000);
        e.setText(contents);
        e.save();
    }

    public void setTextinEditorWithoutSave(String contents, String fileName) {
        SWTBotEclipseEditor e = getTextEditor(fileName);
        e.setText(contents);
    }

    public void typeTextInEditor(String contents, final String fileName) {

        // SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        SWTBotEclipseEditor e = getTextEditor(fileName);
        e.navigateTo(3, 0);

        e.autoCompleteProposal("main", "main - main method");
        e.autoCompleteProposal("sys", "sysout - print to standard out");

        e.typeText("System.currentTimeMillis()");

        // SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        // SWTBotEclipseEditor e = getTextEditor(fileName);
        // e.navigateTo(3, 0);
        // e.pressShortcut(SWT.CTRL, '.');
        // e.quickfix("Add unimplemented methods");
        // e.navigateTo(7, 0);
        //
        // e.navigateTo(3, 0);
        // e.autoCompleteProposal("main", "main - main method");
        // e.autoCompleteProposal("sys", "sysout - print to standard out");
        // e.typeText("System.currentTimeMillis()");

        // e.typeText("thread.start();\n");
        // e.typeText("thread.join();");
        // SWTBotPreferences.KEYBOARD_LAYOUT = "DE_DE";
        // e.quickfix("Add throws declaration");
        // e.pressShortcut(SWT.NONE, (char) 27);
        // e.pressShortcut(SWT.NmainONE, '\n');
        //
        // e.pressShortcut(SWT.CTRL, 's');
        //
        // e.pressShortcut(SWT.ALT | SWT.SHIFT, 'x');
        // e.pressShortcut(SWT.NONE, 'j');
    }

    public SWTBotEclipseEditor getTextEditor(String fileName) {
        SWTBotEditor editor;
        editor = bot.editorByTitle(fileName);
        SWTBotEclipseEditor e = editor.toTextEditor();
        return e;
    }

    public List<String> getEditorTitles() {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotEditor editor : bot.editors())
            list.add(editor.getTitle());
        return list;
    }

    public void selectLineInEditor(int line, String fileName) {
        getTextEditor(fileName).selectLine(line);
    }

    public void closeEditorWithText(String text) {
        if (isEditorOpen(text)) {
            bot.editorByTitle(text).close();
        }
    }

    public boolean isEditorOpen(String name) {
        return getEditorTitles().contains(name);

        // try {
        // return delegate.editorByTitle(name) != null;
        // } catch (WidgetNotFoundException e) {
        // log.warn("Editor '" + name + "' doesn't exist!");
        // return false;
        // }
    }

    public void activateEditor(String name) {
        try {
            bot.cTabItem(name).activate();
        } catch (WidgetNotFoundException e) {
            log.warn("tableItem not found '", e);
        }
        // waitUntilEditorActive(name);
    }

    public boolean isEditorActive(String name) {
        return bot.activeEditor().getTitle().equals(name);
    }

}
