package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import de.fu_berlin.inf.dpp.intellij.editor.colorstorage.ColorModel;
import de.fu_berlin.inf.dpp.intellij.filesystem.Filesystem;

import java.awt.Color;

/**
 * IntellJ editor API. An Editor is a window for editing source files.
 * <p/>
 * Performs IntelliJ editor related actions in the UI thread.
 */

public class EditorAPI {

    private Application application;
    private CommandProcessor commandProcessor;

    private Project project;

    /**
     * Creates an EditorAPI with the current Project and initializes Fields.
     */
    public EditorAPI(Project project) {
        this.project = project;
        this.application = ApplicationManager.getApplication();
        this.commandProcessor = CommandProcessor.getInstance();
    }

    /**
     * Sets the given Editor to the specified line range in the UI thread.
     *
     * @param editor
     * @param lineStart
     * @param lineEnd
     */
    public void setViewPort(final Editor editor, final int lineStart,
        final int lineEnd) {

        Runnable action = new Runnable() {
            @Override
            public void run() {

                VisualPosition posCenter = new VisualPosition(
                    (lineStart + lineEnd) / 2, 0);
                editor.getCaretModel().moveToVisualPosition(posCenter);
                editor.getScrollingModel()
                    .scrollToCaret(ScrollType.MAKE_VISIBLE);

            }
        };

        application.invokeAndWait(action, ModalityState.defaultModalityState());
    }

    /**
     * Inserts text at the given position inside the UI thread.
     *
     * @param doc
     * @param position
     * @param text
     */
    public void insertText(final Document doc, final int position,
        final String text) {

        Runnable action = new Runnable() {
            @Override
            public void run() {
                commandProcessor.executeCommand(project, new Runnable() {

                    @Override
                    public void run() {
                        doc.insertString(position, text);
                    }
                }, "insertText()", commandProcessor.getCurrentCommandGroupId());
            }
        };

        Filesystem.runWriteAction(action, ModalityState.defaultModalityState());
    }

    /**
     * Overwrites the content of the document with text inside the UI thread.
     *
     * @param doc
     * @param text
     */
    public void setText(final Document doc, final String text) {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                commandProcessor.executeCommand(project, new Runnable() {

                    @Override
                    public void run() {
                        doc.setText(text);
                    }
                }, "setText()", commandProcessor.getCurrentCommandGroupId());
            }
        };

        Filesystem.runWriteAction(action, ModalityState.defaultModalityState());
    }

    /**
     * Adds text mark on the editor for the specified line range.
     *
     * @param editor
     * @param start
     * @param end
     * @param color
     * @return
     */
    public RangeHighlighter textMarkAdd(final Editor editor, final int start,
        final int end, Color color) {
        if (color == null || editor == null) {
            return null;
        }

        TextAttributes textAttr = new TextAttributes();
        textAttr.setBackgroundColor(color);

        RangeHighlighter highlighter = editor.getMarkupModel()
            .addRangeHighlighter(start, end, HighlighterLayer.LAST, textAttr,
                HighlighterTargetArea.EXACT_RANGE);
        highlighter.setGreedyToLeft(false);
        highlighter.setGreedyToRight(false);

        return highlighter;
    }

    /**
     * Removes text mark of the <code>highlighter</code> from editor.
     * When <code>highlighter</code> is <code>null</code>, it removes all marks.
     *
     * @param editor
     * @param highlighter when <code>highlighter</code> is <code>null</code>,
     *                    it removes all marks.
     */
    public void textMarkRemove(final Editor editor,
        RangeHighlighter highlighter) {
        if (editor == null) {
            return;
        }

        //TODO: Check if this is necessary at all.
        if (highlighter != null) {
            editor.getMarkupModel().removeHighlighter(highlighter);
        } else {
            for (RangeHighlighter myHighlighter : editor.getMarkupModel()
                .getAllHighlighters()) {
                editor.getMarkupModel().removeHighlighter(myHighlighter);
            }
        }
    }

    /**
     * Deletes text in document in the specified range in the UI thread.
     *
     * @param doc
     * @param start
     * @param end
     */
    public void deleteText(final Document doc, final int start, final int end) {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                commandProcessor.executeCommand(project, new Runnable() {

                    @Override
                        public void run() {
                            doc.deleteString(start, end);
                        }
                    }, "deleteText(" + start + "," + end + ")",
                    commandProcessor.getCurrentCommandGroupId());
            }
        };

        Filesystem.runWriteAction(action, ModalityState.defaultModalityState());
    }

    /**
     * Sets text selection in editor inside the UI thread.
     *
     * @param editor
     * @param start
     * @param end
     * @param colorMode
     */
    public void setSelection(final Editor editor, final int start,
        final int end, ColorModel colorMode) {

        Runnable action = new Runnable() {
            @Override
            public void run() {
                application.runReadAction(new Runnable() {
                    @Override
                    public void run() {
                        //set selection
                        editor.getSelectionModel().setSelection(start, end);

                        //move scroll
                        int lineStart = editor.getSelectionModel()
                            .getSelectionStartPosition().getLine();
                        int lineEnd = editor.getSelectionModel()
                            .getSelectionEndPosition().getLine();

                        int colStart = editor.getSelectionModel()
                            .getSelectionStartPosition().getColumn();
                        int colEnd = editor.getSelectionModel()
                            .getSelectionEndPosition().getColumn();

                        VisualPosition posCenter = new VisualPosition(
                            (lineStart + lineEnd) / 2, (colStart + colEnd) / 2);
                        editor.getCaretModel().moveToVisualPosition(posCenter);
                        editor.getScrollingModel()
                            .scrollToCaret(ScrollType.CENTER);

                        //move cursor
                        editor.getCaretModel().moveToOffset(start, true);
                    }
                });
            }
        };

        application.invokeAndWait(action, ModalityState.defaultModalityState());

    }
}
