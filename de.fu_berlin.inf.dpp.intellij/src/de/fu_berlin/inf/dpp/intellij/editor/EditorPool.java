package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import de.fu_berlin.inf.dpp.activities.SPath;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * IntelliJ editor pool
 */
//FIXME: Document instances should not be stored.
// (see http://confluence.jetbrains.com/display/IDEADEV/IntelliJ+IDEA+Architectural+Overview#IntelliJIDEAArchitecturalOverview-Documents)
// Maybe just store editors here, load all non-opened files on the fly?
public class EditorPool {
    private Map<SPath, Editor> editors = new HashMap<SPath, Editor>();
    private Map<SPath, Document> documents = new HashMap<SPath, Document>();
    private Map<Document, SPath> files = new HashMap<Document, SPath>();

    public EditorPool() {
    }

    /**
     * Adds editor and its documents.
     *
     * @param file
     * @param editor
     */
    public void add(SPath file, Editor editor) {
        editors.put(file, editor);
        add(file, editor.getDocument());
    }

    /**
     * Adds this to files and documents.
     *
     * @param file
     * @param document
     */
    public void add(SPath file, Document document) {
        documents.put(file, document);
        files.put(document, file);
    }

    /**
     * Removes the editor for this file, all documents and the file.
     *
     * @param file
     */
    public void removeAll(SPath file) {

        removeEditor(file);

        Document doc = null;
        if (documents.containsKey(file)) {
            doc = documents.remove(file);
        }

        if (doc != null) {
            files.remove(doc);
        }
    }

    /**
     * Removes the editor.
     *
     * @param file
     */
    public void removeEditor(SPath file) {
        if (editors.containsKey(file)) {
            editors.remove(file);
        }
    }

    /**
     * Replaces all occurences of editors and documents with key oldPath with
     * newPath.
     *
     * @param oldPath
     * @param newPath
     */
    public void replaceAll(SPath oldPath, SPath newPath) {
        if (editors.containsKey(oldPath)) {
            Editor editor = editors.remove(oldPath);
            if (editor != null)
                editors.put(newPath, editor);
        }

        if (documents.containsKey(oldPath)) {
            Document doc = documents.remove(oldPath);

            if (doc != null) {
                documents.put(newPath, doc);
                files.put(doc, newPath);
            }
        }
    }

    public void unlockAllDocuments() {
        for (Document doc : documents.values()) {
            doc.setReadOnly(false);
        }
    }

    public void lockAllDocuments() {
        for (Document doc : documents.values()) {
            doc.setReadOnly(true);
        }
    }

    public Collection<Document> getDocuments() {
        return documents.values();
    }

    public Document getDocument(SPath file) {
        return documents.get(file);
    }

    public Editor getEditor(SPath file) {
        return editors.get(file);
    }

    public SPath getFile(Document doc) {
        return files.get(doc);
    }

    public Collection<Editor> getEditors() {
        return editors.values();
    }

    public Set<SPath> getFiles() {
        return documents.keySet();
    }

    /**
     * Clears all state.
     */
    public void clear() {
        documents.clear();
        editors.clear();
        files.clear();
    }

}
