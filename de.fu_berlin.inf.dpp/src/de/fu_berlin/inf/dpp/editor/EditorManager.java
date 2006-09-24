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
package de.fu_berlin.inf.dpp.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
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
            if (!isDriver || isDirty || !(element instanceof FileEditorInput))
                return;
            
            FileEditorInput fileEditorInput = (FileEditorInput)element;
            IFile file = fileEditorInput.getFile();
            
            if (file.getProject() != sharedProject.getProject())
                return;
            
            IPath path = file.getProjectRelativePath();
            saveDriverEditor(path, false);
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
    
    private class EditorPool {
        private Map<IPath, HashSet<IEditorPart>> editorParts = 
            new HashMap<IPath, HashSet<IEditorPart>>(); 
        
        public void add(IEditorPart editorPart) {
            IResource resource = editorAPI.getEditorResource(editorPart);
            IPath path = resource.getProjectRelativePath();
            
            if (path == null)
                return;
            
            HashSet<IEditorPart> editors = editorParts.get(path);
            
            editorAPI.addSharedEditorListener(editorPart);
            editorAPI.setEditable(editorPart, isDriver);
            
            IDocumentProvider documentProvider = 
                editorAPI.getDocumentProvider(editorPart.getEditorInput());
            
            documentProvider.addElementStateListener(elementStateListener);
            
            IDocument document = editorAPI.getDocument(editorPart);
            document.addDocumentListener(documentListener);
            
            if (editors == null) {
                editors = new HashSet<IEditorPart>();
                editorParts.put(path, editors);
            }
            
            editors.add(editorPart);
        }

        public void remove(IEditorPart editor) {
        }
        
        public Set<IEditorPart> getEditors(IPath path) {
            HashSet<IEditorPart> set = editorParts.get(path);
            return set == null ? new HashSet<IEditorPart>() : set; // HACK
        }
        
        public Set<IEditorPart> getAllEditors() {
            Set<IEditorPart> all = new HashSet<IEditorPart>();
            
            for (Set<IEditorPart> parts : editorParts.values()) {
                for (IEditorPart part : parts) {
                    all.add(part);
                }
            }
            
            return all;
        }
    }
    
    private class DocumentListener implements IDocumentListener {
        /* (non-Javadoc)
         * @see org.eclipse.jface.text.IDocumentListener
         */
        public void documentAboutToBeChanged(final DocumentEvent event) {
            // don't give NULL string
            String text = event.getText() == null ? "" : event.getText();
            textChanged(event.getOffset(), text, event.getLength());
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.text.IDocumentListener
         */
        public void documentChanged(final DocumentEvent event) {
        }
    }
    
    private static Logger log = Logger.getLogger(EditorManager.class.getName());
    
    private static EditorManager    instance;
    private IEditorAPI              editorAPI;
    private ISharedProject          sharedProject;

    private List<IActivityListener> activityListeners    = new LinkedList<IActivityListener>();

    private boolean                 isFollowing;
    private boolean                 isDriver;

    private EditorPool              editorPool           = new EditorPool();
    private ElementStateListener    elementStateListener = new ElementStateListener();
    private DocumentListener        documentListener     = new DocumentListener();

    private IPath                   activeDriverEditor;
    private Set<IPath>              driverEditors        = new HashSet<IPath>();

    private ITextSelection          driverTextSelection;
    
    private List<ISharedEditorListener> editorListeners = new ArrayList<ISharedEditorListener>();
    
    public static EditorManager getDefault() {
    	if (instance == null)
    		instance = new EditorManager();
    	
    	return instance;
    }
    
    public void setEditorAPI(IEditorAPI editorAPI) {
    	this.editorAPI = editorAPI;
    	editorAPI.setEditorManager(this);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void sessionStarted(ISharedProject session) {
    	sharedProject = session;
        isDriver = sharedProject.isDriver();
        sharedProject.addListener(this);
        sharedProject.getActivityManager().addProvider(this);
        
        activateOpenEditors();
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.ISessionListener
	 */
	public void sessionEnded(ISharedProject session) {
		setAllEditorsToEditable();
		
		sharedProject.removeListener(this);
		sharedProject.getActivityManager().removeProvider(this);
		sharedProject = null;
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.ISessionListener
	 */
	public void invitationReceived(IIncomingInvitationProcess invitation) {
		// ignore
	}

	public void addSharedEditorListener(ISharedEditorListener editorListener) {
        if (!editorListeners.contains(editorListener))
            editorListeners.add(editorListener);
    }
    
    public void removeSharedEditorListener(ISharedEditorListener editorListener) {
        editorListeners.remove(editorListener);
    }
    
    /**
     * @return the path to the resource that the driver is currently editting.
     * Can be <code>null</code>.
     */
    public IPath getActiveDriverEditor() {
        return activeDriverEditor;
    }
    
    /**
     * Returns the resource paths of editors that the driver is currently using.
     * 
     * @return all paths (in project-relative format) of files that the driver
     * is currently editing by using an editor. Never returns <code>null</code>.
     * A empty set is returned if there are no currently opened editors.
     */
    public Set<IPath> getDriverEditors() {
        return driverEditors;
    }

    /**
     * @return the text selection that the driver is currently using.
     */
    public ITextSelection getDriverTextSelection() {
        return driverTextSelection;
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.editor.ISharedEditorListener
     */
    public void viewportChanged(int top, int bottom) {
        if (!isDriver) return;
        
        fireActivity(new ViewportActivity(top, bottom));
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.editor.ISharedEditorListener
     */
    public void selectionChanged(ITextSelection selection) { 
        if (!isDriver) return;
        
        int offset = selection.getOffset();
        int length = selection.getLength();
        
        fireActivity(new TextSelectionActivity(offset, length));
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.editor.ISharedEditorListener
     */
    public void textChanged(int offset, String text, int replace) {
        if (!isDriver) return;
        
        fireActivity(new TextEditActivity(offset, text, replace));
        
        IEditorInput input = editorAPI.getActiveEditor().getEditorInput();
        IDocumentProvider provider = editorAPI.getDocumentProvider(input);
        IAnnotationModel model = provider.getAnnotationModel(input);
        
        ContributionHelper.splitAnnotation(model, offset);
    }

    /* ---------- ISharedProjectListener --------- */
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void driverChanged(JID driver, boolean replicated) {
        isDriver = sharedProject.isDriver();
        activateOpenEditors();
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void userJoined(JID user) {
        // ignore
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void userLeft(JID user) {
        // ignore
    }

    /* ---------- etc --------- */
    
    /**
     * Opens the editor that is currently used by the driver. This method needs 
     * to be called from an UI thread. Is ignored if caller is already driver.
     */
    public void openDriverEditor() {
        if (isDriver) return;
        
        IPath path = getActiveDriverEditor();
        if (path == null)
            return;
        
        editorAPI.openEditor(sharedProject.getProject().getFile(path));
    }

    public void setEnableFollowing(boolean enable) {
        isFollowing = enable;
        
        for (ISharedEditorListener editorListener : editorListeners) {
			editorListener.followModeChanged(enable);
		}
        
        openDriverEditor();
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void addActivityListener(IActivityListener listener) {
        activityListeners.add(listener);
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void removeActivityListener(IActivityListener listener) {
        activityListeners.remove(listener);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void exec(final IActivity activity) {
        if (activity instanceof EditorActivity) {
            EditorActivity editorActivity = (EditorActivity)activity;
            
            if (editorActivity.getType().equals(Type.Activated)) {
                setActiveDriverEditor(editorActivity.getPath(), true);
                
            } else if (editorActivity.getType().equals(Type.Closed)) {
                removeDriverEditor(editorActivity.getPath(), true);
                
            } else if (editorActivity.getType().equals(Type.Saved)) {
                saveDriverEditor(editorActivity.getPath(), true);
            } 
        } 
        
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                if (activity instanceof TextEditActivity)
                    execTextEdit((TextEditActivity)activity);
                    
                else if (activity instanceof TextSelectionActivity)
                    execTextSelection((TextSelectionActivity)activity);
                    
                else if (activity instanceof ViewportActivity)
                    execViewport((ViewportActivity)activity);
            }

            private void execTextEdit(TextEditActivity textEdit) {
                if (getActiveDriverEditor() == null) {
                    log.severe("Received text edit but have no driver editor");
                    return;
                }
                
                IPath driverEditor = getActiveDriverEditor();
                IFile file = sharedProject.getProject().getFile(driverEditor);
                
                String text = fixDelimiters(file, textEdit.text);
                insertDriverText(file,textEdit.offset, textEdit.replace, text);
                
                Set<IEditorPart> editors = editorPool.getEditors(driverEditor);
                for (IEditorPart editorPart : editors) {
                    editorAPI.setSelection(editorPart, 
                        new TextSelection(textEdit.offset + text.length(), 0));
                }
            }

            private void execTextSelection(TextSelectionActivity cursor) {
                IPath activeDriverEditor = getActiveDriverEditor();
                TextSelection textSelection = new TextSelection(
                    cursor.getOffset(), cursor.getLength());
                
                setDriverTextSelection(textSelection);
                
                if (activeDriverEditor == null) {
                    log.severe("Received text selection but have no driver editor");
                    return;
                }
                
                Set<IEditorPart> editors = editorPool.getEditors(activeDriverEditor);
                for (IEditorPart editorPart : editors) {
                    editorAPI.setSelection(editorPart, textSelection);
                }
            }
            
            private void execViewport(ViewportActivity viewport) {
                if (getActiveDriverEditor() == null) {
                    log.severe("Received viewport but have no driver editor");
                    return;
                }
                
                int top = viewport.getTopIndex();
                int bottom = viewport.getBottomIndex();
                String user = sharedProject.getDriver().toString();
                String text = "What "+user+" is seeing";
                
                IPath driverEditor = getActiveDriverEditor();
                Set<IEditorPart> editors = editorPool.getEditors(driverEditor);
                for (IEditorPart editorPart : editors) {
                    editorAPI.setViewport(editorPart,isFollowing, top, 
                        bottom, text);
                }
            }
        });
    }
    
    // TODO unify partActivated and partOpened
    public void partOpened(IEditorPart editorPart) {
        if (!isSharedEditor(editorPart))
            return;
        
        editorPool.add(editorPart);
        sharedEditorActivated(editorPart); // HACK
    }
    
    public void partActivated(IEditorPart editorPart) {
        if (!isSharedEditor(editorPart))
            return;
        
        sharedEditorActivated(editorPart);
    }
    
    public void partClosed(IEditorPart editorPart) {
        if (!isSharedEditor(editorPart) || !isDriver)
            return;
        
        IResource resource = editorAPI.getEditorResource(editorPart);
        IPath path = resource.getProjectRelativePath();
        removeDriverEditor(path, false); // HACK
    }
    
    /**
     * Checks wether given resource is currently opened.
     * 
     * @param path the project-relative path to the resource.
     * @return <code>true</code> if the given resource is opened accoring to
     * the editor pool.
     */
    public boolean isOpened(IPath path) {
        return editorPool.getEditors(path).size() > 0;
    }

    /* (non-Javadoc)
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
    		log.severe("Couldn't parse message");
    	} catch (IOException e) {
    		log.severe("Couldn't parse message");
    	}
    	
    	return null;
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.IActivityProvider
	 */
	public String toXML(IActivity activity) {
		if (activity instanceof EditorActivity) {
	        EditorActivity editorActivity = (EditorActivity)activity;
	        return "<editor " +
	            "path=\""+editorActivity.getPath()+"\" " +
	            "type=\""+editorActivity.getType()+"\" />";
	        
	    } else if (activity instanceof TextEditActivity) {
	        TextEditActivity textEditActivity = (TextEditActivity)activity;
	        return "<edit " +
	            "offset=\"" +textEditActivity.offset  +"\" " +
	            "replace=\""+textEditActivity.replace +"\">" +
	            "<![CDATA[" +textEditActivity.text    +"]]>" +
	            "</edit>";
	        
	    } else if (activity instanceof TextSelectionActivity) {
	        TextSelectionActivity textSelection = (TextSelectionActivity)activity;
	        return "<textSelection " +
	            "offset=\""+textSelection.getOffset()+"\" "+
	            "length=\""+textSelection.getLength()+"\" />";
	        
	    } else if (activity instanceof ViewportActivity) {
	        ViewportActivity viewportActvity = (ViewportActivity)activity;
	        return "<viewport " +
	            "top=\""+viewportActvity.getTopIndex()+"\" " +
	            "bottom=\""+viewportActvity.getBottomIndex()+"\" />";
	    }
		
		return null;
	}

	private IActivity parseTextEditActivity(XmlPullParser parser) throws XmlPullParserException, IOException {
		// TODO extract constants
		int offset = Integer.parseInt(parser.getAttributeValue(null, "offset"));
		int replace = Integer.parseInt(parser.getAttributeValue(null, "replace"));

		String text = "";
		if (parser.next() == XmlPullParser.TEXT) {
			text = parser.getText();
		}

		return new TextEditActivity(offset, text, replace);
	}

	private IActivity parseEditorActivity(XmlPullParser parser) {
		String pathString = parser.getAttributeValue(null, "path");

		// TODO handle cases where the file is really named "null"
		Path path = pathString.equals("null") ? null : new Path(pathString);

		Type type = EditorActivity.Type.valueOf(parser.getAttributeValue(null, "type"));
		return new EditorActivity(type, path);
	}
	
	private TextSelectionActivity parseTextSelection(XmlPullParser parser) {
        // TODO extract constants
        int offset = Integer.parseInt(parser.getAttributeValue(null, "offset")); 
        int length = Integer.parseInt(parser.getAttributeValue(null, "length"));
        return new TextSelectionActivity(offset, length);
    }
	
	private ViewportActivity parseViewport(XmlPullParser parser) {
        int top = Integer.parseInt(parser.getAttributeValue(null, "top"));
        int bottom = Integer.parseInt(parser.getAttributeValue(null, "bottom"));
        return new ViewportActivity(top, bottom);
    }

	private boolean isSharedEditor(IEditorPart editorPart) {
        IResource resource = editorAPI.getEditorResource(editorPart);
        return (sharedProject != null && 
            resource.getProject() == sharedProject.getProject());
    }
    
    private void insertDriverText(IFile file, int offset, int replace, String text) {
        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = editorAPI.getDocumentProvider(input);
    
        try {
            provider.connect(input);
            IDocument doc = provider.getDocument(input);
            doc.replace(offset, replace, text);
            
            IAnnotationModel model = provider.getAnnotationModel(input);
            ContributionHelper.insertContribution(model, offset, text.length());
            
            // TODO We can't just disconnect from the provider, because
            // otherwise pending text changes will be lost.
            
        } catch (BadLocationException e) {
            log.log(Level.SEVERE, "Couldn't edit text because of bad location.", e);
            
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Needs to be called form a UI thread.
     */
    private void resetText(IFile file) {
        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = editorAPI.getDocumentProvider(input);
    
        try {
            provider.connect(input);
            provider.resetDocument(input);
            
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sends given activity to all registered activity listeners.
     */
    private void fireActivity(IActivity activity) {
        for (IActivityListener listener : activityListeners) {
            listener.activityCreated(activity);
        }
    }

    /**
     * Replaces the line delimiters in given text by the default line delimiters
     * of given file.
     * 
     * @return the string with replaced delimiters.
     */
    private String fixDelimiters(IFile file, String text) {
        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = editorAPI.getDocumentProvider(input);
        IDocument doc = provider.getDocument(input);
        
        if (doc instanceof IDocumentExtension4) {
            IDocumentExtension4 docExtension4 = (IDocumentExtension4)doc;
            String delimiter = docExtension4.getDefaultLineDelimiter();
            
            if (delimiter != null)
                return text.replace("\n", delimiter);
        }
        
        return text;
    }

    private void activateOpenEditors() {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                for (IEditorPart editorPart : editorAPI.getOpenEditors()) {
                    partOpened(editorPart);
                }

                IEditorPart activeEditor = editorAPI.getActiveEditor();
                if (activeEditor != null) {
                    sharedEditorActivated(activeEditor);
                }
            }
        });
    }
    
    private void sharedEditorActivated(IEditorPart editorPart) {
        if (!isDriver) return;
        
        IResource resource = editorAPI.getEditorResource(editorPart);
        IPath editorPath = resource.getProjectRelativePath();
        setActiveDriverEditor(editorPath, false);

        ITextSelection selection = editorAPI.getSelection(editorPart);
        setDriverTextSelection(selection);

        ILineRange viewport = editorAPI.getViewport(editorPart);
        int startLine = viewport.getStartLine();
        viewportChanged(startLine, startLine + viewport.getNumberOfLines());
    }
    
    private void setAllEditorsToEditable() {
		for (IEditorPart editor : editorPool.getAllEditors()) {
			editorAPI.setEditable(editor, true);
		}
	}

	private EditorManager() {
	    setEditorAPI(new EditorAPI());
	    Saros.getDefault().getSessionManager().addSessionListener(this);
	}

	/**
     * Sets the currently active driver editor.
     * 
     * @param path the project-relative path to the resource that the editor is
     * currently editting.
     * @param replicated <code>false</code> if this action originates on this
     * client. <code>false</code> if it is an replication of an action from
     * another participant of the shared project.
     */
    private void setActiveDriverEditor(IPath path, boolean replicated) {
        activeDriverEditor = path;
        driverEditors.add(path);
        
        for (ISharedEditorListener listener : editorListeners) {
            listener.activeDriverEditorChanged(activeDriverEditor, replicated);
        }
        
        if (replicated) {
            if (isFollowing) {
                Display.getDefault().syncExec(new Runnable(){
                    public void run() {
                        openDriverEditor();
                    }
                });
            }
            
        } else {
            IActivity activity = new EditorActivity(Type.Activated, path);
            for (IActivityListener listener : activityListeners) {
                listener.activityCreated(activity);
            }
        }
    }
    
    /**
     * Removes the given editor from the list of editors that the driver is
     * currently using.
     * 
     * @param path the path to the resource that the driver was editting.
     * @param replicated <code>false</code> if this action originates on this
     * client. <code>false</code> if it is an replication of an action from
     * another participant of the shared project.
     */
    private void removeDriverEditor(final IPath path, boolean replicated) {
        if (path.equals(activeDriverEditor)) { // HACK
            setActiveDriverEditor(null, replicated);
        }
        
        driverEditors.remove(path);
        
        for (ISharedEditorListener listener : editorListeners) {
            listener.driverEditorRemoved(path, replicated);
        }
        
        if (replicated) {
            Display.getDefault().syncExec(new Runnable(){
                public void run() {
                    IFile file = sharedProject.getProject().getFile(path);
                    resetText(file);
                    
                    if (!isFollowing)
                        return;
                    
                    Set<IEditorPart> editors = editorPool.getEditors(path);
                    for (IEditorPart part : editors) {
                        editorAPI.closeEditor(part);
                    }
                }
            });
            
        } else {
            IActivity activity = new EditorActivity(Type.Closed, path);
            for (IActivityListener listener : activityListeners) {
                listener.activityCreated(activity);
            }
        }
    }
    
    /**
     * Saves the driver editor.
     * 
     * @param path the path to the resource that the driver was editting.
     * @param replicated <code>false</code> if this action originates on this
     * client. <code>false</code> if it is an replication of an action from
     * another participant of the shared project.
     */
    private void saveDriverEditor(IPath path, boolean replicated) {
        for (ISharedEditorListener listener : editorListeners) {
            listener.driverEditorSaved(path, replicated);
        }
        
        if (replicated) {
            IFile file = sharedProject.getProject().getFile(path);
            FileEditorInput input = new FileEditorInput(file);
            
            try {
                IDocumentProvider provider = editorAPI.getDocumentProvider(input);
                
                provider.connect(input);
                IDocument doc = provider.getDocument(input);
                
                IAnnotationModel model = provider.getAnnotationModel(input);
                model.connect(doc);
                
                provider.saveDocument(new NullProgressMonitor(), input, doc, true);
                log.fine("Saved document at "+path);
                
                model.disconnect(doc);
                provider.disconnect(input);
                
            } catch (CoreException e) {
                log.log(Level.SEVERE, "Failed to save document.", e);
            }
            
        } else {
            IActivity activity = new EditorActivity(Type.Saved, path);
            for (IActivityListener listener : activityListeners) {
                listener.activityCreated(activity);
            }
        }
    }

    /**
     * @param selection sets the current text selection that is used by the
     * driver.
     */
    private void setDriverTextSelection(ITextSelection selection) {
        driverTextSelection = selection;
    }
}
