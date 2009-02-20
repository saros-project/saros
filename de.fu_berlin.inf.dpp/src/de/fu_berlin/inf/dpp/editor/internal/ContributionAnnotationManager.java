package de.fu_berlin.inf.dpp.editor.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

import de.fu_berlin.inf.dpp.editor.annotations.ContributionAnnotation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;

/**
 * This class keeps a history of added {@link ContributionAnnotation}s and
 * removes old ones.
 */
public class ContributionAnnotationManager {
    private static int MAX_HISTORY_LENGTH = 20;

    protected Map<String, Queue<ContributionAnnotation>> sourceToHistory = new HashMap<String, Queue<ContributionAnnotation>>();

    protected SharedProjectListener sharedProjectListener = new SharedProjectListener();

    protected class SharedProjectListener extends AbstractSharedProjectListener {
        @Override
        public void userLeft(JID user) {
            /*
             * Just remove the annotations from the history. They are removed by
             * the EditorManager from the editors.
             */
            ContributionAnnotationManager.this.sourceToHistory.remove(user
                .toString());
        }
    }

    ISharedProject project;

    public ContributionAnnotationManager(ISharedProject project) {
        this.project = project;
        sharedProjectListener = new SharedProjectListener();
        project.addListener(sharedProjectListener);
    }

    /**
     * Inserts a contribution annotation to given model if there is not already
     * a contribution annotation at given position. This method should be called
     * after the text has changed.
     * 
     * @param model
     *            to add the annotation to.
     * @param offset
     *            start of the annotation to add.
     * @param length
     *            length of the annotation.
     * @param source
     *            of the annotation.
     */
    public void insertAnnotation(IAnnotationModel model, int offset,
        int length, String source) {

        if (length > 0) {
            /* Return early if there already is an annotation at that offset */
            for (@SuppressWarnings("unchecked")
            Iterator<Annotation> it = model.getAnnotationIterator(); it
                .hasNext();) {
                Annotation annotation = it.next();

                if (annotation.getType().equals(ContributionAnnotation.TYPE)
                    && model.getPosition(annotation).includes(offset)
                    && ((ContributionAnnotation) annotation).getSource()
                        .equals(source)) {
                    return;
                }
            }

            ContributionAnnotation annotation = new ContributionAnnotation(
                source, model);
            addToHistory(annotation, new Position(offset, length));
        }
    }

    /**
     * Splits the contribution annotation at given position, so that the
     * following text change won't expand the annotation. This needs to be
     * called before the text is changed.
     * 
     * @param model
     *            to search for annotations to split.
     * @param offset
     *            at which annotations should be splitted.
     */
    public void splitAnnotation(IAnnotationModel model, int offset) {
        for (@SuppressWarnings("unchecked")
        Iterator<Annotation> it = model.getAnnotationIterator(); it.hasNext();) {
            Annotation annotation = it.next();

            if (annotation.getType().equals(ContributionAnnotation.TYPE)) {

                Position pos = model.getPosition(annotation);

                if ((offset > pos.offset) && (offset < pos.offset + pos.length)) {
                    Position beforeOffset = new Position(pos.offset, offset
                        - pos.offset);
                    Position afterOffset = new Position(offset, pos.length
                        - (offset - pos.offset));

                    ContributionAnnotation oldAnnotation = (ContributionAnnotation) annotation;

                    removeFromHistory(oldAnnotation);

                    ContributionAnnotation newAnnotation;
                    String source = oldAnnotation.getSource();

                    newAnnotation = new ContributionAnnotation(source, model);
                    addToHistory(newAnnotation, beforeOffset);

                    newAnnotation = new ContributionAnnotation(source, model);
                    addToHistory(newAnnotation, afterOffset);
                }
            }
        }
    }

    public void dispose() {
        this.project.removeListener(sharedProjectListener);
        sourceToHistory.clear();
    }

    /**
     * Get the history of contribution annotations of the given user.
     * 
     * @param source
     *            source of the user who's history we want.
     * @return the history of source.
     */
    protected Queue<ContributionAnnotation> getHistory(String source) {
        Queue<ContributionAnnotation> result = sourceToHistory.get(source);
        if (result == null) {
            result = new LinkedList<ContributionAnnotation>();
            sourceToHistory.put(source, result);
        }
        return result;
    }

    /**
     * Add a contribution annotation to the given user's history. Old entries
     * are removed from the history and the annotation model.
     * 
     * @param source
     *            user who's history should be used for the operation.
     * @param annotation
     *            the annotation to add.
     */
    protected void addToHistory(ContributionAnnotation annotation,
        Position position) {

        annotation.getModel().addAnnotation(annotation, position);

        Queue<ContributionAnnotation> history = getHistory(annotation
            .getSource());
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
    protected void removeFromHistory(ContributionAnnotation annotation) {
        getHistory(annotation.getSource()).remove(annotation);
        annotation.getModel().removeAnnotation(annotation);
    }
}
