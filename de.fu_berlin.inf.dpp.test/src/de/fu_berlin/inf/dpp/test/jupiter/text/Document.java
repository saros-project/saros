package de.fu_berlin.inf.dpp.test.jupiter.text;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;

import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;

/**
 * this class represent a document object for testing.
 * 
 * @author troll
 * @author oezbek
 */
public class Document {

	private static final Logger log = Logger
			.getLogger(Document.class.getName());
	
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
	
    	List<TextEditActivity> activities = op.toTextEdit(new Path("dummy"), "dummy");
    	
    	for (TextEditActivity activity : activities){
    		
    		int start = activity.offset;
    		int end = start + activity.replacedText.length();
    		String is = doc.toString().substring(start, end); 
    		
    		if (!is.equals(activity.replacedText)){
    			log.warn("Text should be '" + activity.replacedText + "' is '" + is + "'");
    			throw new RuntimeException("Text should be '" + activity.replacedText + "' is '" + is + "'");
    		}
    		
    		doc.replace(start, end, activity.text);
    	}
    }
}
