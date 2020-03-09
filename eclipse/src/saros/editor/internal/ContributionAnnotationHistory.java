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

/** */
class ContributionAnnotationHistory {

  private static final Logger log = Logger.getLogger(ContributionAnnotationHistory.class);
  Queue<List<ContributionAnnotation>> queue;
  private int maxHistoryLength;

  protected ContributionAnnotationHistory(int maxHistoryLength) {
    this.queue = new ArrayDeque<>(maxHistoryLength);
    this.maxHistoryLength = maxHistoryLength;
  }

  /**
   * Removes all entries in the history and returns an aggregated list of all contribution
   * annotations in the history.
   *
   * @return A flattened all history entries
   */
  protected List<ContributionAnnotation> clear() {
    List<ContributionAnnotation> content = new ArrayList<>();
    while (!queue.isEmpty()) {
      content.addAll(queue.remove());
    }
    return content;
  }

  /** If the history */
  protected Pair<IAnnotationModel, List<ContributionAnnotation>> removeHistoryEntry() {

    Pair<IAnnotationModel, List<ContributionAnnotation>> removedEntry = null;

    if (queue.size() == maxHistoryLength) {
      List<ContributionAnnotation> annotations = queue.remove();
      IAnnotationModel model = annotations.get(0).getModel();
      for (ContributionAnnotation a : annotations) {
        if (a.getModel() != model) throw new RuntimeException("Different Models");
      }
      removedEntry = new ImmutablePair<>(model, annotations);
    }

    return removedEntry;
  }

  /*
   * Replaces the an annotation in the history with another one.
   * E.g. if we want to replace the annotation D with D':
   * [[A, B], [C], [D, E], [F], [G]] -> [[A, B], [C], [D', E], [F], [G]]
   *
   */
  protected void replaceInHistory(
      final ContributionAnnotation oldAnnotation, final ContributionAnnotation newAnnotation) {

    for (List<ContributionAnnotation> entry : queue) {

      for (final ListIterator<ContributionAnnotation> annotationsLit = entry.listIterator();
          annotationsLit.hasNext(); ) {
        final ContributionAnnotation annotation = annotationsLit.next();

        if (annotation.equals(oldAnnotation)) {
          annotationsLit.remove();

          assert oldAnnotation.getSource().equals(newAnnotation.getSource());

          annotationsLit.add(newAnnotation);
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

  protected void addNewEntry(List<ContributionAnnotation> annotations) {
    if (queue.size() >= maxHistoryLength) {
      throw new IllegalStateException(
          "The queue already contains the " + "allowed number of annotations.");
    }

    queue.add(annotations);
  }
}
