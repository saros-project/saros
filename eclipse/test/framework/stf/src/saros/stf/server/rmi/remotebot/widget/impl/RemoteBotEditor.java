package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.ListResult;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import saros.editor.annotations.SelectionAnnotation;
import saros.editor.internal.EditorAPI;
import saros.editor.text.LineRange;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotEditor;
import saros.stf.server.util.WidgetUtil;

public class RemoteBotEditor extends StfRemoteObject implements IRemoteBotEditor {

  private static final RemoteBotEditor INSTANCE = new RemoteBotEditor();

  private SWTBotEclipseEditor widget;

  public static RemoteBotEditor getInstance() {
    return INSTANCE;
  }

  public void setWidget(SWTBotEclipseEditor editor) {
    this.widget = editor;
  }

  @Override
  public void show() throws RemoteException {
    widget.show();
  }

  @Override
  public void setFocus() throws RemoteException {
    widget.setFocus();
  }

  @Override
  public void closeWithSave() throws RemoteException {
    widget.save();
    widget.close();
  }

  @Override
  public void save() throws RemoteException {
    widget.save();
  }

  @Override
  public void closeWithoutSave() throws RemoteException {
    widget.close();
    if (RemoteWorkbenchBot.getInstance().isShellOpen(SHELL_SAVE_RESOURCE)
        && RemoteWorkbenchBot.getInstance().shell(SHELL_SAVE_RESOURCE).isActive())
      RemoteWorkbenchBot.getInstance().shell(SHELL_SAVE_RESOURCE).confirm(NO);
  }

  @Override
  public void setTextFromFile(String contentPath) throws RemoteException {
    String contents = FileUtils.read(getSaros().getBundle().getEntry(contentPath));
    widget.setText(contents);
  }

  @Override
  public void setText(String text) throws RemoteException {
    widget.setText(text);
  }

  @Override
  public void typeText(String text) throws RemoteException {
    widget.setFocus();
    widget.typeText(text);
  }

  @Override
  public void navigateTo(int line, int column) throws RemoteException {
    widget.setFocus();
    widget.navigateTo(line, column);
  }

  @Override
  public void selectCurrentLine() throws RemoteException {
    widget.selectCurrentLine();
    // It's is necessary to sleep a litte time so that the following
    // operation like quickfix will be successfully performed.
    SWTUtils.sleep(500);
  }

  @Override
  public void selectLine(int line) throws RemoteException {
    widget.selectLine(line);
    // It's is necessary to sleep a litte time so that the following
    // operation like quickfix will be successfully performed.
    SWTUtils.sleep(1000);
  }

  @Override
  public void selectRange(int line, int column, int length) throws RemoteException {
    widget.selectRange(line, column, length);
    // It's is necessary to sleep a litte time so that the following
    // operation like quickfix will be successfully performed.
    SWTUtils.sleep(800);
  }

  @Override
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

  @Override
  public void pressShortCut(int modificationKeys, char c) throws RemoteException {
    widget.pressShortcut(modificationKeys, c);
  }

  @Override
  public void pressShortCutDelete() throws RemoteException {
    pressShortcut(IKeyLookup.DELETE_NAME);
  }

  @Override
  public void pressShortCutEnter() throws RemoteException {
    pressShortcut(IKeyLookup.LF_NAME);
  }

  @Override
  public void pressShortCutSave() throws RemoteException {
    if (WidgetUtil.getOperatingSystem() == WidgetUtil.OperatingSystem.MAC)
      widget.pressShortcut(SWT.COMMAND, 's');
    else widget.pressShortcut(SWT.CTRL, 's');
  }

  @Override
  public void pressShortRunAsJavaApplication() throws RemoteException {
    if (WidgetUtil.getOperatingSystem() == WidgetUtil.OperatingSystem.MAC)
      widget.pressShortcut(SWT.ALT | SWT.COMMAND, 'x');
    else widget.pressShortcut(SWT.ALT | SWT.SHIFT, 'x');

    SWTUtils.sleep(1000);

    widget.pressShortcut(SWT.NONE, 'j');
  }

  @Override
  public void pressShortCutNextAnnotation() throws RemoteException {
    if (WidgetUtil.getOperatingSystem() == WidgetUtil.OperatingSystem.MAC)
      widget.pressShortcut(SWT.COMMAND, '.');
    else widget.pressShortcut(SWT.CTRL, '.');

    SWTUtils.sleep(100);
  }

  @Override
  public void pressShortCutQuickAssignToLocalVariable() throws RemoteException {
    if (WidgetUtil.getOperatingSystem() == WidgetUtil.OperatingSystem.MAC)
      widget.pressShortcut(SWT.COMMAND, '2');
    else widget.pressShortcut(SWT.CTRL, '2');

    SWTUtils.sleep(1000);

    widget.pressShortcut(SWT.NONE, 'l');
  }

  @Override
  public void autoCompleteProposal(String insertText, String proposalText) throws RemoteException {
    widget.autoCompleteProposal(insertText, proposalText);
  }

  @Override
  public void quickfix(String quickFixName) throws RemoteException {
    widget.quickfix(quickFixName);
  }

  @Override
  public void quickfix(int index) throws RemoteException {
    widget.quickfix(index);
  }

  /*
   *
   * states
   */

  @Override
  public int getLineCount() throws RemoteException {
    return widget.getLineCount();
  }

  @Override
  public List<String> getLines() throws RemoteException {
    return widget.getLines();
  }

  @Override
  public String getText() throws RemoteException {
    return widget.getText();
  }

  @Override
  public String getTextOnCurrentLine() throws RemoteException {
    return widget.getTextOnCurrentLine();
  }

  @Override
  public String getTextOnLine(int line) throws RemoteException {
    return widget.getTextOnLine(line);
  }

  @Override
  public int getCursorLine() throws RemoteException {
    return widget.cursorPosition().line;
  }

  @Override
  public int getCursorColumn() throws RemoteException {
    return widget.cursorPosition().column;
  }

  @Override
  public RGB getLineBackground(int line) throws RemoteException {
    return widget.getLineBackground(line);
  }

  @Override
  public boolean isDirty() throws RemoteException {
    return widget.isDirty();
  }

  @Override
  public List<Integer> getViewport() throws RemoteException {
    @SuppressWarnings("deprecation")
    final IEditorPart editorPart = widget.getEditorReference().getEditor(false);

    if (editorPart == null)
      throw new WidgetNotFoundException(
          "could not find editor part for editor " + widget.getTitle());

    List<Integer> viewPort =
        UIThreadRunnable.syncExec(
            new ListResult<Integer>() {
              @Override
              public List<Integer> run() {
                List<Integer> viewPort = new ArrayList<Integer>(2);

                ITextViewer viewer = EditorAPI.getViewer(editorPart);
                LineRange r = EditorAPI.getViewport(viewer);

                viewPort.add(r.getStartLine());
                viewPort.add(r.getNumberOfLines());
                return viewPort;
              }
            });

    return viewPort;
  }

  @Override
  public String getSelection() throws RemoteException {

    return widget.getSelection();
  }

  @Override
  public String getSelectionByAnnotation() throws RemoteException {

    @SuppressWarnings("deprecation")
    final IEditorPart editorPart = widget.getEditorReference().getEditor(false);

    if (editorPart == null)
      throw new WidgetNotFoundException(
          "could not find editor part for editor " + widget.getTitle());

    if (!(editorPart instanceof ITextEditor))
      throw new IllegalArgumentException("editor part is not an instance of ITextEditor");

    List<Integer> selectionRange =
        UIThreadRunnable.syncExec(
            new ListResult<Integer>() {
              @Override
              public List<Integer> run() {

                List<Integer> selectionRange = new ArrayList<Integer>();
                selectionRange.add(0);
                selectionRange.add(0);

                ITextEditor textEditor = (ITextEditor) editorPart;

                IDocumentProvider docProvider = textEditor.getDocumentProvider();

                if (docProvider == null) return selectionRange;

                IEditorInput input = textEditor.getEditorInput();
                IAnnotationModel model = docProvider.getAnnotationModel(input);

                if (model == null) return selectionRange;

                @SuppressWarnings("unchecked")
                Iterator<Annotation> annotationIterator = model.getAnnotationIterator();

                while (annotationIterator.hasNext()) {
                  Annotation annotation = annotationIterator.next();

                  if (!(annotation instanceof SelectionAnnotation)) continue;

                  Position p = model.getPosition(annotation);

                  selectionRange.clear();
                  selectionRange.add(p.getOffset());
                  selectionRange.add(p.getLength());
                  return selectionRange;
                }

                return selectionRange;
              }
            });

    String text = widget.getText();
    return text.substring(selectionRange.get(0), selectionRange.get(0) + selectionRange.get(1));
  }

  @Override
  public List<String> getAutoCompleteProposals(String insertText) throws RemoteException {
    return widget.getAutoCompleteProposals(insertText);
  }

  @Override
  public boolean isActive() throws RemoteException {
    return widget.isActive();
  }

  @Override
  public void waitUntilIsActive() throws RemoteException {
    RemoteWorkbenchBot.getInstance()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return isActive();
              }

              @Override
              public String getFailureMessage() {
                return "editor '" + widget.getTitle() + "' is not open";
              }
            });
  }

  @Override
  public void waitUntilIsTextSame(final String otherText) throws RemoteException {
    RemoteWorkbenchBot.getInstance()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return getText().equals(otherText);
              }

              @Override
              public String getFailureMessage() {
                return "content of editor '"
                    + widget.getTitle()
                    + "' does not match: "
                    + widget.getText()
                    + " != "
                    + otherText;
              }
            });
  }
}
