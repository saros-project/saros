package de.fu_berlin.inf.dpp.core.editor.adapter;

import com.intellij.openapi.editor.Document;

/**
 * Adapts the {@link Document} to {@link IDocument}.
 */
public class DocumentAdapter implements IDocument {
    private final Document doc;

    public DocumentAdapter(Document doc) {
        this.doc = doc;
    }

    @Override
    public String get() {
        return doc.getText();
    }

    @Override
    public int getLength() {
        return doc.getTextLength();
    }

    @Override
    public int getNumberOfLines() {
        return doc.getLineCount();
    }

}
