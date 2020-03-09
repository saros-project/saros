package saros.editor.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import saros.editor.annotations.ContributionAnnotation;
import saros.preferences.EclipsePreferenceConstants;
import saros.session.ISarosSession;
import saros.session.ISessionListener;
import saros.session.User;
import saros.ui.util.SWTUtils;

/**
 * Throughout a session, the user should be made aware of the textual changes made by the other
 * session participants. Additions and changes are represented by {@link ContributionAnnotation}s
 * and distinguished by authors (deletions are not highlighted). The Annotations are added in
 * real-time along with the application of the textual changes and are removed when the characters
 * they belong to are deleted, the session ends, or their respective author leaves the session. To
 * avoid cluttering the editors, only the last {@value #MAX_HISTORY_LENGTH} changes are annotated.
 *
 * <p>This class takes care of managing the annotations for session participants which involves
 * adding, removing, and splitting of Annotations.
 */
// <p>TODO Move responsibilities from EditorManager to here
public class ContributionAnnotationManager {

  private static final Logger log = Logger.getLogger(ContributionAnnotationManager.class);

  private final Map<User, ContributionAnnotationHistory> sourceToHistory = new HashMap<>();

  static final int MAX_HISTORY_LENGTH = 20;

  private final ISarosSession sarosSession;

  private final IPreferenceStore preferenceStore;

  private boolean contribtionAnnotationsEnabled;

  private final AnnotationModelHelper annotationModelHelper = new AnnotationModelHelper();

  private final ISessionListener sessionListener =
      new ISessionListener() {
        @Override
        public void userLeft(final User user) {
          removeAnnotationsForUser(user);
        }
      };

  private final IPropertyChangeListener propertyChangeListener =
      new IPropertyChangeListener() {

        @Override
        public void propertyChange(final PropertyChangeEvent event) {

          if (!EclipsePreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS.equals(event.getProperty()))
            return;

          SWTUtils.runSafeSWTAsync(
              log,
              new Runnable() {

                @Override
                public void run() {
                  contribtionAnnotationsEnabled = Boolean.valueOf(event.getNewValue().toString());

                  if (!contribtionAnnotationsEnabled) removeAllAnnotations();
                }
              });
        }
      };

  public ContributionAnnotationManager(
      final ISarosSession sarosSession, final IPreferenceStore preferenceStore) {

    this.sarosSession = sarosSession;
    this.preferenceStore = preferenceStore;
    this.preferenceStore.addPropertyChangeListener(propertyChangeListener);
    this.sarosSession.addListener(sessionListener);

    contribtionAnnotationsEnabled =
        preferenceStore.getBoolean(EclipsePreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS);
  }

  /**
   * Inserts contribution annotations to given model if there is not already a contribution
   * annotation at given position. This method should be called after the text has changed.
   *
   * @param model to add the annotation to.
   * @param offset start of the annotation to add.
   * @param length length of the annotation.
   * @param source of the annotation.
   */
  public void insertAnnotation(
      final IAnnotationModel model, final int offset, final int length, final User source) {

    // Return if length == 0, (called after a deletion was performed)
    if (!contribtionAnnotationsEnabled || length <= 0) return;

    final ContributionAnnotationHistory history = getHistory(source);

    final Map<ContributionAnnotation, Position> annotationsToAdd =
        createAnnotationsForContributionRange(model, offset, length, source);

    final Pair<IAnnotationModel, List<ContributionAnnotation>> annotationsToRemove =
        history.addNewEntry(new ArrayList<>(annotationsToAdd.keySet()));

    // Case 1: An insert operation is performed and the history is full
    if (annotationsToRemove != null) {
      final IAnnotationModel annotationsToRemoveModel = annotationsToRemove.getLeft();

      if (annotationsToRemoveModel.equals(model)) {
        annotationModelHelper.replaceAnnotationsInModel(
            annotationsToRemoveModel, annotationsToRemove.getRight(), annotationsToAdd);
      } else {
        annotationModelHelper.replaceAnnotationsInModel(
            annotationsToRemoveModel, annotationsToRemove.getRight(), Collections.emptyMap());
        annotationModelHelper.replaceAnnotationsInModel(
            model, Collections.emptyList(), annotationsToAdd);
      }
    }
    // Case 2: An insert operation is performed and the history is not full
    else {
      annotationModelHelper.replaceAnnotationsInModel(
          model, Collections.emptyList(), annotationsToAdd);
    }
  }

  /**
   * Creates contribution annotations with length 1 for each char contained in the range defined by
   * {@code offset} and {@code length}.
   *
   * @param model the model that is assigned to the created contribution annotations.
   * @param offset start of the annotation range.
   * @param length length of the annotation range.
   * @param source of the annotation.
   * @return a map containing the annotations and their positions
   */
  private Map<ContributionAnnotation, Position> createAnnotationsForContributionRange(
      final IAnnotationModel model, final int offset, final int length, final User source) {

    final Map<ContributionAnnotation, Position> annotationsToAdd = new HashMap<>();

    for (int i = 0; i < length; i++) {
      final Pair<ContributionAnnotation, Position> positionedAnnotation =
          createPositionedAnnotation(model, offset + i, source);
      if (positionedAnnotation == null) continue;

      annotationsToAdd.put(positionedAnnotation.getKey(), positionedAnnotation.getValue());
    }

    return annotationsToAdd;
  }

  /**
   * Creates a contribution annotations with length 1 at position {@code offset}.
   *
   * @param model to add the annotation to.
   * @param offset start of the annotation to add.
   * @param source of the annotation.
   * @return a pair containing the annotation and its position.
   */
  private Pair<ContributionAnnotation, Position> createPositionedAnnotation(
      final IAnnotationModel model, final int offset, final User source) {
    final int ANNOTATION_LENGTH = 1;

    return new ImmutablePair<ContributionAnnotation, Position>(
        new ContributionAnnotation(source, model), new Position(offset, ANNOTATION_LENGTH));
  }

  /**
   * Refreshes all contribution annotations in the model by removing and reinserting them.
   *
   * @param model the annotation model that should be refreshed
   */
  public void refreshAnnotations(final IAnnotationModel model) {

    final List<Annotation> annotationsToRemove = new ArrayList<Annotation>();
    final Map<Annotation, Position> annotationsToAdd = new HashMap<Annotation, Position>();

    for (final Iterator<Annotation> it = model.getAnnotationIterator(); it.hasNext(); ) {

      final Annotation annotation = it.next();

      if (!(annotation instanceof ContributionAnnotation)) continue;

      final Position position = model.getPosition(annotation);

      if (position == null) {
        log.warn("annotation could not be found in the current model: " + annotation);
        continue;
      }

      /*
       * we rely on the fact the a user object is unique during a running
       * session so that user.equals(user) <=> user == user otherwise just
       * reinserting the annotations would not refresh the colors as the
       * color id of the user has not changed
       */
      annotationsToRemove.add(annotation);

      final ContributionAnnotation annotationToReplace = (ContributionAnnotation) annotation;
      final User source = annotationToReplace.getSource();
      final ContributionAnnotation annotationToAdd = new ContributionAnnotation(source, model);

      annotationsToAdd.put(annotationToAdd, position);

      getHistory(source).replaceInHistory(annotationToReplace, annotationToAdd);
    }

    if (annotationsToRemove.isEmpty()) return;

    annotationModelHelper.replaceAnnotationsInModel(model, annotationsToRemove, annotationsToAdd);
  }

  public void dispose() {
    sarosSession.removeListener(sessionListener);
    preferenceStore.removePropertyChangeListener(propertyChangeListener);
    removeAllAnnotations();
  }

  /** Get the history of the given user. If no history is available a new one is created. */
  private ContributionAnnotationHistory getHistory(final User user) {
    return sourceToHistory.computeIfAbsent(
        user, u -> new ContributionAnnotationHistory(MAX_HISTORY_LENGTH));
  }

  /**
   * Removes all annotations from all annotation models that are currently stored in the history of
   * all users.
   */
  private void removeAllAnnotations() {
    removeFromHistoryAndAnnotationModel(sourceToHistory.values());
  }

  /**
   * Removes all annotations from all annotation models that are currently stored in the history for
   * the given user. The entries of the history are removed as well.
   */
  private void removeAnnotationsForUser(final User user) {
    final ContributionAnnotationHistory history = sourceToHistory.get(user);

    if (history != null) removeFromHistoryAndAnnotationModel(Collections.singletonList(history));
  }

  /**
   * Removes all annotations from all annotation models of the given histories. The entries of the
   * histories are removed as well.
   */
  private void removeFromHistoryAndAnnotationModel(
      final Collection<ContributionAnnotationHistory> histories) {

    final Set<IAnnotationModel> annotationModels = new HashSet<>();
    final Set<User> users = new HashSet<>();

    for (final ContributionAnnotationHistory history : histories) {
      final List<ContributionAnnotation> allAnnotationsInHistory = history.clear();
      for (final ContributionAnnotation annotation : allAnnotationsInHistory) {
        annotationModels.add(annotation.getModel());
        users.add(annotation.getSource());
      }
    }

    for (final IAnnotationModel annotationModel : annotationModels)
      annotationModelHelper.removeAnnotationsFromModel(
          annotationModel,
          (a) ->
              a instanceof ContributionAnnotation
                  && users.contains(((ContributionAnnotation) (a)).getSource()));
  }
}
