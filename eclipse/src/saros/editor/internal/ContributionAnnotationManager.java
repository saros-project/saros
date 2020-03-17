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
        public void userLeft(User user) {
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
      ISarosSession sarosSession, IPreferenceStore preferenceStore) {

    this.sarosSession = sarosSession;
    this.preferenceStore = preferenceStore;
    this.preferenceStore.addPropertyChangeListener(propertyChangeListener);
    this.sarosSession.addListener(sessionListener);

    contribtionAnnotationsEnabled =
        preferenceStore.getBoolean(EclipsePreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS);
  }

  /**
   * Inserts a contribution annotation to given model if there is not already a contribution
   * annotation at given position. This method should be called after the text has changed.
   *
   * @param model to add the annotation to.
   * @param offset start of the annotation to add.
   * @param length length of the annotation.
   * @param source of the annotation.
   */
  public void insertAnnotation(IAnnotationModel model, int offset, int length, User source) {

    if (!contribtionAnnotationsEnabled) return;

    if (length < 0) // why is 0 len allowed ?
    return;

    /* Return early if there already is an annotation at that offset */
    for (Iterator<Annotation> it = model.getAnnotationIterator(); it.hasNext(); ) {
      Annotation annotation = it.next();

      if (annotation instanceof ContributionAnnotation
          && model.getPosition(annotation).includes(offset)
          && ((ContributionAnnotation) annotation).getSource().equals(source)) {
        return;
      }
    }

    addContributionAnnotation(
        new ContributionAnnotation(source, model), new Position(offset, length));
  }

  /**
   * Refreshes all contribution annotations in the model by removing and reinserting them.
   *
   * @param model the annotation model that should be refreshed
   */
  public void refreshAnnotations(IAnnotationModel model) {

    List<Annotation> annotationsToRemove = new ArrayList<Annotation>();
    Map<Annotation, Position> annotationsToAdd = new HashMap<Annotation, Position>();

    for (Iterator<Annotation> it = model.getAnnotationIterator(); it.hasNext(); ) {

      Annotation annotation = it.next();

      if (!(annotation instanceof ContributionAnnotation)) continue;

      Position position = model.getPosition(annotation);

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

      ContributionAnnotation annotationToReplace = (ContributionAnnotation) annotation;
      User source = annotationToReplace.getSource();
      ContributionAnnotation annotationToAdd = new ContributionAnnotation(source, model);

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
   * Add a contribution annotation to the annotation model and store it into the history of the
   * associated user. Old entries are removed from the history and the annotation model.
   */
  private void addContributionAnnotation(
      final ContributionAnnotation annotation, final Position position) {

    final ContributionAnnotationHistory history = getHistory(annotation.getSource());

    final Pair<IAnnotationModel, List<ContributionAnnotation>> annotationToRemove =
        history.removeHistoryEntry();

    history.addNewEntry(annotation);

    if (annotationToRemove == null) {
      annotation.getModel().addAnnotation(annotation, position);
      return;
    }

    // TODO OPTIMIZE it is usually 2 annotations after the queue is full
    annotation.getModel().addAnnotation(annotation, position);

    for (ContributionAnnotation a : annotationToRemove.getRight()) {
      annotationToRemove.getLeft().removeAnnotation(a);
    }
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
      List<ContributionAnnotation> allAnnotationsInHistory = history.clear();
      for (ContributionAnnotation annotation : allAnnotationsInHistory) {
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
