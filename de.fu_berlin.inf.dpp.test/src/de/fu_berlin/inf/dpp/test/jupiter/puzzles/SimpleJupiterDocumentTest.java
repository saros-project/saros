package de.fu_berlin.inf.dpp.test.jupiter.puzzles;

/**
 * This test class represent local execution of document changes and 
 * appropriate jupiter operations.
 */
import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.Jupiter;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.test.jupiter.text.Document;
import de.fu_berlin.inf.dpp.test.jupiter.text.JupiterTestCase;

public class SimpleJupiterDocumentTest extends JupiterTestCase {

    /**
     * simple test to generate local operations and compute the requests for
     * other sides.
     */
    public void testExecuteLocalOperations() {
	Algorithm algo = new Jupiter(true);

	Document doc = new Document("abc");
	assertEquals("abc", doc.getDocument());

	/* insert one char. */
	Operation op = new InsertOperation(2, "d");
	doc.execOperation(op);
	assertEquals("abdc", doc.getDocument());

	Request req = algo.generateRequest(op);
	assertTrue(req.getOperation().equals(op));

	/* insert one short string. */
	op = new InsertOperation(2, "insert");
	doc.execOperation(op);
	assertEquals("abinsertdc", doc.getDocument());

	req = algo.generateRequest(op);
	System.out.println(req.getOperation().toString());

    }

}
