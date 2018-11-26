package de.fu_berlin.inf.dpp.concurrent.jupiter.test.puzzles;

/**
 * This test class represent local execution of document changes and appropriate jupiter operations.
 */
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.activities.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.Jupiter;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.Document;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.JupiterTestCase;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.PathFake;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.session.User;
import org.junit.Test;

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
    Operation op = new InsertOperation(2, "d");
    doc.execOperation(op);
    assertEquals("abdc", doc.getDocument());

    User user = JupiterTestCase.createUser("user");

    JupiterActivity jupiterActivity = algo.generateJupiterActivity(op, user, null);
    assertTrue(jupiterActivity.getOperation().equals(op));

    /* insert one short string. */
    op = new InsertOperation(2, "insert");
    doc.execOperation(op);
    assertEquals("abinsertdc", doc.getDocument());

    jupiterActivity = algo.generateJupiterActivity(op, user, null);
    System.out.println(jupiterActivity.getOperation().toString());
  }
}
