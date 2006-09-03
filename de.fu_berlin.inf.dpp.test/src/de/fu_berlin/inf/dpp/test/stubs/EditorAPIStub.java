package de.fu_berlin.inf.dpp.test.stubs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.StatusTextEditor;

import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.IEditorAPI;

public class EditorAPIStub implements IEditorAPI {
    private IEditorPart                      activeEditor;
    private Set<IEditorPart>                 openEditors = new HashSet<IEditorPart>();
    private Map<IEditorPart, ITextSelection> selections  = new HashMap<IEditorPart, ITextSelection>();
    private Map<IEditorPart, ILineRange>     viewports   = new HashMap<IEditorPart, ILineRange>();
    
    public void addSharedEditorListener(IEditorPart editorPart) {
    }

    public void closeEditor(IEditorPart part) {
        openEditors.remove(part);
    }

    public IEditorPart getActiveEditor() {
        return activeEditor;
    }

    public IDocument getDocument(IEditorPart editorPart) {
        return null;
    }

    public IDocumentProvider getDocumentProvider(IEditorInput editorInput) {
        return null;
    }

    public IResource getEditorResource(IEditorPart editorPart) {
        return new FileStub("/foo/test", "test content");
    }

    public Set<IEditorPart> getOpenEditors() {
        return openEditors;
    }

    public IEditorPart openEditor(IFile file) {
        //activeEditor = // TODO;
        
        StatusTextEditor statusTextEditor = new StatusTextEditor();
        FileEditorInput input = new FileEditorInput(file);
        statusTextEditor.setInput(input);
        openEditors.add(statusTextEditor); // HACK
        
        return activeEditor;
    }

    public void setSelection(IEditorPart editorPart, ITextSelection selection) {
        selections.put(editorPart, selection);
    }

    public ITextSelection getSelection(IEditorPart editorPart) {
        ITextSelection selection = selections.get(editorPart);
        return selection != null ? selection : TextSelection.emptySelection();
    }

    public void setViewport(IEditorPart editorPart, boolean jumpTo, int top, int bottom, String text) {
        viewports.put(editorPart, new LineRange(top, bottom - top));
    }

    public ILineRange getViewport(IEditorPart editorPart) {
        ILineRange range = viewports.get(editorPart);
        return range != null ? range : new LineRange(1, 0);
    }

    public void setEditable(IEditorPart editorPart, boolean editable) {
    }

    public void setEditorManager(EditorManager editorManager) {
    }
}
