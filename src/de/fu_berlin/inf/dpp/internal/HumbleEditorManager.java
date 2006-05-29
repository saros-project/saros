package de.fu_berlin.inf.dpp.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;

import de.fu_berlin.inf.dpp.EditorManager;
import de.fu_berlin.inf.dpp.ISharedProject;

public class HumbleEditorManager implements IHumbleEditorManager {
    private class SharedProjectPartListener implements IPartListener2 {
        public void partActivated(IWorkbenchPartReference partRef) {
            IWorkbenchPart part = partRef.getPart(false);
            
            if (part != null && part instanceof IEditorPart) {
                IEditorPart editor = (IEditorPart)part;
                editorManager.partActivated(editor);
            }
        }

        public void partBroughtToTop(IWorkbenchPartReference partRef) {}
        public void partClosed(IWorkbenchPartReference partRef) {}
        public void partDeactivated(IWorkbenchPartReference partRef) {}
        public void partOpened(IWorkbenchPartReference partRef) {}
        public void partHidden(IWorkbenchPartReference partRef) {}
        public void partVisible(IWorkbenchPartReference partRef) {}
        public void partInputChanged(IWorkbenchPartReference partRef) {}
    }
    
    private class EditorListener implements IViewportListener, MouseListener, 
        KeyListener, ISelectionChangedListener, IDocumentListener {

        private ITextViewer viewer;

        public EditorListener(ITextViewer viewer) {
            this.viewer = viewer;
            
            viewer.getTextWidget().addMouseListener(this);
            viewer.getTextWidget().addKeyListener(this);
            viewer.getSelectionProvider().addSelectionChangedListener(this);
            viewer.addViewportListener(this);
            viewer.getDocument().addDocumentListener(this);
        }

        // TODO why doesnt this react to window resizes?
        /* (non-Javadoc)
         * @see org.eclipse.jface.text.IViewportListener
         */
        public void viewportChanged(int verticalOffset) {
            editorManager.viewportChanged(viewer.getTopIndex(), viewer.getBottomIndex());
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
            try {
                editorManager.textChanged(event.getOffset(), event.getText(), event.getLength(),
                    event.getDocument().getLineOfOffset(event.getOffset()));
            } catch (BadLocationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.IDocumentListener
         */
        public void documentChanged(DocumentEvent event) {
            // ignore
        }

        private void fireCursorListeners() {
            editorManager.cursorChanged(getSelection());
        }

        private ITextSelection getSelection() {
            return (ITextSelection)viewer.getSelectionProvider().getSelection();
        }
    }

    private class ReadOnlyKeyVerifier implements VerifyKeyListener {
        public void verifyKey(VerifyEvent event) {
            event.doit = false;
        }
    }
    
    private VerifyKeyListener keyVerifier = new ReadOnlyKeyVerifier();
    private EditorManager     editorManager;
    

    public void setEditorManager(EditorManager editorManager) {
        this.editorManager = editorManager;
        
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                window.getPartService().addPartListener(new SharedProjectPartListener());
            }
        });
    }

    public void openEditor(IFile file) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            try {
                IWorkbenchPage page = window.getActivePage();
                IDE.openEditor(page, file);
                
            } catch (PartInitException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void setText(IFile file, int offset, int replace, String text) {
        FileEditorInput editorInput = new FileEditorInput(file);
        
        DocumentProviderRegistry registry = DocumentProviderRegistry.getDefault();
        IDocumentProvider provider = registry.getDocumentProvider(editorInput);
    
        try {
            provider.connect(editorInput);
            IDocument doc = provider.getDocument(editorInput);
            doc.replace(offset, replace, text);
    
            file.refreshLocal(1, new NullProgressMonitor());
            provider.disconnect(editorInput);
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<IEditorPart> getOpenedEditors() {
        List<IEditorPart> editorParts = new ArrayList<IEditorPart>();
        
        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
        for (int i = 0; i < windows.length; i++) {
            IWorkbenchPage[] pages = windows[i].getPages();
            
            for (int j = 0; j < pages.length; j++) {
                IEditorReference[] editorReferences = pages[j].getEditorReferences();
                
                for (int k = 0; k < editorReferences.length; k++) {
                    IEditorReference reference = editorReferences[k];
                    IEditorPart editorPart = reference.getEditor(false);
                    
                    if (editorPart != null)
                        editorParts.add(editorPart);
                }
            }
        }
        
        return editorParts;
    }

    public IEditorPart getActiveEditor() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                return page.getActiveEditor();
            }
        }
        
        return null;
    }

    public IPath getEditorPath(IEditorPart editorPart) {
        IPathEditorInput input = (IPathEditorInput)editorPart.getEditorInput();
        IResource resource = (IResource)input.getAdapter(IFile.class);
        
        if (resource == null) {
            resource = (IResource)input.getAdapter(IResource.class);
        }
        
        return resource.getProjectRelativePath();
    }

    public void setSelection(IEditorPart editorPart, ITextSelection selection) {
        getViewer(editorPart).getSelectionProvider().setSelection(selection);
    }

    public void setEditable(final IEditorPart editorPart, final boolean editable) {
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                updateStatusLine(editorPart, editable);        
                
                ITextViewerExtension textViewer = (ITextViewerExtension)getViewer(editorPart);
                if (editable) {
                    textViewer.appendVerifyKeyListener(keyVerifier);
                } else {
                    textViewer.removeVerifyKeyListener(keyVerifier);
                }
            }
        });
    }

    public void connect(IEditorPart editorPart) {
        new EditorListener(getViewer(editorPart)); // HACK
    }
    
    /**
     * Needs UI-thread.
     */
    private void updateStatusLine(final IEditorPart editorPart, boolean editable) {
        Object adapter = editorPart.getAdapter(IEditorStatusLine.class);
        if (adapter != null) {
            IEditorStatusLine statusLine = (IEditorStatusLine)adapter;
            statusLine.setMessage(false, editable ? "" : "Not editable", null);
        }
    }

    private ITextViewer getViewer(IEditorPart editorPart) {
        return (ITextViewer)editorPart.getAdapter(ITextOperationTarget.class);
    }

    private void makeAllProjectResourcesReadOnly(ISharedProject sharedProject) {
        try {
            ResourceAttributes attributes = new ResourceAttributes();
            attributes.setReadOnly(!sharedProject.isDriver());
            attributes.setArchive(!sharedProject.isDriver());
            
            IResource[] resources = sharedProject.getProject().members();
            for (int i = 0; i < resources.length; i++) {
                if (resources[i] instanceof IFile) {
                    IFile file = (IFile)resources[i];
    
                    try {
                        file.setResourceAttributes(attributes);
                    } catch (CoreException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
}
