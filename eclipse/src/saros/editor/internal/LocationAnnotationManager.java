package saros.editor.internal;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import saros.editor.annotations.RemoteCursorAnnotation;
import saros.editor.annotations.SarosAnnotation;
import saros.editor.annotations.SelectionAnnotation;
import saros.editor.annotations.SelectionFillUpAnnotation;
import saros.editor.annotations.ViewportAnnotation;
import saros.editor.text.LineRange;
import saros.editor.text.TextSelection;
import saros.preferences.EclipsePreferenceConstants;
import saros.session.User;
import saros.ui.util.SWTUtils;
import saros.util.Predicate;

/**
 * This class is responsible for managing annotations related with other users' locations, i.e.
 * their cursors, selections, and viewports.
 */
public class LocationAnnotationManager {

  private static final Logger LOG = Logger.getLogger(LocationAnnotationManager.class);

  private AnnotationModelHelper annotationModelHelper;

  private boolean fillUpEnabled;

  public LocationAnnotationManager(IPreferenceStore preferenceStore) {
    annotationModelHelper = new AnnotationModelHelper();

    fillUpEnabled =
        preferenceStore.getBoolean(EclipsePreferenceConstants.SHOW_SELECTIONFILLUP_ANNOTATIONS);
  }

  /**
   * Listens for changes to the preferences whether to show {@link SelectionFillUpAnnotation}s.
   *
   * <p>In principle, this class could also listen to preference changes directly. But since we want
   * to remove existing annotations when the option is disabled, and this class doesn't have access
   * to all open editors, we leave the actual listening to a class that does.
   *
   * @param event
   * @param allEditors
   */
  public void propertyChange(final PropertyChangeEvent event, final Set<IEditorPart> allEditors) {

    if (!EclipsePreferenceConstants.SHOW_SELECTIONFILLUP_ANNOTATIONS.equals(event.getProperty()))
      return;

    SWTUtils.runSafeSWTAsync(
        LOG,
        new Runnable() {

          @Override
          public void run() {
            fillUpEnabled = Boolean.valueOf(event.getNewValue().toString());

            if (!fillUpEnabled) {
              Predicate<Annotation> predicate =
                  new Predicate<Annotation>() {
                    @Override
                    public boolean evaluate(Annotation annotation) {
                      return (annotation instanceof SelectionFillUpAnnotation);
                    }
                  };

              for (IEditorPart editorPart : allEditors) {
                annotationModelHelper.removeAnnotationsFromEditor(editorPart, predicate);
              }
            }
          }
        });
  }

  /**
   * Create or update the annotations that represent a user's viewport (visible lines of code).
   *
   * @param user The remote user whose viewport should be visualized.
   * @param editorPart The {@link IEditorPart} which shows the file that the remote user has opened,
   *     too.
   * @param lineRange The {@link LineRange} that is visible to the remote user.
   */
  public void setViewportForUser(final User user, IEditorPart editorPart, LineRange lineRange) {

    ITextViewer viewer = EditorAPI.getViewer(editorPart);
    if (!(viewer instanceof ISourceViewer)) {
      return;
    }

    IDocument document = viewer.getDocument();
    IAnnotationModel model = ((ISourceViewer) viewer).getAnnotationModel();

    if (model == null) {
      return;
    }

    // TODO Make use of AnnotationModelHelper.replaceAnnotationsInModel()

    int top = lineRange.getStartLine();
    int bottom = top + lineRange.getNumberOfLines();

    // Clean-up. Remove any existing ViewportAnnotations of the given user
    annotationModelHelper.removeAnnotationsFromModel(
        model,
        new Predicate<Annotation>() {
          @Override
          public boolean evaluate(Annotation annotation) {
            return (annotation instanceof ViewportAnnotation)
                && ((SarosAnnotation) annotation).getSource().equals(user);
          }
        });

    // Calculate the position of the new ViewportAnnotation and then add it
    // to the model
    try {
      int lines = document.getNumberOfLines();
      top = Math.max(0, Math.min(lines - 1, top));
      bottom = Math.max(0, Math.min(lines - 1, bottom));

      int start = document.getLineOffset(top);
      int end = document.getLineOffset(bottom);

      if (start == -1) throw new BadLocationException("Start line -1");

      if (end == -1 || end < start)
        throw new BadLocationException("End line -1 or less than start");

      ViewportAnnotation va = new ViewportAnnotation(user);

      if (lines > 1) va.setMoreThanOneLine(true);

      // TODO Is there a reason *not* to use "va"?
      SarosAnnotation annotation = va;
      Position position = new Position(start, end - start);
      model.addAnnotation(annotation, position);
    } catch (BadLocationException e) {
      LOG.warn("Internal Error:", e);
    }
  }

  /**
   * Remove any existing viewport-related annotations of a user that might exist inside the given
   * {@link IEditorPart}.
   *
   * @param user
   * @param editorPart
   */
  public void clearViewportForUser(final User user, IEditorPart editorPart) {
    IAnnotationModel model = annotationModelHelper.retrieveAnnotationModel(editorPart);

    if (model == null) {
      return;
    }

    annotationModelHelper.removeAnnotationsFromModel(
        model,
        new Predicate<Annotation>() {
          @Override
          public boolean evaluate(Annotation annotation) {
            return (annotation instanceof ViewportAnnotation)
                && ((SarosAnnotation) annotation).getSource().equals(user);
          }
        });
  }

  /**
   * Create or update annotations related to text selections made by remote users.
   *
   * <p>Such selections consist of a highlight (one character wide, if there is no actual text
   * selection) and a vertical line that resembles the local text cursor. If the selection includes
   * multiple lines an additional element will be created to highlight the space between a line's
   * last character and the right margin.
   *
   * @param source The remote user who made the text selection (or to whom the text cursor belongs).
   * @param selection The selection itself.
   * @param editorPart {@link IEditorPart} that displays the opened document of which the
   *     annotations should be updated.
   */
  public void setSelection(IEditorPart editorPart, TextSelection selection, User source) {

    if (!(editorPart instanceof ITextEditor)) return;

    ITextEditor textEditor = (ITextEditor) editorPart;
    IDocumentProvider docProvider = textEditor.getDocumentProvider();

    if (docProvider == null) return;

    IEditorInput input = textEditor.getEditorInput();
    IAnnotationModel model = docProvider.getAnnotationModel(input);

    if (model == null) return;

    if (selection.isEmpty()) {
      clearSelectionForUser(source, editorPart);
      return;
    }

    int offset = selection.getOffset();
    int length = selection.getLength();
    boolean isCursor = length == 0;

    // TODO For better performance: Currently, all selection-related
    // annotations are created and replaced individually. Since the access
    // to the annotation model tends to be slow and the replacement may take
    // place in batches, one could first create all new selection-related
    // annotations and replace them at once.

    if (isCursor) {
      if (offset > 0) {
        /*
         * Highlight the character left of the cursor in the light color
         * of the user.
         */
        setSelectionAnnotation(source, isCursor, new Position(offset - 1, 1), model);
      } else {
        /*
         * We have to draw this "highlight" even though it's not visible
         * at all. This is to prevent ghosting of the highlight when
         * jumping to the beginning of the file (offset == 0).
         */
        setSelectionAnnotation(source, isCursor, new Position(0, 0), model);
      }
    } else {
      /*
       * Highlight the selection of a remote user in the remote user's
       * light color.
       */
      setSelectionAnnotation(source, isCursor, new Position(offset, length), model);
    }

    /*
     * Draw a cursor at the cursor position of other user in the current
     * session. When there is a selection, the cursor will be shown at the
     * end of it.
     */
    setRemoteCursorAnnotation(source, new Position(offset + length), model);

    if (fillUpEnabled) {
      setFillUpAnnotation(source, new Position(offset, length), model);
    }
  }

  /**
   * Removes all selection-related annotations of a user inside the given {@link IEditorPart}.
   *
   * @param user The originator of the annotations to be deleted.
   * @param editorPart
   */
  public void clearSelectionForUser(final User user, IEditorPart editorPart) {
    IAnnotationModel model = annotationModelHelper.retrieveAnnotationModel(editorPart);

    if (model == null) {
      return;
    }

    annotationModelHelper.removeAnnotationsFromModel(
        model,
        new Predicate<Annotation>() {
          @Override
          public boolean evaluate(Annotation annotation) {
            return (annotation instanceof SelectionAnnotation
                    || annotation instanceof SelectionFillUpAnnotation
                    || annotation instanceof RemoteCursorAnnotation)
                && ((SarosAnnotation) annotation).getSource().equals(user);
          }
        });
  }

  /**
   * Helper function to create and add an annotation that highlights the selected text (see {@link
   * SelectionAnnotation}).
   */
  private void setSelectionAnnotation(
      User user, boolean isCursor, Position position, IAnnotationModel annotationModel) {

    setAnnotationForSelection(new SelectionAnnotation(user, isCursor), position, annotationModel);
  }

  /**
   * Helper function to create an additional annotation that highlights the empty space between a
   * line's last character an the right margin (see {@link SelectionFillUpAnnotation}).
   */
  private void setFillUpAnnotation(User user, Position position, IAnnotationModel annotationModel) {

    setAnnotationForSelection(
        new SelectionFillUpAnnotation(user, position.offset, position.length),
        position,
        annotationModel);
  }

  /**
   * Helper function to create an add an annotation that marks the current text cursor position (see
   * {@link RemoteCursorAnnotation}).
   */
  private void setRemoteCursorAnnotation(
      User user, Position position, IAnnotationModel annotationModel) {

    setAnnotationForSelection(new RemoteCursorAnnotation(user), position, annotationModel);
  }

  /**
   * Sets annotations related to selections made by remote users.
   *
   * @param newAnnotation {@link SarosAnnotation} that is set during this call.
   * @param position {@link Position} at which the annotation is replaced, removed, or updated.
   * @param model {@link IAnnotationModel} that maintains the annotations for the opened document.
   */
  private void setAnnotationForSelection(
      final SarosAnnotation newAnnotation, Position position, IAnnotationModel model) {

    if (newAnnotation == null || position == null) {
      throw new IllegalArgumentException("Both newAnnotation and position must not be null");
    }

    Predicate<Annotation> predicate =
        new Predicate<Annotation>() {
          @Override
          public boolean evaluate(Annotation annotation) {
            return annotation.getType().equals(newAnnotation.getType())
                && ((SarosAnnotation) annotation).getSource().equals(newAnnotation.getSource());
          }
        };

    Map<Annotation, Position> replacement =
        Collections.singletonMap((Annotation) newAnnotation, position);

    annotationModelHelper.replaceAnnotationsInModel(model, predicate, replacement);
  }
}
