package de.fu_berlin.inf.dpp.test.jupiter.text;

/**
 * interface for testing document state and content.
 * 
 * @author troll
 * 
 */
public interface DocumentTestChecker {

    public String getDocument();

    public void addJupiterDocumentListener(JupiterDocumentListener jdl);

    public void removeJupiterDocumentListener(String id);
}
