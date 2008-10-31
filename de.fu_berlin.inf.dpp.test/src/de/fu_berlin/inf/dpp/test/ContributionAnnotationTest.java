package de.fu_berlin.inf.dpp.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;

import de.fu_berlin.inf.dpp.editor.annotations.ContributionAnnotation;
import de.fu_berlin.inf.dpp.editor.internal.ContributionHelper;

/**
 * Unit tests for contribution annotation insertions.
 * 
 * @author rdjemili
 */
public class ContributionAnnotationTest extends TestCase {

    /**
     * A simple Position class extension that overwrites the toString method.
     */
    private class PositionPrint extends Position {
	public PositionPrint(int offset, int length) {
	    super(offset, length);
	}

	public PositionPrint(Position position) {
	    super(position.offset, position.length);
	}

	@Override
	public String toString() {
	    return "(" + offset + ", " + length + ")";
	}
    }

    private AnnotationModel annotationModel;
    private Document document;

    @Override
    protected void setUp() throws Exception {
	document = new Document("abcdefghijklmnopqrstuvwxyz");

	annotationModel = new AnnotationModel();
	annotationModel.connect(document);
    }

    @Override
    protected void tearDown() throws Exception {
	annotationModel.disconnect(document);
    }

    /*
     * 0123456789 xxx
     */
    public void testInsertSimpleAnnotation() throws BadLocationException {
	document.replace(5, 0, "abc");
	ContributionHelper.insertAnnotation(annotationModel, 5, 3);
	assertPositions(annotationModel, new int[] { 5, 3 });
    }

    /*
     * 0123456789 xx---x
     */
    public void testExpandAnnotationAtCenter() throws BadLocationException {
	Position pos = new Position(3, 3);
	annotationModel.addAnnotation(new ContributionAnnotation(), pos);

	document.replace(5, 0, "abc");
	ContributionHelper.insertAnnotation(annotationModel, 5, 3);
	assertPositions(annotationModel, new int[] { 3, 6 });
    }

    /*
     * 0123456789 xxx---
     */
    public void testExpandAnnotationAtEnd() throws BadLocationException {
	Position pos = new Position(3, 3);
	annotationModel.addAnnotation(new ContributionAnnotation(), pos);

	document.replace(6, 0, "abc");
	ContributionHelper.insertAnnotation(annotationModel, 6, 3);
	assertPositions(annotationModel, new int[] { 3, 3, 6, 3 });
    }

    /*
     * 0123456789 ---xxx
     */
    public void testExpandAnnotationAtBegin() throws BadLocationException {
	Position pos = new Position(3, 3);
	annotationModel.addAnnotation(new ContributionAnnotation(), pos);

	document.replace(3, 0, "abc");
	ContributionHelper.insertAnnotation(annotationModel, 3, 3);
	assertPositions(annotationModel, new int[] { 3, 3, 6, 3 });
    }

    /*
     * 0123456789 xx___xxx
     */
    public void testSplitAnnotationAtCenter() throws BadLocationException {
	Position pos = new Position(1, 5);
	annotationModel.addAnnotation(new ContributionAnnotation(), pos);

	ContributionHelper.splitAnnotation(annotationModel, 3);
	document.replace(3, 0, "___");

	assertPositions(annotationModel, new int[] { 1, 2, 6, 3 });
    }

    /*
     * 0123456789 ___xxxxx
     */
    public void testDontExpandAnnotationAtBegin() throws BadLocationException {
	Position pos = new Position(1, 5);
	annotationModel.addAnnotation(new ContributionAnnotation(), pos);

	ContributionHelper.splitAnnotation(annotationModel, 1);
	document.replace(1, 0, "___");

	assertPositions(annotationModel, new int[] { 4, 5 });
    }

    /*
     * 0123456789 xxxxx___
     */
    public void testDontExpandAnnotationAtEnd() throws BadLocationException {
	Position pos = new Position(1, 5);
	annotationModel.addAnnotation(new ContributionAnnotation(), pos);

	ContributionHelper.splitAnnotation(annotationModel, 6);
	document.replace(6, 0, "___");

	assertPositions(annotationModel, new int[] { 1, 5 });
    }

    /**
     * Asserts if given annotation model contains annotations with given
     * positions.
     * 
     * @param model
     *            the annotation model.
     * @param posArray
     *            the positions of the annotations in (offset, length) format.
     *            Each two integers make up one positions. The positions can be
     *            given in any order.
     */
    private void assertPositions(AnnotationModel model, int[] posArray) {
	// convert array format to set of positions
	Set<PositionPrint> positions = new HashSet<PositionPrint>();
	for (int i = 0; i < posArray.length / 2; i++) {
	    positions.add(new PositionPrint(posArray[i * 2],
		    posArray[i * 2 + 1]));
	}

	// check if positions are equal
	Set<PositionPrint> positions2 = new HashSet<PositionPrint>();
	for (Iterator it = model.getAnnotationIterator(); it.hasNext();) {
	    Annotation annotation = (Annotation) it.next();

	    positions2.add(new PositionPrint(model.getPosition(annotation)));
	}

	assertEquals(positions, positions2);
    }
}
