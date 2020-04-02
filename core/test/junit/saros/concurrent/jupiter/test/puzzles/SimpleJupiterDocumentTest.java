package saros.concurrent.jupiter.test.puzzles;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static saros.test.util.OperationHelper.I;

import org.junit.Test;
import saros.activities.JupiterActivity;
import saros.concurrent.jupiter.Algorithm;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.internal.Jupiter;
import saros.concurrent.jupiter.test.util.Document;
import saros.concurrent.jupiter.test.util.JupiterTestCase;
import saros.concurrent.jupiter.test.util.PathFake;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.session.User;

/**
 * This test class represents the local execution of document changes and appropriate jupiter
 * operations.
 */
public class SimpleJupiterDocumentTest extends JupiterTestCase {

  /** simple test to generate local operations and compute the JupiterActivities for other sides. */
  @Test
  public void testExecuteLocalOperations() {
    Algorithm algo = new Jupiter(true);
    IProject project = createMock(IProject.class);
    replay(project);

    IPath path = new PathFake("dummy");

    Document doc = new Document("abc", project, path);
    assertEquals("abc", doc.getDocument());

    /* insert one char. */
    Operation op = I(2, "d");
    doc.execOperation(op);
    assertEquals("abdc", doc.getDocument());

    User user = JupiterTestCase.createUser("user");

    JupiterActivity jupiterActivity = algo.generateJupiterActivity(op, user, null);
    assertEquals(jupiterActivity.getOperation(), op);

    /* insert one short string. */
    op = I(2, "insert");
    doc.execOperation(op);
    assertEquals("abinsertdc", doc.getDocument());

    jupiterActivity = algo.generateJupiterActivity(op, user, null);
    System.out.println(jupiterActivity.getOperation().toString());
  }
}
