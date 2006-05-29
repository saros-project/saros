/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.listeners.ISharedEditorListener;

/**
 * Listens for all relevant events in the complex {@link AbstractTextEditor}
 * and unites them into the simplified and compact {@link ISharedEditorListener}
 * interface.
 * 
 * @author rdjemili
 */
public class SharedEditor implements IActivityProvider {
    private List<ISharedEditorListener> listeners = new ArrayList<ISharedEditorListener>();
    private List<IActivityListener> activityListeners = new ArrayList<IActivityListener>();

    private ITextViewer                 viewer;
    private ITextEditor                 editor;

    private EditorListener              editorListener;
    private ReadOnlyKeyVerifier         readOnlyKeyVerifier = new ReadOnlyKeyVerifier();

    private IPath                       path;
    private boolean                     editable;

    private class EditorListener implements IViewportListener, MouseListener, 
        KeyListener, ISelectionChangedListener, IDocumentListener {

        public EditorListener() {
            viewer.getTextWidget().addMouseListener(this);
            viewer.getTextWidget().addKeyListener(this);
            viewer.getSelectionProvider().addSelectionChangedListener(this);
            viewer.addViewportListener(this);
            getDocument().addDocumentListener(this);
        }
        
        // TODO why doesnt this react to window resizes?
        /* (non-Javadoc)
         * @see org.eclipse.jface.text.IViewportListener
         */
        public void viewportChanged(int verticalOffset) {
            for (ISharedEditorListener listener : listeners) {
                listener.viewportChanged(viewer.getTopIndex(), viewer.getBottomIndex());
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.events.MouseListener
         */
        public void mouseDown(MouseEvent e) {
            fireCursorListeners();
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.events.MouseListener
         */
        public void mouseUp(MouseEvent e) {
            fireCursorListeners();
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.events.MouseListener
         */
        public void mouseDoubleClick(MouseEvent e) {
            // ignore
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.events.KeyListener
         */
        public void keyPressed(KeyEvent e) {
            // ignore
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.events.KeyListener
         */
        public void keyReleased(KeyEvent e) {
            // ignore
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ISelectionChangedListener
         */
        public void selectionChanged(SelectionChangedEvent event) {
            fireCursorListeners();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.IDocumentListener
         */
        public void documentAboutToBeChanged(DocumentEvent event) {
            for (ISharedEditorListener listener : listeners) {
                try {
                    listener.textChanged(event.getOffset(),  event.getText(), event.getLength(), 
                        event.getDocument().getLineOfOffset(event.getOffset()));
                } catch (BadLocationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.IDocumentListener
         */
        public void documentChanged(DocumentEvent event) {
            // ignore
        }
        
        private void fireCursorListeners() {
            for (ISharedEditorListener listener : listeners) {
                listener.cursorChanged(getSelection());
            }
        }

        private ITextSelection getSelection() {
            return (ITextSelection)viewer.getSelectionProvider().getSelection();
        } 
    }
    
    // CHECK merge with keylistener!?
    private class ReadOnlyKeyVerifier implements VerifyKeyListener {
        public void verifyKey(VerifyEvent event) {
            event.doit = editable;
        }
    }

    public SharedEditor(IWorkbenchPart part, IPath path) {
        if (!(part instanceof ITextEditor)) {
            throw new IllegalArgumentException("Can't attach listener to non-text editor.");
        }
        
        try {
            editor = (ITextEditor)part;
            if (part instanceof AbstractTextEditor &&
                editor.getEditorInput() instanceof IPathEditorInput) {
                
                // look ma' no hacks!
                viewer = (ITextViewer)editor.getAdapter(ITextOperationTarget.class);
        
                if (viewer != null) {
                    attachInternalListeners(viewer);
                }
                
                this.path = path;
            } 
        } catch (Exception e) {
            e.printStackTrace();
            // TODO remove listeners if one of them wasnt added succesfully
        }
    }
    
    public IPath getPath() {
        return path;
    }
    
    public void setEditable(final boolean editable) {
        this.editable = editable;
        
//        updateFileAttributes();
        updateStatusLine();
    }
    
    /**
     * Sets the selection. Needs to be called from an UI-thread.
     */
    public void setSelection(final ITextSelection selection) {
        viewer.getSelectionProvider().setSelection(selection);
    }
    
    /**
     * Sets the line selection. Needs to be called from an UI-thread.
     */
    public void setLineSelection(int startLine, int endLine) {
        try {
            // TODO handle line delimiter
            int startOffset = getDocument().getLineOffset(startLine);
            int endOffset = getDocument().getLineOffset(endLine - 1);
            int length = endOffset - startOffset;
            
            TextSelection selection = new TextSelection(startOffset, length);
            setSelection(selection);
        } catch (BadLocationException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
    
    /**
     * Sets the text. Needs to be called from an UI-thread.
     */
    public void setText(final int offset, final String text, final int replace) {
        try {
            // \r gets lost due to XML normalization. inconsistent document
            // lengths are the consequence
            getDocument().replace(offset, replace, text.replace("\n", "\r\n"));

            // use textedits also for cursor updates
            TextSelection selection = new TextSelection(offset + text.length(), 0);

            viewer.getSelectionProvider().setSelection(selection);

        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Adds the listener if isn't already registered as an listener.
     * 
     * @param listener
     */
    public void addListener(ISharedEditorListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(ISharedEditorListener listener) {
        listeners.remove(listener);
    }

    public List<ISharedEditorListener> getListeners() {
        return listeners;
    }
    
    public void exec(IActivity activity) {
        // TODO Auto-generated method stub
        
    }

    public void addActivityListener(IActivityListener listener) {
        activityListeners.add(listener);
    }

    public void removeActivityListener(IActivityListener listener) {
        activityListeners.remove(listener);
    }

    @Override
    public String toString() {
        return "SharedEditor("+path+")";
    }
    
    private IDocument getDocument() {
        return editor.getDocumentProvider().getDocument(editor.getEditorInput());
    }

    private void updateStatusLine() {
        if (!editable) {
            Display.getDefault().asyncExec(new Runnable() {
    
                public void run() {
                    Object adapter = editor.getAdapter(IEditorStatusLine.class);
                    if (adapter != null) {
                        IEditorStatusLine statusLine = (IEditorStatusLine)adapter;
                        statusLine.setMessage(false, "Not editable", null);
                    }
                }
            });
        }
    }
    
    private void attachInternalListeners(final ITextViewer viewer) {
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                editorListener = new EditorListener();
                
                ITextViewerExtension textViewer = (ITextViewerExtension)viewer;
                textViewer.appendVerifyKeyListener(readOnlyKeyVerifier);
            }
        });
    }

    private void updateFileAttributes() {
        IEditorInput input = editor.getEditorInput();
        if (input instanceof FileEditorInput) {
            FileEditorInput fileInput = (FileEditorInput)input;
            IFile file = fileInput.getFile();
            
            try {
                ResourceAttributes attributes = new ResourceAttributes();
                attributes.setReadOnly(!editable);
                file.setResourceAttributes(attributes);
            } catch (CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}