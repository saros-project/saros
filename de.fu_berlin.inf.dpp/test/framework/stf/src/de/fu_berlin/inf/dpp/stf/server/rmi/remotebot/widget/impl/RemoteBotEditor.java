package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.ListResult;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import de.fu_berlin.inf.dpp.editor.annotations.SelectionAnnotation;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotEditor;
import de.fu_berlin.inf.dpp.stf.server.util.Util;

public class RemoteBotEditor extends StfRemoteObject implements
    IRemoteBotEditor {

    private static final Logger log = Logger.getLogger(RemoteBotEditor.class);
    private static final RemoteBotEditor INSTANCE = new RemoteBotEditor();

    private SWTBotEclipseEditor widget;

    public static RemoteBotEditor getInstance() {
        return INSTANCE;
    }

    public void setWidget(SWTBotEclipseEditor editor) {
        this.widget = editor;
    }

    public void show() throws RemoteException {
        widget.show();
    }

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    public void closeWithSave() throws RemoteException {
        widget.save();
        widget.close();
    }

    public void save() throws RemoteException {
        widget.save();
    }

    public void closeWithoutSave() throws RemoteException {
        widget.close();
        if (RemoteWorkbenchBot.getInstance().isShellOpen(SHELL_SAVE_RESOURCE)
            && RemoteWorkbenchBot.getInstance().shell(SHELL_SAVE_RESOURCE)
                .isActive())
            RemoteWorkbenchBot.getInstance().shell(SHELL_SAVE_RESOURCE)
                .confirm(NO);
    }

    public void setTextFromFile(String contentPath) throws RemoteException {
        String contents = FileUtils.read(getSaros().getBundle().getEntry(
            contentPath));
        widget.setText(contents);
    }

    public void setText(String text) throws RemoteException {
        widget.setText(text);
    }

    public void typeText(String text) throws RemoteException {
        widget.setFocus();
        widget.typeText(text);
    }

    public void navigateTo(int line, int column) throws RemoteException {
        widget.setFocus();
        widget.navigateTo(line, column);
    }

    public void selectCurrentLine() throws RemoteException {
        widget.selectCurrentLine();
        // It's is necessary to sleep a litte time so that the following
        // operation like quickfix will be successfully performed.
        RemoteWorkbenchBot.getInstance().sleep(500);
    }

    public void selectLine(int line) throws RemoteException {
        widget.selectLine(line);
        // It's is necessary to sleep a litte time so that the following
        // operation like quickfix will be successfully performed.
        RemoteWorkbenchBot.getInstance().sleep(1000);

    }

    public void selectRange(int line, int column, int length)
        throws RemoteException {
        widget.selectRange(line, column, length);
        // It's is necessary to sleep a litte time so that the following
        // operation like quickfix will be successfully performed.
        RemoteWorkbenchBot.getInstance().sleep(800);
    }

    public void pressShortcut(String... keys) throws RemoteException {
        widget.setFocus();
        for (String key : keys) {
            try {
                widget.pressShortcut(KeyStroke.getInstance(key));
            } catch (ParseException e) {
                throw new RemoteException("could not parse \"" + key + "\"", e);
            }
        }
    }

    public void pressShortCut(int modificationKeys, char c)
        throws RemoteException {
        widget.pressShortcut(modificationKeys, c);
    }

    public void pressShortCutDelete() throws RemoteException {
        pressShortcut(IKeyLookup.DELETE_NAME);
    }

    public void pressShortCutEnter() throws RemoteException {
        pressShortcut(IKeyLookup.LF_NAME);
    }

    public void pressShortCutSave() throws RemoteException {
        if (Util.getOperatingSystem() == Util.OperatingSystem.MAC)
            widget.pressShortcut(SWT.COMMAND, 's');
        else
            widget.pressShortcut(SWT.CTRL, 's');
    }

    public void pressShortRunAsJavaApplication() throws RemoteException {
        if (Util.getOperatingSystem() == Util.OperatingSystem.MAC)
            widget.pressShortcut(SWT.ALT | SWT.COMMAND, 'x');
        else
            widget.pressShortcut(SWT.ALT | SWT.SHIFT, 'x');
        RemoteWorkbenchBot.getInstance().sleep(1000);
        widget.pressShortcut(SWT.NONE, 'j');
    }

    public void pressShortCutNextAnnotation() throws RemoteException {
        if (Util.getOperatingSystem() == Util.OperatingSystem.MAC)
            widget.pressShortcut(SWT.COMMAND, '.');
        else
            widget.pressShortcut(SWT.CTRL, '.');

        RemoteWorkbenchBot.getInstance().sleep(20);
    }

    public void pressShortCutQuickAssignToLocalVariable()
        throws RemoteException {
        if (Util.getOperatingSystem() == Util.OperatingSystem.MAC)
            widget.pressShortcut(SWT.COMMAND, '2');
        else
            widget.pressShortcut(SWT.CTRL, '2');
        RemoteWorkbenchBot.getInstance().sleep(1000);
        widget.pressShortcut(SWT.NONE, 'l');

    }

    public void autoCompleteProposal(String insertText, String proposalText)
        throws RemoteException {
        widget.autoCompleteProposal(insertText, proposalText);
    }

    public void quickfix(String quickFixName) throws RemoteException {
        widget.quickfix(quickFixName);
    }

    public void quickfix(int index) throws RemoteException {
        widget.quickfix(index);
    }

    /*
     * 
     * states
     */

    public int getLineCount() throws RemoteException {
        return widget.getLineCount();
    }

    public List<String> getLines() throws RemoteException {
        return widget.getLines();
    }

    public String getText() throws RemoteException {
        return widget.getText();
    }

    public String getTextOnCurrentLine() throws RemoteException {
        return widget.getTextOnCurrentLine();
    }

    public String getTextOnLine(int line) throws RemoteException {
        return widget.getTextOnLine(line);
    }

    public int getCursorLine() throws RemoteException {
        return widget.cursorPosition().line;
    }

    public int getCursorColumn() throws RemoteException {
        return widget.cursorPosition().column;
    }

    public RGB getLineBackground(int line) throws RemoteException {
        return widget.getLineBackground(line);
    }

    public boolean isDirty() throws RemoteException {
        return widget.isDirty();
    }

    public List<Integer> getViewport() throws RemoteException {
        @SuppressWarnings("deprecation")
        final IEditorPart editorPart = widget.getEditorReference().getEditor(
            false);

        if (editorPart == null)
            throw new WidgetNotFoundException(
                "could not find editor part for editor " + widget.getTitle());

        List<Integer> viewPort = UIThreadRunnable
            .syncExec(new ListResult<Integer>() {
                public List<Integer> run() {
                    List<Integer> viewPort = new ArrayList<Integer>(2);
                    ILineRange r = getEditorAPI().getViewport(editorPart);
                    viewPort.add(r.getStartLine());
                    viewPort.add(r.getNumberOfLines());
                    return viewPort;
                }
            });

        return viewPort;
    }

    public String getSelection() throws RemoteException {

        return widget.getSelection();
    }

    public String getSelectionByAnnotation() throws RemoteException {

        @SuppressWarnings("deprecation")
        final IEditorPart editorPart = widget.getEditorReference().getEditor(
            false);

        if (editorPart == null)
            throw new WidgetNotFoundException(
                "could not find editor part for editor " + widget.getTitle());

        if (!(editorPart instanceof ITextEditor))
            throw new IllegalArgumentException(
                "editor part is not an instance of ITextEditor");

        List<Integer> selectionRange = UIThreadRunnable
            .syncExec(new ListResult<Integer>() {
                public List<Integer> run() {

                    List<Integer> selectionRange = new ArrayList<Integer>();
                    selectionRange.add(0);
                    selectionRange.add(0);

                    ITextEditor textEditor = (ITextEditor) editorPart;

                    IDocumentProvider docProvider = textEditor
                        .getDocumentProvider();

                    if (docProvider == null)
                        return selectionRange;

                    IEditorInput input = textEditor.getEditorInput();
                    IAnnotationModel model = docProvider
                        .getAnnotationModel(input);

                    if (model == null)
                        return selectionRange;

                    @SuppressWarnings("unchecked")
                    Iterator<Annotation> annotationIterator = model
                        .getAnnotationIterator();

                    while (annotationIterator.hasNext()) {
                        Annotation annotation = annotationIterator.next();

                        if (!(annotation instanceof SelectionAnnotation))
                            continue;

                        Position p = model.getPosition(annotation);

                        selectionRange.clear();
                        selectionRange.add(p.getOffset());
                        selectionRange.add(p.getLength());

                        return selectionRange;
                    }

                    return selectionRange;
                }
            });

        return widget.getText().substring(selectionRange.get(0),
            selectionRange.get(1));

    }

    public List<String> getAutoCompleteProposals(String insertText)
        throws RemoteException {
        return widget.getAutoCompleteProposals(insertText);
    }

    public boolean isActive() throws RemoteException {
        return widget.isActive();
    }

    public void waitUntilIsActive() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isActive();
            }

            public String getFailureMessage() {
                return "editor '" + widget.getTitle() + "' is not open";
            }
        });
    }

    public void waitUntilIsTextSame(final String otherText)
        throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return getText().equals(otherText);
            }

            public String getFailureMessage() {
                return "content of editor '" + widget.getTitle()
                    + "' does not match: " + widget.getText() + " != "
                    + otherText;
            }
        });

    }

}
