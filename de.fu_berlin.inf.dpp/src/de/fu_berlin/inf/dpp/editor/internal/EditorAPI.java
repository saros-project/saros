package de.fu_berlin.inf.dpp.editor.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.annotations.SelectionAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.ViewportAnnotation;
import de.fu_berlin.inf.dpp.project.ISharedProject;

/**
 * The central implementation of the IEditorAPI which basically encapsulates the interaction with the 
 * TextEditor.
 * 
 * @author rdjemili
 *
 */
public class EditorAPI implements IEditorAPI {

	private class SharedProjectPartListener implements IPartListener2 {
		public void partActivated(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(false);

			if (part != null && part instanceof IEditorPart) {
				IEditorPart editor = (IEditorPart) part;
				editorManager.partActivated(editor);
			}
		}

		public void partOpened(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(false);

			if (part != null && part instanceof IEditorPart) {
				IEditorPart editor = (IEditorPart) part;
				editorManager.partOpened(editor);
			}
		}

		public void partClosed(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(false);

			if (part != null && part instanceof IEditorPart) {
				IEditorPart editor = (IEditorPart) part;
				editorManager.partClosed(editor);
			}
		}

		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		public void partDeactivated(IWorkbenchPartReference partRef) {
		}

		public void partHidden(IWorkbenchPartReference partRef) {
		}

		public void partVisible(IWorkbenchPartReference partRef) {
		}

		public void partInputChanged(IWorkbenchPartReference partRef) {
		}
	}

	private class EditorListener implements IViewportListener, MouseListener, KeyListener,
		ISelectionChangedListener {

		private ITextViewer viewer;

		private ITextSelection lastSelection = new TextSelection(-1, -1);

		private int lastViewportTop = -1;

		private int lastViewportBottom = -1;

		public EditorListener(ITextViewer viewer) {
			this.viewer = viewer;

			viewer.getTextWidget().addMouseListener(this);
			viewer.getTextWidget().addKeyListener(this);
			viewer.getSelectionProvider().addSelectionChangedListener(this);
			viewer.addViewportListener(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.text.IViewportListener
		 */
		public void viewportChanged(int verticalOffset) {
			// TODO why doesnt this react to window resizes?
			checkViewport();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.MouseListener
		 */
		public void mouseDown(MouseEvent e) {
			checkSelection();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.MouseListener
		 */
		public void mouseUp(MouseEvent e) {
			checkSelection();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.MouseListener
		 */
		public void mouseDoubleClick(MouseEvent e) {
			// ignore
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.KeyListener
		 */
		public void keyReleased(KeyEvent e) {
			checkSelection();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.KeyListener
		 */
		public void keyPressed(KeyEvent e) {
			// ignore
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			checkSelection();
		}

		private void checkSelection() {
			ISelectionProvider sp = viewer.getSelectionProvider();
			ITextSelection selection = (ITextSelection) sp.getSelection();

			if (!lastSelection.equals(selection)) {
				editorManager.selectionChanged(selection);
				lastSelection = selection;
			}
		}

		private void checkViewport() {
			int top = viewer.getTopIndex();
			int bottom = viewer.getBottomIndex();

			if (top != lastViewportTop || bottom != lastViewportBottom) {
				lastViewportTop = top;
				lastViewportBottom = bottom;

				editorManager.viewportChanged(viewer.getTopIndex(), viewer.getBottomIndex());
			}
		}
	}

	private static Logger log = Logger.getLogger(EditorAPI.class.getName());

	private VerifyKeyListener keyVerifier = new VerifyKeyListener() {
		public void verifyKey(VerifyEvent event) {
			System.out.println(((int)event.character) + " - " + event.keyCode + " - " + event.stateMask);
			if (event.character > 0) {
				event.doit = false;

				MessageDialog.openInformation(Display.getDefault().getActiveShell(),
					"You're not the driver",
					"You're not allowed to change the text while not being "
						+ "the driver of the session.");
			}
		}
	};

	private EditorManager editorManager;

	/** Editors where the user isn't allowed to write */
	private List<IEditorPart> lockedEditors = new ArrayList<IEditorPart>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.editor.internal.IEditorAPI
	 */
	public void setEditorManager(EditorManager editorManager) {
		this.editorManager = editorManager;

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = getActiveWindow();
				window.getPartService().addPartListener(new SharedProjectPartListener());
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.editor.internal.IEditorAPI
	 */
	public IEditorPart openEditor(IFile file) {
		IWorkbenchWindow window = getActiveWindow();
		if (window != null) {
			try {
				IWorkbenchPage page = window.getActivePage();
				return IDE.openEditor(page, file);

			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.editor.internal.IEditorAPI
	 */
	public void closeEditor(IEditorPart part) {
		IWorkbenchWindow window = getActiveWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			page.closeEditor(part, false);
		}
	}

	@SuppressWarnings("restriction")
	public IDocumentProvider getDocumentProvider(IEditorInput input) {
		Object adapter = input.getAdapter(IFile.class);
		if (adapter != null) {
			IFile file = (IFile) adapter;

			String fileExtension = file.getFileExtension();
			if (fileExtension != null && fileExtension.equals("java")) {
				JavaPlugin javaPlugin = JavaPlugin.getDefault();
				return javaPlugin.getCompilationUnitDocumentProvider();
			}
		}

		DocumentProviderRegistry registry = DocumentProviderRegistry.getDefault();
		return registry.getDocumentProvider(input);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.editor.internal.IEditorAPI
	 */
	public Set<IEditorPart> getOpenEditors() {
		Set<IEditorPart> editorParts = new HashSet<IEditorPart>();

		IWorkbenchWindow[] windows = getWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();

			for (int j = 0; j < pages.length; j++) {
				IEditorReference[] editorRefs = pages[j].getEditorReferences();

				for (int k = 0; k < editorRefs.length; k++) {
					IEditorReference reference = editorRefs[k];
					IEditorPart editorPart = reference.getEditor(true);

					if (editorPart != null)
						editorParts.add(editorPart);
				}
			}
		}

		return editorParts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.editor.internal.IEditorAPI
	 */
	public IEditorPart getActiveEditor() {
		IWorkbenchWindow window = getActiveWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				return page.getActiveEditor();
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.editor.internal.IEditorAPI
	 */
	public IResource getEditorResource(IEditorPart editorPart) {
		IEditorInput input = editorPart.getEditorInput();

		if (input instanceof IPathEditorInput) {
			IResource resource = (IResource) input.getAdapter(IFile.class);

			if (resource == null) {
				resource = (IResource) input.getAdapter(IResource.class);
			}

			return resource;
		}

		return null;
	}

	/*
	 * This implementation does not really set the selection but rather adds an annotation.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.editor.internal.IEditorAPI
	 */
	public void setSelection(IEditorPart editorPart, ITextSelection selection) {

		if (!(editorPart instanceof ITextEditor))
			return;

		ITextEditor textEditor = (ITextEditor) editorPart;

		IAnnotationModel model = textEditor.getDocumentProvider().getAnnotationModel(
			textEditor.getEditorInput());

		if (model != null) {

			for (Iterator it = model.getAnnotationIterator(); it.hasNext();) {
				Annotation annotation = (Annotation) it.next();

				if (annotation.getType().equals(SelectionAnnotation.TYPE))
					model.removeAnnotation(annotation);
			}

			Position position = new Position(selection.getOffset(), selection.getLength());
			Annotation annotation = new SelectionAnnotation();
			model.addAnnotation(annotation, position);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.editor.internal.IEditorAPI#getSelection
	 */
	public ITextSelection getSelection(IEditorPart editorPart) {
		if (!(editorPart instanceof ITextEditor))
			return null;

		ITextEditor textEditor = (ITextEditor) editorPart;
		ISelectionProvider selectionProvider = textEditor.getSelectionProvider();
		if (selectionProvider != null)
			return (ITextSelection) selectionProvider.getSelection();

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.editor.internal.IEditorAPI
	 */
	public void setEditable(final IEditorPart editorPart, final boolean editable) {
		log.fine(editorPart + " set to editable:" + editable);

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				updateStatusLine(editorPart, editable);

				ITextViewerExtension textViewer = (ITextViewerExtension) getViewer(editorPart);

				if (textViewer == null)
					return;

				if (editable && lockedEditors.contains(editorPart)) {
					lockedEditors.remove(editorPart);
					textViewer.removeVerifyKeyListener(keyVerifier);

				} else if (!editable && !lockedEditors.contains(editorPart)) {
					lockedEditors.add(editorPart);
					textViewer.appendVerifyKeyListener(keyVerifier);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.editor.internal.IEditorAPI
	 */
	public void addSharedEditorListener(IEditorPart editorPart) {
		new EditorListener(getViewer(editorPart)); // HACK
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.editor.internal.IEditorAPI
	 */
	public IDocument getDocument(IEditorPart editorPart) {
		AbstractTextEditor textEditor = (AbstractTextEditor) editorPart;
		IEditorInput input = textEditor.getEditorInput();

		return textEditor.getDocumentProvider().getDocument(input);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.editor.internal.IEditorAPI
	 */
	public void setViewport(IEditorPart editorPart, boolean jumpTo, int top, int bottom, String text) {

		ITextViewer viewer = getViewer(editorPart);
		updateViewportAnnotation(viewer, top, bottom, text);

		if (jumpTo)
			viewer.setTopIndex(top);
	}

	public ILineRange getViewport(IEditorPart editorPart) {
		ITextViewer viewer = getViewer(editorPart);

		int top = viewer.getTopIndex();
		int bottom = viewer.getBottomIndex();

		return new LineRange(top, bottom - top);
	}

	private void updateViewportAnnotation(ITextViewer viewer, int top, int bottom, String text) {

		if (!(viewer instanceof ISourceViewer))
			return;

		ISourceViewer sourceViewer = (ISourceViewer) viewer;
		IAnnotationModel model = sourceViewer.getAnnotationModel();

		try {
			IDocument document = viewer.getDocument();
			for (Iterator it = model.getAnnotationIterator(); it.hasNext();) {
				Annotation ann = (Annotation) it.next();
				if (ann.getType().equals(ViewportAnnotation.TYPE))
					model.removeAnnotation(ann);
			}

			int start = document.getLineOffset(top);
			int end = document.getLineOffset(bottom);

			Annotation annotation = new ViewportAnnotation(text);
			Position position = new Position(start, end - start);
			model.addAnnotation(annotation, position);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Needs UI-thread.
	 */
	private void updateStatusLine(IEditorPart editorPart, boolean editable) {
		Object adapter = editorPart.getAdapter(IEditorStatusLine.class);
		if (adapter != null) {
			IEditorStatusLine statusLine = (IEditorStatusLine) adapter;
			statusLine.setMessage(false, editable ? "" : "Not editable", null);
		}
	}

	private static ITextViewer getViewer(IEditorPart editorPart) {
		return (ITextViewer) editorPart.getAdapter(ITextOperationTarget.class);
	}

	/**
	 * Returns the active workbench window. Needs to be called from UI thread.
	 * 
	 * @return the active workbench window or <code>null</code> if there is no
	 *         window or method is called from non-UI thread.
	 * @see IWorkbench#getActiveWorkbenchWindow()
	 */
	private static IWorkbenchWindow getActiveWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	private static IWorkbenchWindow[] getWindows() {
		return PlatformUI.getWorkbench().getWorkbenchWindows();
	}

	private void makeAllProjectResourcesReadOnly(ISharedProject sharedProject) {
		try {
			ResourceAttributes attributes = new ResourceAttributes();
			attributes.setReadOnly(!sharedProject.isDriver());
			attributes.setArchive(!sharedProject.isDriver());

			IResource[] resources = sharedProject.getProject().members();
			for (int i = 0; i < resources.length; i++) {
				if (resources[i] instanceof IFile) {
					IFile file = (IFile) resources[i];

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
