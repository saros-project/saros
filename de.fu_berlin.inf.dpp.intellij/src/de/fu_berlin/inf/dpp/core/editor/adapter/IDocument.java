package de.fu_berlin.inf.dpp.core.editor.adapter;

/**
 * Equivalent to the org.eclipse.jface.text.IDocument interface with only relevant
 * methods.If not specified otherwise, the methods behave like their Eclipse
 * counterparts.
 */
public interface IDocument {

    /**
     * Returns this document's complete text.
     *
     * @return the document's complete text
     */
    String get();

    int getLength();

    int getNumberOfLines();
}
