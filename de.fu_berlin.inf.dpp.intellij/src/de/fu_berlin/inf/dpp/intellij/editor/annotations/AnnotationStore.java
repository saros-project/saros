package de.fu_berlin.inf.dpp.intellij.editor.annotations;

import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.session.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * A class to store all current annotations of a certain type.
 *
 * @param <E> the stored annotation type
 */
class AnnotationStore<E extends AbstractEditorAnnotation> {
  private final Map<IFile, Map<User, List<E>>> annotationMap;

  AnnotationStore() {
    annotationMap = new HashMap<>();
  }

  /**
   * Adds the given annotation to the <code>AnnotationStore</code>.
   *
   * @param annotation the annotation to add
   */
  void addAnnotation(@NotNull E annotation) {

    IFile file = annotation.getFile();
    User user = annotation.getUser();

    Map<User, List<E>> annotationsForFile =
        annotationMap.computeIfAbsent(file, givenFile -> new HashMap<>());

    List<E> storedAnnotations =
        annotationsForFile.computeIfAbsent(user, givenFile -> new ArrayList<>());

    storedAnnotations.add(annotation);
  }

  /**
   * Returns all annotations for the given file.
   *
   * @param file the file to get annotations for
   * @return a list of annotations belonging to the given file
   */
  @NotNull
  List<E> getAnnotations(@NotNull IFile file) {

    List<E> annotations = new ArrayList<>();

    Map<User, List<E>> annotationsForFile = annotationMap.get(file);

    if (annotationsForFile != null) {
      annotationsForFile.forEach(
          (user, storedAnnotations) -> {
            if (storedAnnotations != null) {
              annotations.addAll(storedAnnotations);
            }
          });
    }

    return annotations;
  }

  /**
   * Removes the given annotation from the <code>AnnotationStore</code>.
   *
   * <p><b>NOTE:</b> This does not remove the annotation from the local editor.
   *
   * @param annotation the annotation to remove
   */
  void removeAnnotation(@NotNull E annotation) {

    User user = annotation.getUser();
    IFile file = annotation.getFile();

    Map<User, List<E>> annotationsForFile = annotationMap.get(file);

    if (annotationsForFile == null) {
      return;
    }

    List<E> storedAnnotations = annotationsForFile.get(user);

    if (storedAnnotations != null) {
      storedAnnotations.remove(annotation);
    }

    if (storedAnnotations == null || storedAnnotations.isEmpty()) {
      annotationsForFile.remove(user);
    }

    if (annotationsForFile.isEmpty()) {
      annotationMap.remove(file);
    }
  }

  /**
   * Removes all annotations belonging to the given user and file combination.
   *
   * <p><b>NOTE:</b> This does not remove the annotations from the local editor.
   *
   * @param user the user whose annotations to remove
   * @param file the file whose annotations to remove
   * @return a list of removed annotations
   */
  @NotNull
  List<E> removeAnnotations(@NotNull User user, @NotNull IFile file) {

    Map<User, List<E>> annotationsForFile = annotationMap.get(file);

    if (annotationsForFile == null) {
      return Collections.emptyList();
    }

    List<E> storedAnnotations = annotationsForFile.remove(user);

    if (annotationsForFile.isEmpty()) {
      annotationMap.remove(file);
    }

    if (storedAnnotations == null) {
      return Collections.emptyList();
    }

    return storedAnnotations;
  }

  /**
   * Removes all annotations belonging to the given user from the <code>AnnotationStore</code>.
   *
   * <p><b>NOTE:</b> This does not remove the annotations from the local editors.
   *
   * <p>This method should be used to remove annotations of users who left the session from the
   * store.
   *
   * @param user the user whose annotations to remove
   * @return the list of removed annotations
   */
  @NotNull
  List<E> removeAnnotations(@NotNull User user) {

    List<E> removedAnnotations = new ArrayList<>();
    List<IFile> emptyFileStores = new ArrayList<>();

    annotationMap.forEach(
        (file, annotationsForFile) -> {
          if (annotationsForFile == null) {
            return;
          }

          List<E> storedAnnotations = annotationsForFile.remove(user);

          if (storedAnnotations != null) {
            removedAnnotations.addAll(storedAnnotations);
          }

          if (annotationsForFile.isEmpty()) {
            emptyFileStores.add(file);
          }
        });

    emptyFileStores.forEach(annotationMap::remove);

    return removedAnnotations;
  }

  /**
   * Removes all annotations from the <code>AnnotationStore</code>.
   *
   * <p><b>NOTE:</b> This does not remove the annotations from the local editors.
   *
   * <p>This method should be used to remove all annotations after the session has ended.
   *
   * @return the list of removed annotations
   */
  @NotNull
  List<E> removeAllAnnotations() {
    List<E> removedAnnotations = new ArrayList<>();

    annotationMap.forEach(
        (file, annotationsForFile) -> {
          if (annotationsForFile != null) {
            annotationsForFile.forEach(
                (user, storedAnnotations) -> {
                  if (storedAnnotations != null) {
                    removedAnnotations.addAll(storedAnnotations);
                  }
                });
          }
        });

    annotationMap.clear();

    return removedAnnotations;
  }

  /**
   * Sets the given new file as the file for all annotations belonging to the given old file.
   *
   * <p>This method should be used to update the annotation store when a file is moved.
   *
   * @param oldFile the old file of the annotations
   * @param newFile the new file of the annotations
   */
  void updateAnnotationPath(@NotNull IFile oldFile, @NotNull IFile newFile) {

    Map<User, List<E>> oldMapping = annotationMap.remove(oldFile);

    if (oldMapping != null) {
      annotationMap.put(newFile, oldMapping);
    }
  }
}
