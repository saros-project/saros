package de.fu_berlin.inf.dpp.test.jupiter.text;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;

/**
 * this class represent a document object for testing.
 * 
 * @author troll
 * 
 */
public class Document {

    /** document state. */
    private StringBuffer doc;

    /**
     * constructor to init doc.
     * 
     * @param initState
     *            start document state.
     */
    public Document(String initState) {
	doc = new StringBuffer(initState);
    }

    /**
     * return string representation of current doc state.
     * 
     * @return string of current doc state.
     */
    public String getDocument() {
	return doc.toString();
    }

    /**
     * Execute Operation on document state.
     * 
     * @param op
     */
    public void execOperation(Operation op) {
	try {
	    /* execute insert operation */
	    if (op instanceof InsertOperation) {
		InsertOperation iop = (InsertOperation) op;
		doc.insert(iop.getPosition(), iop.getText());
		return;
	    }
	    /* execute delete operation */
	    if (op instanceof DeleteOperation) {
		DeleteOperation dop = (DeleteOperation) op;
		doc.delete(dop.getPosition(), dop.getPosition()
			+ dop.getTextLength());
		return;
	    }
	    /* execute split operations. */
	    if (op instanceof SplitOperation) {
		SplitOperation sop = (SplitOperation) op;
		execOperation(sop.getSecond());
		execOperation(sop.getFirst());
	    }
	} catch (Exception e) {
	    System.out.println(e.getMessage() + " : " + " doc "
		    + doc.toString() + " | op : " + op.toString());
	}
    }
}
