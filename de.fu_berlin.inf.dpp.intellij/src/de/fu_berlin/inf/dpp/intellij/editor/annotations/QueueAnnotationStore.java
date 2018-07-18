package de.fu_berlin.inf.dpp.intellij.editor.annotations;

import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.session.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * A class to store a limited number of annotations of a certain type. The store
 * operates like a queue, meaning, if the store is full, the oldest annotation
 * is removed. This is not done automatically but rather should be done by the
 * caller by calling {@link #removeIfFull()} before adding new annotations.
 *
 * @param <E> the stored annotation type
 */
class QueueAnnotationStore<E extends AbstractEditorAnnotation>
    extends AnnotationStore<E> {

    private final Queue<E> annotationQueue;

    private final int maxSize;

    /**
     * Creates an annotation store with the given capacity. The given capacity
     * needs to be at least 1.
     *
     * @param maxSize the capacity of the annotation store.
     */
    QueueAnnotationStore(int maxSize) {
        super();

        if (maxSize < 1) {
            throw new IllegalArgumentException(
                "The given size of the queue must be at least 1. maxSize: "
                    + maxSize);
        }

        this.maxSize = maxSize;

        this.annotationQueue = new LinkedList<>();
    }

    /**
     * Removes and returns the last element of the annotation queue if the queue
     * has reached its maximum size.
     * <p>
     * <b>NOTE:</b> This does not remove the annotation from the local editor.
     * </p>
     * This method should always be called before
     * {@link #addAnnotation(AbstractEditorAnnotation)}.
     *
     * @return the last element of the annotation queue if the queue has reached
     * its maximum size or <code>null</code> otherwise
     */
    @Nullable
    E removeIfFull() {
        if (annotationQueue.size() == maxSize) {
            return annotationQueue.remove();
        }

        return null;
    }

    /**
     * Returns the current size of the annotation queue.
     *
     * @return the current size of the annotation queue
     */
    int getSize() {
        return annotationQueue.size();
    }

    /**
     * Returns the maximum size of the annotation queue.
     *
     * @return the maximum size of the annotation queue
     */
    int getMaxSize() {
        return maxSize;
    }

    /**
     * Adds the given annotation to the annotation store.
     * <p>
     * {@link #removeIfFull()} should always be called before this method to
     * ensure that the queue is not full when trying to add an element.
     * </p>
     *
     * @param annotation the annotation to add
     * @throws IllegalStateException if the annotation queue is full
     */
    @Override
    void addAnnotation(
        @NotNull
            E annotation) {

        if (annotationQueue.size() >= maxSize) {
            throw new IllegalStateException("The queue already contains the "
                + "allowed number of annotations.");
        }

        super.addAnnotation(annotation);

        annotationQueue.add(annotation);
    }

    @Override
    void removeAnnotation(
        @NotNull
            E annotation) {

        super.removeAnnotation(annotation);

        annotationQueue.remove(annotation);
    }

    @Override
    @NotNull
    List<E> removeAnnotations(
        @NotNull
            User user,
        @NotNull
            IFile file) {

        List<E> removedAnnotations = super.removeAnnotations(user, file);

        annotationQueue.removeAll(removedAnnotations);

        return removedAnnotations;
    }

    @Override
    @NotNull
    List<E> removeAnnotations(
        @NotNull
            User user) {

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
