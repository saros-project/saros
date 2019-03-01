package saros.intellij.editor.annotations;

import com.intellij.openapi.editor.Editor;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IFile;
import saros.session.User;

/**
 * Represents a Saros selection annotation. Selection annotations are used during a session to
 * highlight the text sections that are currently selected by other participants.
 */
class SelectionAnnotation extends AbstractEditorAnnotation {

  /**
   * Creates a selection annotation with the given parameters.
   *
   * <p>A selection annotation must have exactly one annotation range.
   *
   * @param user the user the annotation belongs to and whose color is used
   * @param file the file the annotation belongs to
   * @param editor the editor the annotation is displayed in
   * @param annotationRanges the range of the annotation
   * @see AbstractEditorAnnotation#AbstractEditorAnnotation(User, IFile, Editor, List)
   */
  SelectionAnnotation(
      @NotNull User user,
      @NotNull IFile file,
      @Nullable Editor editor,
      @NotNull List<AnnotationRange> annotationRanges) {

    super(user, file, editor, annotationRanges);

    if (annotationRanges.size() != 1) {
      throw new IllegalArgumentException(
          "A selection annotation must have exactly one " + "AnnotationRange.");
    }
  }
}
