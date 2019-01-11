package de.fu_berlin.inf.dpp.intellij.editor.annotations;

import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.session.User;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class to store a limited number of annotations of a certain type. The store operates like a
 * queue, meaning, if the store is full, the oldest annotation is removed. This is not done
 * automatically but rather should be done by the caller by calling {@link #removeIfFull()} before
 * adding new annotations.
 *
 * @param <E> the stored annotation type
 */
class AnnotationQueue<E extends AbstractEditorAnnotation> extends AnnotationStore<E> {

  private final Queue<E> annotationQueue;

  private final int maxSize;

  /**
   * Creates an annotation store with the given capacity. The given capacity needs to be at least 1.
   *
   * @param maxSize the capacity of the annotation store.
   */
  AnnotationQueue(int maxSize) {
    super();

    if (maxSize < 1) {
      throw new IllegalArgumentException(
          "The given size of the queue must be at least 1. maxSize: " + maxSize);
    }

    this.maxSize = maxSize;

    this.annotationQueue = new ArrayDeque<>(maxSize);
  }

  /**
   * Removes and returns the oldest element of the annotation queue if the queue has reached its
   * maximum size.
   *
   * <p><b>NOTE:</b> This does not remove the annotation from the local editor. This method should
   * always be called before {@link #addAnnotation(AbstractEditorAnnotation)}.
   *
   * @return the oldest element of the annotation queue if the queue has reached its maximum size or
   *     <code>null</code> otherwise
   */
  @Nullable
  E removeIfFull() {
    if (annotationQueue.size() == maxSize) {
      return annotationQueue.remove();
    }

    return null;
  }

  /**
   * Adds the given annotation to the annotation store.
   *
   * <p>{@link #removeIfFull()} should always be called before this method to ensure that the queue
   * is not full when trying to add an element.
   *
   * @param annotation the annotation to add
   * @throws IllegalStateException if the annotation queue is full
   */
  @Override
  void addAnnotation(@NotNull E annotation) {

    if (annotationQueue.size() >= maxSize) {
      throw new IllegalStateException(
          "The queue already contains the " + "allowed number of annotations.");
    }

    super.addAnnotation(annotation);

    annotationQueue.add(annotation);
  }

  @Override
  void removeAnnotation(@NotNull E annotation) {

    super.removeAnnotation(annotation);

    annotationQueue.remove(annotation);
  }

  @Override
  @NotNull
  List<E> removeAnnotations(@NotNull User user, @NotNull IFile file) {

    List<E> removedAnnotations = super.removeAnnotations(user, file);

    annotationQueue.removeAll(removedAnnotations);

    return removedAnnotations;
  }

  @Override
  @NotNull
  List<E> removeAnnotations(@NotNull User user) {

    List<E> removedAnnotations = super.removeAnnotations(user);

    annotationQueue.removeAll(removedAnnotations);

    return removedAnnotations;
  }

  @Override
  @NotNull
  List<E> removeAllAnnotations() {
    annotationQueue.clear();

    return super.removeAllAnnotations();
  }
}
