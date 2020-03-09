package saros.editor.internal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.eclipse.jface.text.source.IAnnotationModel;
import saros.editor.annotations.ContributionAnnotation;

/** A history for the contribution annotations that has a fixed-size and uses a FIFO approach. */
class ContributionAnnotationHistory {

  private static final Logger log = Logger.getLogger(ContributionAnnotationHistory.class);
  private Queue<List<ContributionAnnotation>> queue;
  private int maxHistoryLength;

  ContributionAnnotationHistory(final int maxHistoryLength) {
    this.queue = new ArrayDeque<>(maxHistoryLength);
    this.maxHistoryLength = maxHistoryLength;
  }

  /** @return current size of the history */
  int getSize() {
    return queue.size();
  }

  /**
   * Removes all entries in the history and returns an aggregated list of all contribution
   * annotations in the history
   *
   * @return a flattened list of all history entries
   */
  List<ContributionAnnotation> clear() {
    final List<ContributionAnnotation> content = new ArrayList<>();
    while (!queue.isEmpty()) {
      content.addAll(queue.remove());
    }
    return content;
  }

  /**
   * Replaces an annotation in the history with another one. E.g. if we want to replace the
   * annotation D with D': [[A, B], [C], [D, E], [F], [G]] -> [[A, B], [C], [D', E], [F], [G]]
   *
   * @param oldAnnotation the annotation that has to be replaced.
   * @param newAnnotation the annotation that has to be inserted.
   */
  void replaceInHistory(
      final ContributionAnnotation oldAnnotation, final ContributionAnnotation newAnnotation) {

    for (final List<ContributionAnnotation> entry : queue) {

      for (final ListIterator<ContributionAnnotation> annotationsListIt = entry.listIterator();
          annotationsListIt.hasNext(); ) {
        final ContributionAnnotation annotation = annotationsListIt.next();

        if (annotation.equals(oldAnnotation)) {
          annotationsListIt.remove();

          assert oldAnnotation.getSource().equals(newAnnotation.getSource());

          annotationsListIt.add(newAnnotation);
          return;
        }
      }
    }

    log.warn(
        "could not find annotation "
            + oldAnnotation
            + " in the current history for user: "
            + oldAnnotation.getSource());
  }

  /**
   * @param annotations annotations to add as one entry into the history
   * @return If an entry has to be removed from the history in order to add the new one, the removed
   *     entry is returned with the corresponding {@link IAnnotationModel}. Otherwise return <code>
   *     null</code>.
   */
  Pair<IAnnotationModel, List<ContributionAnnotation>> addNewEntry(
      final List<ContributionAnnotation> annotations) {

    Pair<IAnnotationModel, List<ContributionAnnotation>> removedEntry = removeEntryIfFull();
    queue.add(annotations);
    return removedEntry;
  }

  private Pair<IAnnotationModel, List<ContributionAnnotation>> removeEntryIfFull() {

    Pair<IAnnotationModel, List<ContributionAnnotation>> removedEntry = null;

    if (queue.size() == maxHistoryLength) {
      final List<ContributionAnnotation> annotations = queue.remove();
      final IAnnotationModel model = annotations.get(0).getModel();
      removedEntry = new ImmutablePair<>(model, annotations);
    }

    return removedEntry;
  }
}
