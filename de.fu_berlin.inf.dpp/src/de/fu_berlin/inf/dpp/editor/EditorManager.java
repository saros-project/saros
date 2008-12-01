/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
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
package de.fu_berlin.inf.dpp.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.manipulation.ConvertLineDelimitersOperation;
import org.eclipse.core.filebuffers.manipulation.FileBufferOperationRunner;
import org.eclipse.core.filebuffers.manipulation.TextFileBufferOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity.Type;
import de.fu_berlin.inf.dpp.editor.annotations.AnnotationSaros;
import de.fu_berlin.inf.dpp.editor.annotations.ContributionAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.SelectionAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.ViewportAnnotation;
import de.fu_berlin.inf.dpp.editor.internal.ContributionHelper;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.editor.internal.IEditorAPI;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;

/**
 * The EditorManager is responsible for handling all editors in a DPP-session.
 * This includes the functionality of listening for user inputs in an editor,
 * locking the editors of the observer.
 * 
 * The EditorManager contains the testable logic. All untestable logic should
 * only appear in an class of the {@link IEditorAPI} type.
 * 
 * @author rdjemili
 */
public class EditorManager implements IActivityProvider, ISharedProjectListener {

    private class ElementStateListener implements IElementStateListener {
	public void elementDirtyStateChanged(Object element, boolean isDirty) {
	    if (!EditorManager.this.isDriver || isDirty
		    || !(element instanceof FileEditorInput)) {
		return;
	    }

	    FileEditorInput fileEditorInput = (FileEditorInput) element;
	    IFile file = fileEditorInput.getFile();

	    if (file.getProject() != EditorManager.this.sharedProject
		    .getProject()) {
		return;
	    }

	    IPath path = file.getProjectRelativePath();
	    saveText(path, false);
	}

	public void elementContentAboutToBeReplaced(Object element) {
	    // ignore
	}

	public void elementContentReplaced(Object element) {
	    // ignore
	}

	public void elementDeleted(Object element) {
	    // ignore
	}

	public void elementMoved(Object originalElement, Object movedElement) {
	    // ignore
	}
    }

    /**
     * @author rdjemili
     * 
     */
    private class EditorPool {
	private final Map<IPath, HashSet<IEditorPart>> editorParts = new HashMap<IPath, HashSet<IEditorPart>>();

	public void add(IEditorPart editorPart) {
	    IResource resource = EditorManager.this.editorAPI
		    .getEditorResource(editorPart);
	    IPath path = resource.getProjectRelativePath();

	    if (path == null) {
		return;
	    }

	    HashSet<IEditorPart> editors = this.editorParts.get(path);

	    EditorManager.this.editorAPI.addSharedEditorListener(editorPart);
	    EditorManager.this.editorAPI.setEditable(editorPart,
		    EditorManager.this.isDriver);

	    IDocumentProvider documentProvider = EditorManager.this.editorAPI
		    .getDocumentProvider(editorPart.getEditorInput());

	    documentProvider
		    .addElementStateListener(EditorManager.this.elementStateListener);

	    IDocument document = EditorManager.this.editorAPI
		    .getDocument(editorPart);

	    if (editors == null) {
		editors = new HashSet<IEditorPart>();
		this.editorParts.put(path, editors);
	    }

	    // if line delimiters are not in unix style convert them
	    if (document instanceof IDocumentExtension4) {
		if (!((IDocumentExtension4) document).getDefaultLineDelimiter()
			.equals("\n")) {
		    convertLineDelimiters(editorPart);
		}
		((IDocumentExtension4) document).setInitialLineDelimiter("\n");
	    } else {
		EditorManager.log
			.error("Can't discover line delimiter of document");
	    }
	    document.addDocumentListener(EditorManager.this.documentListener);
	    editors.add(editorPart);
	}

	private void convertLineDelimiters(IEditorPart editorPart) {

	    EditorManager.log.debug("Converting line delimiters...");

	    // get path of file
	    IFile file = ((FileEditorInput) editorPart.getEditorInput())
		    .getFile();
	    IPath[] paths = new IPath[1];
	    paths[0] = file.getFullPath();

	    ITextFileBufferManager buffManager = FileBuffers
		    .getTextFileBufferManager();

	    // convert operation to change line delimiters
	    TextFileBufferOperation convertOperation = new ConvertLineDelimitersOperation(
		    "\n");

	    // operation runner for the convert operation
	    FileBufferOperationRunner runner = new FileBufferOperationRunner(
		    buffManager, null);

	    // execute convert operation in runner
	    try {
		runner.execute(paths, convertOperation,
			new NullProgressMonitor());
	    } catch (OperationCanceledException e) {
		EditorManager.log.error("Can't convert line delimiters! "
			+ e.getMessage());
	    } catch (CoreException e) {
		EditorManager.log.error("Can't convert line delimiters!"
			+ e.getMessage());
	    }
	}

	public void remove(IEditorPart editorPart) {
	    IResource resource = EditorManager.this.editorAPI
		    .getEditorResource(editorPart);
	    IPath path = resource.getProjectRelativePath();

	    if (path == null) {
		return;
	    }

	    HashSet<IEditorPart> editors = this.editorParts.get(path);
	    editors.remove(editorPart);
	}

	public Set<IEditorPart> getEditors(IPath path) {
	    HashSet<IEditorPart> set = this.editorParts.get(path);
	    return set == null ? new HashSet<IEditorPart>() : set; // HACK
	}

	public Set<IEditorPart> getAllEditors() {
	    Set<IEditorPart> all = new HashSet<IEditorPart>();

	    for (Set<IEditorPart> parts : this.editorParts.values()) {
		for (IEditorPart part : parts) {
		    all.add(part);
		}
	    }

	    return all;
	}
    }

    private class DocumentListener implements IDocumentListener {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IDocumentListener
	 */
	public void documentAboutToBeChanged(final DocumentEvent event) {
	    String text = event.getText() == null ? "" : event.getText();
	    textAboutToBeChanged(event.getOffset(), text, event.getLength(),
		    event.getDocument());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IDocumentListener
	 */
	public void documentChanged(final DocumentEvent event) {
	}
    }

    private static Logger log = Logger.getLogger(EditorManager.class.getName());

    private static EditorManager instance;

    private IEditorAPI editorAPI;

    private ISharedProject sharedProject;

    private final List<IActivityListener> activityListeners = new LinkedList<IActivityListener>();

    private boolean isFollowing;

    private boolean isDriver;

    private final EditorPool editorPool = new EditorPool();

    private final ElementStateListener elementStateListener = new ElementStateListener();

    private final DocumentListener documentListener = new DocumentListener();

    private IPath activeDriverEditor;

    private final Set<IPath> driverEditors = new HashSet<IPath>();

    private ITextSelection driverTextSelection;

    /** all files that have connected document providers */
    private final Set<IFile> connectedFiles = new HashSet<IFile>();

    private final List<ISharedEditorListener> editorListeners = new ArrayList<ISharedEditorListener>();

    /* this activity has arrived and will be execute now. */
    private IActivity currentExecuteActivity;

    public static EditorManager getDefault() {
	if (EditorManager.instance == null) {
	    EditorManager.instance = new EditorManager();
	}

	return EditorManager.instance;
    }

    public void setEditorAPI(IEditorAPI editorAPI) {
	this.editorAPI = editorAPI;
	editorAPI.setEditorManager(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void sessionStarted(ISharedProject session) {
	this.sharedProject = session;
	this.isDriver = this.sharedProject.isDriver();
	this.sharedProject.addListener(this);
	this.sharedProject.getActivityManager().addProvider(this);

	activateOpenEditors();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void sessionEnded(ISharedProject session) {
	setAllEditorsToEditable();
	removeAllAnnotations(null, null);

	this.sharedProject.removeListener(this);
	this.sharedProject.getActivityManager().removeProvider(this);
	this.sharedProject = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void invitationReceived(IIncomingInvitationProcess invitation) {
	// ignore
    }

    public void addSharedEditorListener(ISharedEditorListener editorListener) {
	if (!this.editorListeners.contains(editorListener)) {
	    this.editorListeners.add(editorListener);
	}
    }

    public void removeSharedEditorListener(ISharedEditorListener editorListener) {
	this.editorListeners.remove(editorListener);
    }

    /**
     * @return the path to the resource that the driver is currently editting.
     *         Can be <code>null</code>.
     */
    public IPath getActiveDriverEditor() {
	return this.activeDriverEditor;
    }

    /**
     * Returns the resource paths of editors that the driver is currently using.
     * 
     * @return all paths (in project-relative format) of files that the driver
     *         is currently editing by using an editor. Never returns
     *         <code>null</code>. A empty set is returned if there are no
     *         currently opened editors.
     */
    public Set<IPath> getDriverEditors() {
	return this.driverEditors;
    }

    /**
     * Return the document of the given path.
     * 
     * @param path
     *            the path of the wanted document
     * @return the document or null if no document exists with given path or no
     *         editor with this file is open
     */
    public IDocument getDocument(IPath path) {
	Set<IEditorPart> editors = getEditors(path);
	if (editors.isEmpty())
	    return null;
	AbstractTextEditor editor = (AbstractTextEditor) editors.toArray()[0];
	IEditorInput input = editor.getEditorInput();
	return editor.getDocumentProvider().getDocument(input);
    }

    // TODO CJ: find a better solution
    public IPath getPathOfDocument(IDocument doc) {
	IPath path = null;
	Set<IEditorPart> editors = editorPool.getAllEditors();
	for (IEditorPart editor : editors) {
	    if (editorAPI.getDocument(editor) == doc) {
		path = editorAPI.getEditorResource(editor)
			.getProjectRelativePath();
		break;
	    }
	}
	return path;
    }

    /**
     * @return the text selection that the driver is currently using.
     */
    public ITextSelection getDriverTextSelection() {
	return this.driverTextSelection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.editor.ISharedEditorListener
     */
    public void viewportChanged(int top, int bottom, IPath editor) {
	if (!this.sharedProject.isHost()) {
	    return;
	}

	fireActivity(new ViewportActivity(top, bottom, editor));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.editor.ISharedEditorListener
     */
    public void selectionChanged(ITextSelection selection, ISelectionProvider sp) {

	IDocument doc = ((ITextViewer) sp).getDocument();

	int offset = selection.getOffset();
	int length = selection.getLength();
	IPath path = getPathOfDocument(doc);

	if (path == null) {
	    log.error("Couldn't get editor!");
	} else
	    fireActivity(new TextSelectionActivity(offset, length, path));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.editor.ISharedEditorListener
     */
    public void textAboutToBeChanged(int offset, String text, int replace,
	    IDocument document) {
	if (!this.isDriver) {
	    this.currentExecuteActivity = null;
	    return;
	}

	IEditorPart changedEditor = null;
	IPath path = null;

	// search editor which changed
	Set<IEditorPart> editors = editorPool.getAllEditors();
	for (IEditorPart editor : editors) {
	    if (editorAPI.getDocument(editor) == document) {
		changedEditor = editor;
		break;
	    }
	}

	path = editorAPI.getEditorResource(changedEditor)
		.getProjectRelativePath();

	if (path != null) {
	    TextEditActivity activity = new TextEditActivity(offset, text,
		    replace, path);
	    /*
	     * check if text edit activity is executed by other driver activity
	     * recently.
	     */
	    if (activity.sameLike(this.currentExecuteActivity)) {
		this.currentExecuteActivity = null;
		return;
	    }

	    fireActivity(activity);

	    IEditorInput input = changedEditor.getEditorInput();
	    IDocumentProvider provider = this.editorAPI
		    .getDocumentProvider(input);
	    IAnnotationModel model = provider.getAnnotationModel(input);

	    ContributionHelper.splitAnnotation(model, offset);
	} else {
	    log.error("Can't get editor path");
	}
    }

    /* ---------- ISharedProjectListener --------- */

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void driverChanged(JID driver, boolean replicated) {
	this.isDriver = this.sharedProject.isDriver();
	activateOpenEditors();

	removeAllAnnotations(null, ContributionAnnotation.TYPE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void userJoined(JID user) {
	// ignore
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void userLeft(JID user) {
	removeAllAnnotations(user.toString(), null);
    }

    /* ---------- etc --------- */

    /**
     * Opens the editor that is currently used by the driver. This method needs
     * to be called from an UI thread. Is ignored if caller is already driver.
     */
    public void openDriverEditor() {
	if (this.isDriver) {
	    return;
	}

	IPath path = getActiveDriverEditor();
	if (path == null) {
	    return;
	}

	this.editorAPI
		.openEditor(this.sharedProject.getProject().getFile(path));
    }

    public void setEnableFollowing(boolean enable) {
	this.isFollowing = enable;

	for (ISharedEditorListener editorListener : this.editorListeners) {
	    editorListener.followModeChanged(enable);
	}

	openDriverEditor();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void addActivityListener(IActivityListener listener) {
	this.activityListeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void removeActivityListener(IActivityListener listener) {
	this.activityListeners.remove(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void exec(final IActivity activity) {

	if (activity instanceof EditorActivity) {
	    EditorActivity editorActivity = (EditorActivity) activity;

	    if (editorActivity.getType().equals(Type.Activated)) {
		setActiveDriverEditor(editorActivity.getPath(), true);

	    } else if (editorActivity.getType().equals(Type.Closed)) {
		removeDriverEditor(editorActivity.getPath(), true);

	    } else if (editorActivity.getType().equals(Type.Saved)) {
		saveText(editorActivity.getPath(), true);
	    }
	}

	if (activity instanceof TextEditActivity) {
	    execTextEdit((TextEditActivity) activity);
	} else if (activity instanceof TextSelectionActivity) {
	    execTextSelection((TextSelectionActivity) activity);
	} else if (activity instanceof ViewportActivity) {
	    execViewport((ViewportActivity) activity);
	}
    }

    private void execTextEdit(TextEditActivity textEdit) {

	IPath path = textEdit.getEditor();
	IFile file = EditorManager.this.sharedProject.getProject()
		.getFile(path);

	/* set current execute activity to avoid cirle executions. */
	EditorManager.this.currentExecuteActivity = textEdit;

	replaceText(file, textEdit.offset, textEdit.replace, textEdit.text,
		textEdit.getSource());

	Set<IEditorPart> editors = EditorManager.this.editorPool
		.getEditors(path);
	for (IEditorPart editorPart : editors) {
	    EditorManager.this.editorAPI.setSelection(editorPart,
		    new TextSelection(textEdit.offset + textEdit.text.length(),
			    0), textEdit.getSource());
	}
    }

    private void execTextSelection(TextSelectionActivity cursor) {
	IPath path = cursor.getEditor();
	TextSelection textSelection = new TextSelection(cursor.getOffset(),
		cursor.getLength());

	setDriverTextSelection(textSelection);

	if (path == null) {
	    EditorManager.log
		    .error("Received text selection but have no driver editor");
	    return;
	}

	Set<IEditorPart> editors = EditorManager.this.editorPool
		.getEditors(path);
	for (IEditorPart editorPart : editors) {
	    EditorManager.this.editorAPI.setSelection(editorPart,
		    textSelection, cursor.getSource());
	}
    }

    private void execViewport(ViewportActivity viewport) {

	int top = viewport.getTopIndex();
	int bottom = viewport.getBottomIndex();
	IPath path = viewport.getEditor();

	Set<IEditorPart> editors = EditorManager.this.editorPool
		.getEditors(path);
	for (IEditorPart editorPart : editors) {
	    EditorManager.this.editorAPI.setViewport(editorPart,
		    EditorManager.this.isFollowing, top, bottom,
		    EditorManager.this.sharedProject.getDriver().getJid()
			    .toString());
	}
    }

    // TODO unify partActivated and partOpened
    public void partOpened(IEditorPart editorPart) {
	if (!isSharedEditor(editorPart)) {
	    return;
	}

	this.editorPool.add(editorPart);
	sharedEditorActivated(editorPart); // HACK
    }

    public void partActivated(IEditorPart editorPart) {
	if (!isSharedEditor(editorPart)) {
	    return;
	}

	sharedEditorActivated(editorPart);
    }

    public void partClosed(IEditorPart editorPart) {
	if (!isSharedEditor(editorPart)) {
	    return;
	}

	IResource resource = this.editorAPI.getEditorResource(editorPart);
	IPath path = resource.getProjectRelativePath();

	this.editorPool.remove(editorPart);

	if (this.isDriver) {
	    removeDriverEditor(path, false);
	}
    }

    /**
     * Checks wether given resource is currently opened.
     * 
     * @param path
     *            the project-relative path to the resource.
     * @return <code>true</code> if the given resource is opened accoring to the
     *         editor pool.
     */
    public boolean isOpened(IPath path) {
	return this.editorPool.getEditors(path).size() > 0;
    }

    /**
     * Gives the editors of given path.
     * 
     * @param path
     *            the project-relative path to the resource.
     * @return the set of editors
     */
    public Set<IEditorPart> getEditors(IPath path) {
	return this.editorPool.getEditors(path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.IActivityProvider
     */
    public IActivity fromXML(XmlPullParser parser) {

	try {
	    if (parser.getName().equals("editor")) {
		return parseEditorActivity(parser);

	    } else if (parser.getName().equals("edit")) {
		return parseTextEditActivity(parser);

	    } else if (parser.getName().equals("textSelection")) {
		return parseTextSelection(parser);

	    } else if (parser.getName().equals("viewport")) {
		return parseViewport(parser);
	    }

	} catch (XmlPullParserException e) {
	    EditorManager.log.error("Couldn't parse message");
	} catch (IOException e) {
	    EditorManager.log.error("Couldn't parse message");
	}

	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.IActivityProvider
     */
    public String toXML(IActivity activity) {
	if (activity instanceof EditorActivity) {
	    EditorActivity editorActivity = (EditorActivity) activity;
	    // return "<editor " + "path=\"" + editorActivity.getPath() + "\" "
	    // + "type=\""
	    // + editorActivity.getType() + "\" />";
	    return "<editor " + "path=\"" + editorActivity.getPath() + "\" "
		    + "type=\"" + editorActivity.getType() + "\" "
		    + "checksum=\"" + editorActivity.getChecksum() + "\"  />";

	} else if (activity instanceof TextEditActivity) {
	    TextEditActivity textEditActivity = (TextEditActivity) activity;
	    return "<edit " + "path=\"" + textEditActivity.getEditor() + "\" "
		    + "offset=\"" + textEditActivity.offset + "\" "
		    + "replace=\"" + textEditActivity.replace + "\">"
		    + "<![CDATA[" + textEditActivity.text + "]]>" + "</edit>";

	} else if (activity instanceof TextSelectionActivity) {
	    TextSelectionActivity textSelection = (TextSelectionActivity) activity;
	    return "<textSelection " + "offset=\"" + textSelection.getOffset()
		    + "\" " + "length=\"" + textSelection.getLength() + "\" "
		    + "editor=\""
		    + textSelection.getEditor().toPortableString() + "\" />";

	} else if (activity instanceof ViewportActivity) {
	    ViewportActivity viewportActvity = (ViewportActivity) activity;
	    return "<viewport " + "top=\"" + viewportActvity.getTopIndex()
		    + "\" " + "bottom=\"" + viewportActvity.getBottomIndex()
		    + "\" " + "editor=\""
		    + viewportActvity.getEditor().toPortableString() + "\" />";
	}

	return null;
    }

    private IActivity parseTextEditActivity(XmlPullParser parser)
	    throws XmlPullParserException, IOException {

	// extract current editor for text edit.
	String pathString = parser.getAttributeValue(null, "path");
	Path path = pathString.equals("null") ? null : new Path(pathString);

	int offset = Integer.parseInt(parser.getAttributeValue(null, "offset"));
	int replace = Integer.parseInt(parser
		.getAttributeValue(null, "replace"));

	String text = "";
	if (parser.next() == XmlPullParser.TEXT) {
	    text = parser.getText();
	}

	return new TextEditActivity(offset, text, replace, path);
    }

    private IActivity parseEditorActivity(XmlPullParser parser) {
	String pathString = parser.getAttributeValue(null, "path");
	String checksumString = parser.getAttributeValue(null, "checksum");

	// TODO handle cases where the file is really named "null"
	Path path = pathString.equals("null") ? null : new Path(pathString);

	Type type = EditorActivity.Type.valueOf(parser.getAttributeValue(null,
		"type"));
	EditorActivity edit = new EditorActivity(type, path);
	try {
	    long checksum = Long.parseLong(checksumString);
	    edit.setChecksum(checksum);
	} catch (Exception e) {
	    /* exception during parse process */
	}

	return edit;
    }

    private TextSelectionActivity parseTextSelection(XmlPullParser parser) {
	// TODO extract constants
	int offset = Integer.parseInt(parser.getAttributeValue(null, "offset"));
	int length = Integer.parseInt(parser.getAttributeValue(null, "length"));
	String path = parser.getAttributeValue(null, "editor");
	return new TextSelectionActivity(offset, length, Path
		.fromPortableString(path));
    }

    private ViewportActivity parseViewport(XmlPullParser parser) {
	int top = Integer.parseInt(parser.getAttributeValue(null, "top"));
	int bottom = Integer.parseInt(parser.getAttributeValue(null, "bottom"));
	String path = parser.getAttributeValue(null, "editor");
	return new ViewportActivity(top, bottom, Path.fromPortableString(path));
    }

    private boolean isSharedEditor(IEditorPart editorPart) {
	IResource resource = this.editorAPI.getEditorResource(editorPart);
	return ((this.sharedProject != null) && (resource.getProject() == this.sharedProject
		.getProject()));
    }

    private void replaceText(IFile file, int offset, int replace, String text,
	    String source) {
	FileEditorInput input = new FileEditorInput(file);
	IDocumentProvider provider = this.editorAPI.getDocumentProvider(input);

	try {
	    if (!this.connectedFiles.contains(file)) {
		provider.connect(input);
		this.connectedFiles.add(file);
	    }

	    IDocument doc = provider.getDocument(input);
	    doc.replace(offset, replace, text);

	    IAnnotationModel model = provider.getAnnotationModel(input);
	    ContributionHelper.insertAnnotation(model, offset, text.length(),
		    source);

	    // Don't disconnect from provider yet, because otherwise the text
	    // changes would be lost. We only disconnect when the document is
	    // reset or saved.

	} catch (BadLocationException e) {
	    // TODO If this happens a resend of the original text should be
	    // initiated.
	    log
		    .error(
			    "Couldn't insert driver text because of bad location.",
			    e);
	} catch (CoreException e) {
	    log.error("Couldn't insert driver text.", e);
	}
    }

    /**
     * Needs to be called from a UI thread.
     */
    private void resetText(IFile file) {
	if (!file.exists()) {
	    return;
	}

	FileEditorInput input = new FileEditorInput(file);
	IDocumentProvider provider = this.editorAPI.getDocumentProvider(input);

	if (this.connectedFiles.contains(file)) {
	    provider.disconnect(input);
	    this.connectedFiles.remove(file);
	}
    }

    /**
     * Saves the driver editor.
     * 
     * @param path
     *            the path to the resource that the driver was editing.
     * @param replicated
     *            <code>false</code> if this action originates on this client.
     *            <code>false</code> if it is an replication of an action from
     *            another participant of the shared project.
     */
    private void saveText(IPath path, boolean replicated) {
	for (ISharedEditorListener listener : this.editorListeners) {
	    listener.driverEditorSaved(path, replicated);
	}

	if (replicated) {
	    IFile file = this.sharedProject.getProject().getFile(path);
	    FileEditorInput input = new FileEditorInput(file);

	    try {
		file.setReadOnly(false);
		IDocumentProvider provider = this.editorAPI
			.getDocumentProvider(input);

		// save not necessary, if we have no modified document
		if (!this.connectedFiles.contains(file)) {
		    return;
		}

		IDocument doc = provider.getDocument(input);

		IAnnotationModel model = provider.getAnnotationModel(input);
		model.connect(doc);

		provider.saveDocument(new NullProgressMonitor(), input, doc,
			true);
		EditorManager.log.debug("Saved document " + path);

		model.disconnect(doc);

		provider.disconnect(input);
		this.connectedFiles.remove(file);

	    } catch (CoreException e) {
		EditorManager.log.error("Failed to save document.", e);
	    }

	} else {
	    IActivity activity = new EditorActivity(Type.Saved, path);
	    for (IActivityListener listener : this.activityListeners) {
		listener.activityCreated(activity);
	    }
	}
    }

    /**
     * Sends given activity to all registered activity listeners.
     */
    private void fireActivity(IActivity activity) {
	for (IActivityListener listener : this.activityListeners) {
	    listener.activityCreated(activity);
	}
    }

    private void activateOpenEditors() {
	Display.getDefault().syncExec(new Runnable() {
	    public void run() {
		for (IEditorPart editorPart : EditorManager.this.editorAPI
			.getOpenEditors()) {
		    partOpened(editorPart);
		}

		IEditorPart activeEditor = EditorManager.this.editorAPI
			.getActiveEditor();
		if (activeEditor != null) {
		    sharedEditorActivated(activeEditor);
		}
	    }
	});
    }

    private void sharedEditorActivated(IEditorPart editorPart) {
	if (!this.sharedProject.isHost()) {
	    return;
	}

	IResource resource = this.editorAPI.getEditorResource(editorPart);
	IPath editorPath = resource.getProjectRelativePath();
	setActiveDriverEditor(editorPath, false);

	ITextSelection selection = this.editorAPI.getSelection(editorPart);
	setDriverTextSelection(selection);

	ILineRange viewport = this.editorAPI.getViewport(editorPart);
	int startLine = viewport.getStartLine();
	viewportChanged(startLine, startLine + viewport.getNumberOfLines(),
		editorPath);
    }

    private void setAllEditorsToEditable() {
	for (IEditorPart editor : this.editorPool.getAllEditors()) {
	    this.editorAPI.setEditable(editor, true);
	}
    }

    /**
     * Removes all contribution and viewport annotations.
     */

    private void removeAllAnnotations(String forUserID, String typeAnnotation) {

	for (IEditorPart editor : this.editorPool.getAllEditors()) {
	    IEditorInput input = editor.getEditorInput();
	    IDocumentProvider provider = this.editorAPI
		    .getDocumentProvider(input);
	    IAnnotationModel model = provider.getAnnotationModel(input);

	    if (model == null) {
		continue;
	    }

	    for (@SuppressWarnings("unchecked")
	    Iterator it = model.getAnnotationIterator(); it.hasNext();) {
		Annotation annotation = (Annotation) it.next();
		String type = annotation.getType();

		boolean isContribution = type
			.equals(ContributionAnnotation.TYPE);
		boolean isViewport = type.equals(ViewportAnnotation.TYPE);
		boolean isTextSelection = type
			.startsWith(SelectionAnnotation.TYPE);

		if (((typeAnnotation == null) && !isContribution && !isViewport && !isTextSelection)
			|| ((typeAnnotation != null) && (typeAnnotation
				.equals(type) == false))) {
		    continue;
		}

		AnnotationSaros anns = (AnnotationSaros) annotation;
		boolean isfromuser = (forUserID == null)
			|| ((forUserID != null) && anns.getSource().equals(
				forUserID));

		if (isfromuser) {
		    model.removeAnnotation(annotation);
		}
	    }
	}
    }

    private EditorManager() {
	setEditorAPI(new EditorAPI());
	if ((Saros.getDefault() != null)
		&& (Saros.getDefault().getSessionManager() != null)) {
	    Saros.getDefault().getSessionManager().addSessionListener(this);
	}
    }

    /**
     * Sets the currently active driver editor.
     * 
     * @param path
     *            the project-relative path to the resource that the editor is
     *            currently editting.
     * @param replicated
     *            <code>false</code> if this action originates on this client.
     *            <code>false</code> if it is an replication of an action from
     *            another participant of the shared project.
     */
    private void setActiveDriverEditor(IPath path, boolean replicated) {
	this.activeDriverEditor = path;
	this.driverEditors.add(path);

	for (ISharedEditorListener listener : this.editorListeners) {
	    listener.activeDriverEditorChanged(this.activeDriverEditor,
		    replicated);
	}

	if (replicated) {
	    if (this.isFollowing) {
		Display.getDefault().syncExec(new Runnable() {
		    public void run() {
			openDriverEditor();
		    }
		});
	    }

	} else {
	    IActivity activity = new EditorActivity(Type.Activated, path);
	    for (IActivityListener listener : this.activityListeners) {
		listener.activityCreated(activity);
	    }
	}
    }

    /**
     * Removes the given editor from the list of editors that the driver is
     * currently using.
     * 
     * @param path
     *            the path to the resource that the driver was editting.
     * @param replicated
     *            <code>false</code> if this action originates on this client.
     *            <code>true</code> if it is an replication of an action from
     *            another participant of the shared project.
     */
    private void removeDriverEditor(final IPath path, boolean replicated) {
	if (path.equals(this.activeDriverEditor)) {
	    setActiveDriverEditor(null, replicated);
	}

	this.driverEditors.remove(path);

	for (ISharedEditorListener listener : this.editorListeners) {
	    listener.driverEditorRemoved(path, replicated);
	}

	if (replicated) {
	    Display.getDefault().syncExec(new Runnable() {
		public void run() {
		    IFile file = EditorManager.this.sharedProject.getProject()
			    .getFile(path);
		    resetText(file);

		    if (!EditorManager.this.isFollowing) {
			return;
		    }

		    Set<IEditorPart> editors = EditorManager.this.editorPool
			    .getEditors(path);
		    for (IEditorPart part : editors) {
			EditorManager.this.editorAPI.closeEditor(part);
		    }
		}
	    });

	} else {
	    IActivity activity = new EditorActivity(Type.Closed, path);
	    for (IActivityListener listener : this.activityListeners) {
		listener.activityCreated(activity);
	    }
	}
    }

    /**
     * @param selection
     *            sets the current text selection that is used by the driver.
     */
    private void setDriverTextSelection(ITextSelection selection) {
	this.driverTextSelection = selection;
    }
}
