package saros.stf.test.whiteboard;

import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.tester.AbstractTester;
import saros.stf.client.util.Util;
import saros.stf.server.rmi.superbot.component.view.whiteboard.ISarosWhiteboardView.Tool;
import saros.stf.server.rmi.superbot.component.view.whiteboard.IWhiteboardFigure;
import saros.stf.shared.Constants;

public class ObjectCreationTest extends StfTestCase {
  private static final String fileContent =
      "1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n";

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
    Util.setUpSessionWithProjectAndFile("foo", "readme.txt", fileContent, ALICE, BOB);
    for (AbstractTester tester : getCurrentTesters()) {
      if (!tester.remoteBot().isViewOpen(Constants.VIEW_SAROS_WHITEBOARD)) {
        tester
            .superBot()
            .menuBar()
            .window()
            .showViewWithName(Constants.NODE_SAROS, Constants.VIEW_SAROS_WHITEBOARD);
      }
      // maximize the whiteboard to prevent problems with the SWTGefBot
      tester.superBot().views().sarosWhiteboardView().toggleMaximize();
      // set equal zoom
      tester.superBot().views().sarosWhiteboardView().setZoom(100);
    }
  }

  @AfterClass
  public static void tearDown() throws Exception {
    // restore whiteboard view
    for (AbstractTester tester : getCurrentTesters()) {
      tester.superBot().views().sarosWhiteboardView().toggleMaximize();
    }
  }

  @Before
  public void clearWhiteboard() throws Exception {
    for (AbstractTester tester : getCurrentTesters()) {
      tester.superBot().views().sarosWhiteboardView().clear();
      System.out.println("clearing");
    }
    System.out.println("cleared");
  }

  @Test
  public void testRectangleCreation() throws Exception {
    checkObjectFromActiveTool(Tool.RECTANGLE);
  }

  @Test
  public void testEllipseCreation() throws Exception {
    checkObjectFromActiveTool(Tool.ELLIPSE);
  }

  @Test
  public void testPointlistCreation() throws Exception {
    checkObjectFromActiveTool(Tool.PENCIL);
  }

  private void checkObjectFromActiveTool(Tool activeTool) throws Exception {
    // calculate click and object values
    int x = 99;
    int y = 98;
    int width = 97;
    int height = 96;

    int clickX = x;
    int clickY = y;
    switch (activeTool) {
      case ELLIPSE:
        // click on the NORTH position of the enclosing rectangle
        clickX = x + width / 2;
        clickY = y;
        break;
      case RECTANGLE:
      case DIAMOND:
      case PENCIL:
      default:
        break;
    }

    // Alice creates an object from active tool with given parameters
    ALICE.superBot().views().sarosWhiteboardView().activateTool(activeTool.getLabel());
    ALICE.superBot().views().sarosWhiteboardView().createObjectFromActiveTool(x, y, width, height);
    Thread.sleep(2000);

    // obtain object figure from each bob and alice
    IWhiteboardFigure rectAlice =
        ALICE.superBot().views().sarosWhiteboardView().getFigureAt(clickX, clickY);
    IWhiteboardFigure rectBob =
        BOB.superBot().views().sarosWhiteboardView().getFigureAt(clickX, clickY);
    // check if the figure created on Bob's whiteboard equals the figure of
    // Alice
    checkWhiteboardfigure(rectAlice, rectBob);
  }

  private void checkWhiteboardfigure(IWhiteboardFigure figure1, IWhiteboardFigure figure2)
      throws Exception {
    org.junit.Assert.assertNotNull(figure1);
    org.junit.Assert.assertNotNull(figure2);
    org.junit.Assert.assertEquals(figure1.getType(), figure2.getType());
    org.junit.Assert.assertEquals(figure1.getSize(), figure2.getSize());
    org.junit.Assert.assertEquals(figure1.getLocation(), figure2.getLocation());
    org.junit.Assert.assertEquals(figure1.getBackgroundColor(), figure2.getBackgroundColor());
    org.junit.Assert.assertEquals(figure1.getForegroundColor(), figure2.getForegroundColor());
  }
}
