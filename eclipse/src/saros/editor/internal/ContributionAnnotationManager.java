package saros.editor.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import saros.editor.EditorManager;
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
 *
 * <p>TODO Move responsibilities from {@link EditorManager} to here
 */
public class ContributionAnnotationManager {

  private static final Logger log = Logger.getLogger(ContributionAnnotationManager.class);

  static final int MAX_HISTORY_LENGTH = 20;

  private final Map<User, LinkedList<ContributionAnnotation>> sourceToHistory =
      new HashMap<User, LinkedList<ContributionAnnotation>>();

  private final ISarosSession sarosSession;

  private final IPreferenceStore preferenceStore;

  private boolean contribtionAnnotationsEnabled;

  private final ISessionListener sessionListener =
      new ISessionListener() {
        @Override
        public void userLeft(User user) {
          /*
           * Just remove the annotations from the history. They are removed by
           * the EditorManager from the editors.
           */
          sourceToHistory.remove(user);
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
        this.preferenceStore.getBoolean(EclipsePreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS);
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
  @SuppressWarnings("unchecked")
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
   * Splits the contribution annotation at given position, so that the following text change won't
   * expand the annotation. This needs to be called before the text is changed.
   *
   * @param model to search for annotations to split.
   * @param offset at which annotations should be splitted.
   */
  /*
   * FIXME that method is broken, it does not honor the history, i.e Alice
   * insert text ranges a, b, c, d ... now we split a into a0, a1 and as a
   * result a0 and a1 are now the newest entries instead the oldest
   *
   * See also https://github.com/saros-project/saros/issues/32 that includes
   * also another defect which is part of this behavior
   */
  @SuppressWarnings("unchecked")
  public void splitAnnotation(final IAnnotationModel model, final int offset) {

    if (!contribtionAnnotationsEnabled) return;

    final List<ContributionAnnotation> annotationsToRemove =
        new ArrayList<ContributionAnnotation>();
    final Map<ContributionAnnotation, Position> annotationsToAdd =
        new HashMap<ContributionAnnotation, Position>();

    for (Iterator<Annotation> it = model.getAnnotationIterator(); it.hasNext(); ) {

      final Annotation annotation = it.next();

      if (!(annotation instanceof ContributionAnnotation)) continue;

      final ContributionAnnotation contributionAnnotation = (ContributionAnnotation) annotation;
      final User source = contributionAnnotation.getSource();

      final Position pos = model.getPosition(contributionAnnotation);

      if ((offset > pos.offset) && (offset < pos.offset + pos.length)) {

        Position before = new Position(pos.offset, offset - pos.offset);

        Position after = new Position(offset, pos.length - (offset - pos.offset));

        annotationsToRemove.add(contributionAnnotation);

        annotationsToAdd.put(new ContributionAnnotation(source, model), before);

        annotationsToAdd.put(new ContributionAnnotation(source, model), after);
      }
    }

    for (final ContributionAnnotation annotation : annotationsToRemove)
      removeFromHistory(annotation);

    for (final Entry<ContributionAnnotation, Position> entry : annotationsToAdd.entrySet()) {
      addContributionAnnotation(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Refreshes all contribution annotations in the model by removing and reinserting them.
   *
   * @param model the annotation model that should be refreshed
   */
  @SuppressWarnings("unchecked")
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
      ContributionAnnotation annotationToAdd =
          new ContributionAnnotation(((ContributionAnnotation) annotation).getSource(), model);

      annotationsToAdd.put(annotationToAdd, position);

      replaceInHistory((ContributionAnnotation) annotation, annotationToAdd);
    }

    if (annotationsToRemove.isEmpty()) return;

    if (model instanceof IAnnotationModelExtension) {
      ((IAnnotationModelExtension) model)
          .replaceAnnotations(annotationsToRemove.toArray(new Annotation[0]), annotationsToAdd);

      return;
    }

    for (Annotation annotation : annotationsToRemove) model.removeAnnotation(annotation);

    for (Map.Entry<Annotation, Position> entry : annotationsToAdd.entrySet())
      model.addAnnotation(entry.getKey(), entry.getValue());
  }

  public void dispose() {
    sarosSession.removeListener(sessionListener);
    preferenceStore.removePropertyChangeListener(propertyChangeListener);
    sourceToHistory.clear();
  }

  /**
   * Get the history of contribution annotations of the given user.
   *
   * @param source source of the user who's history we want.
   * @return the history of source.
   */
  private Queue<ContributionAnnotation> getHistory(User source) {
    LinkedList<ContributionAnnotation> result = sourceToHistory.get(source);
    if (result == null) {
      result = new LinkedList<ContributionAnnotation>();
      sourceToHistory.put(source, result);
    }
    return result;
  }

  /**
   * Add a contribution annotation to the annotation model and store it into the history of the
   * associated user. Old entries are removed from the history and the annotation model.
   */
  private void addContributionAnnotation(ContributionAnnotation annotation, Position position) {

    annotation.getModel().addAnnotation(annotation, position);

    Queue<ContributionAnnotation> history = getHistory(annotation.getSource());
    history.add(annotation);
    while (history.size() > MAX_HISTORY_LENGTH) {
      ContributionAnnotation oldAnnotation = history.remove();
      oldAnnotation.getModel().removeAnnotation(oldAnnotation);
    }
  }

  /**
   * Removes an annotation from the user's history and the annotation model.
   *
   * @param annotation
   */
  private void removeFromHistory(ContributionAnnotation annotation) {
    getHistory(annotation.getSource()).remove(annotation);
    annotation.getModel().removeAnnotation(annotation);
  }

  /**
   * Replaces an existing annotation in the current history with a new annotation.
   *
   * @param oldAnnotation
   * @param newAnnotation
   */
  private void replaceInHistory(
      ContributionAnnotation oldAnnotation, ContributionAnnotation newAnnotation) {
    assert oldAnnotation.getSource().equals(newAnnotation.getSource());

    LinkedList<ContributionAnnotation> list = sourceToHistory.get(oldAnnotation.getSource());

    if (list == null) {
      log.warn("a annotation history for user " + oldAnnotation.getSource() + " does not exists");

      return;
    }

    for (ListIterator<ContributionAnnotation> it = list.listIterator(); it.hasNext(); ) {
      ContributionAnnotation annotation = it.next();
      if (annotation.equals(oldAnnotation)) {
        it.set(newAnnotation);
        return;
      }
    }

    log.warn(
        "could not find annotation "
            + oldAnnotation
            + " in the current history for user: "
            + oldAnnotation.getSource());
  }

  private void removeAllAnnotations() {
    for (Queue<ContributionAnnotation> queue : sourceToHistory.values())
      while (!queue.isEmpty()) removeFromHistory(queue.peek());
  }
}
