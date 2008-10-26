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
     */
    public static void insertAnnotation(IAnnotationModel model, int offset,
	    int length) {

	for (@SuppressWarnings("unchecked")
	Iterator it = model.getAnnotationIterator(); it.hasNext();) {
	    Annotation annotation = (Annotation) it.next();

	    if (!annotation.getType().equals(ContributionAnnotation.TYPE)) {
		continue;
	    }

	    if (model.getPosition(annotation).includes(offset)) {
		return;
	    }
	}

	if (length > 0) {
	    Position position = new Position(offset, length);
	    AnnotationSaros annotation = new ContributionAnnotation();
	    model.addAnnotation(annotation, position);
	}
    }

    /**
     * Inserts a contribution annotation to given model if there is not already
     * a contribution annotation at given position. This method should be called
     * after the text has changed.
     */
    public static void insertAnnotation(IAnnotationModel model, int offset,
	    int length, String source) {
	for (@SuppressWarnings("unchecked")
	Iterator it = model.getAnnotationIterator(); it.hasNext();) {
	    Annotation annotation = (Annotation) it.next();

	    if (!annotation.getType().equals(ContributionAnnotation.TYPE)) {
		continue;
	    }

	    if (model.getPosition(annotation).includes(offset)) {
		return;
	    }
	}

	if (length > 0) {
	    Position position = new Position(offset, length);
	    AnnotationSaros annotation = new ContributionAnnotation("", source);
	    model.addAnnotation(annotation, position);
	}
    }

    /**
     * Splits the contribution annotation at given position, so that the
     * following text change won't expand the annotation. This needs to be
     * called before the text is changed.
     */
    public static void splitAnnotation(IAnnotationModel model, int offset) {
	for (@SuppressWarnings("unchecked")
	Iterator it = model.getAnnotationIterator(); it.hasNext();) {
	    Annotation annotation = (Annotation) it.next();

	    if (!annotation.getType().equals(ContributionAnnotation.TYPE)) {
		continue;
	    }

	    Position pos = model.getPosition(annotation);

	    if ((offset > pos.offset) && (offset < pos.offset + pos.length)) {
		Position pos1 = new Position(pos.offset, offset - pos.offset);
		Position pos2 = new Position(offset, pos.length
			- (offset - pos.offset));

		model.removeAnnotation(annotation);
		/* get source information and create an split annotation. */
		model.addAnnotation(new ContributionAnnotation("",
			((ContributionAnnotation) annotation).getSource()),
			pos1);
		model.addAnnotation(new ContributionAnnotation("",
			((ContributionAnnotation) annotation).getSource()),
			pos2);
	    }
	}
    }
}
