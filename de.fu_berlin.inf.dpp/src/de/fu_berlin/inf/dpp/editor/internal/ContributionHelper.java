package de.fu_berlin.inf.dpp.editor.internal;

import java.util.Iterator;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

import de.fu_berlin.inf.dpp.editor.annotations.AnnotationSaros;
import de.fu_berlin.inf.dpp.editor.annotations.ContributionAnnotation;

/**
 * A helper class for handling the contribution annotation.
 * 
 * @author rdjemili
 */
public class ContributionHelper {

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
    public static void insertAnnotation(IAnnotationModel model, int offset,
            int length, String source) {

        if (length > 0) {
            /* Return early if there already is an annotation at that offset */
            for (@SuppressWarnings("unchecked")
            Iterator it = model.getAnnotationIterator(); it.hasNext();) {
                Annotation annotation = (Annotation) it.next();

                if ((annotation.getType().equals(ContributionAnnotation.TYPE))
                        && (model.getPosition(annotation).includes(offset))) {
                    return;
                }
            }

            model.addAnnotation(new ContributionAnnotation(source),
                    new Position(offset, length));
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
    public static void splitAnnotation(IAnnotationModel model, int offset) {
        for (@SuppressWarnings("unchecked")
        Iterator it = model.getAnnotationIterator(); it.hasNext();) {
            Annotation annotation = (Annotation) it.next();

            if (annotation.getType().equals(ContributionAnnotation.TYPE)) {

                Position pos = model.getPosition(annotation);

                if ((offset > pos.offset) && (offset < pos.offset + pos.length)) {
                    Position beforeOffset = new Position(pos.offset, offset
                            - pos.offset);
                    Position afterOffset = new Position(offset, pos.length
                            - (offset - pos.offset));

                    model.removeAnnotation(annotation);

                    String source = ((AnnotationSaros) annotation).getSource();
                    model.addAnnotation(new ContributionAnnotation(source),
                            beforeOffset);
                    model.addAnnotation(new ContributionAnnotation(source),
                            afterOffset);
                }
            }
        }
    }
}
