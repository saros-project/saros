package de.fu_berlin.inf.dpp.whiteboard.gef.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.whiteboard.gef.model.GEFRecordFactory;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.LayoutElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.TestUtils;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord;
import org.apache.batik.util.SVGConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.junit.Test;

public class ConflictingCommandsTest {

  static final int[] points = {0, 0, 100, 100};
  static final PointList pointList = new PointList(points);

  DocumentRecord document = TestUtils.getEmptyDocument(new GEFRecordFactory());

  String origPointsAttribute = null;

  @Test
  public void testCreateNewPolyline() {

    PolylineRecordCreateCommand cmd = new PolylineRecordCreateCommand();

    cmd.setParent(document.getRoot());
    cmd.setPointList(pointList);
    cmd.execute();
    int numberOfVisibleChildren = document.getRoot().getVisibleChildElements().size();
    // There should be only one element, the Polyline
    assertEquals(
        "There should be only one element, the Polyline, but the document has "
            + numberOfVisibleChildren
            + " children.",
        numberOfVisibleChildren,
        1);

    LayoutElementRecord polyLine =
        (LayoutElementRecord) document.getRoot().getVisibleChildElements().get(0);

    assertEquals(
        "The Polyline should have exactly 1 attribute for each color and points.",
        2,
        polyLine.getVisibleAttributes().size());

    origPointsAttribute = polyLine.getAttributeValue(SVGConstants.SVG_POINTS_ATTRIBUTE);

    assertNotNull(origPointsAttribute);

    // }
    //
    // @Test
    // public void testConflictingSetRecords() {

    Point locOrig = polyLine.getLocation();
    Point loc1 = new Point(10, 10);
    Rectangle rect1 = new Rectangle(loc1, polyLine.getSize());

    ElementRecordChangeLayoutCommand cmd1 = new ElementRecordChangeLayoutCommand();
    cmd1.setModel(polyLine);
    cmd1.setConstraint(rect1);
    cmd1.execute();

    assertTrue(polyLine.getLocation().equals(loc1));

    Point loc2 = new Point(20, 20);
    Rectangle rect2 = new Rectangle(loc2, polyLine.getSize());

    ElementRecordChangeLayoutCommand cmd2 = new ElementRecordChangeLayoutCommand();
    cmd2.setModel(polyLine);
    cmd2.setConstraint(rect2);
    ConflictingSXECommand conflict2 = new ConflictingSXECommand(cmd2);
    conflict2.execute();

    assertTrue(polyLine.getLocation().equals(locOrig));
  }
}
