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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;

import de.fu_berlin.inf.dpp.activities.CursorOffsetActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.internal.IHumbleEditorManager;
import de.fu_berlin.inf.dpp.listeners.ISharedEditorListener;
import de.fu_berlin.inf.dpp.listeners.ISharedProjectListener;
import de.fu_berlin.inf.dpp.xmpp.JID;

public class EditorManager implements ISharedEditorListener, 
    IActivityProvider, ISharedProjectListener {
    
    private ISharedProject           sharedProject;
    private IHumbleEditorManager     humbleManager;

    private List<IActivityListener>  activityListeners = new LinkedList<IActivityListener>();
    private Map<IPath, IEditorPart>  editorParts = new HashMap<IPath, IEditorPart>();
    private boolean isFollowing;
    
    
    public EditorManager(ISharedProject sharedProject, IHumbleEditorManager humbleManager) {
        this.sharedProject = sharedProject;
        this.humbleManager = humbleManager;
        humbleManager.setEditorManager(this);
        
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                convertOpenEditorsToSharedEditors();
                activateOpenedEditor();
            }
        });
        
        updateEditable();
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISharedEditorListener
     */
    public void viewportChanged(int topIndex, int bottomIndex) {
        // ignore
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISharedEditorListener
     */
    public void cursorChanged(ITextSelection selection) {
        IActivity activity = new CursorOffsetActivity(
            selection.getOffset(), selection.getLength());
        
        for (IActivityListener listener : activityListeners) {
            listener.activityCreated(activity);
        }
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISharedEditorListener
     */
    public void textChanged(int offset, String text, int replace, int line) {
        IActivity activity = new TextEditActivity(offset, text, replace);
        
        for (IActivityListener listener : activityListeners) {
            listener.activityCreated(activity);
        }
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void driverChanged(JID driver, boolean replicated) {
        updateEditable();
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void driverPathChanged(IPath path, boolean replicated) {
        // TODO
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
    
    /**
     * This method needs to be called from an UI-thread.
     */
    public void openDriverEditor() {
        IPath path = sharedProject.getDriverPath();
        IFile file = sharedProject.getProject().getFile(path);
        
        humbleManager.openEditor(file);
    }

    public void setEnableFollowing(boolean enable) {
        isFollowing = enable;
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
        if (activity instanceof CursorOffsetActivity) {
            CursorOffsetActivity cursorOffset = (CursorOffsetActivity)activity;
            sharedProject.setDriverTextSelection(new TextSelection(
                cursorOffset.getOffset(), cursorOffset.getLength()));
        }
        
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                if (activity instanceof TextEditActivity) {
                    execTextEdit((TextEditActivity)activity);
                    
                } else if (activity instanceof CursorOffsetActivity) {
                    execCursorOffset((CursorOffsetActivity)activity);
                }
                
                if (isFollowing)
                    openDriverEditor();
            }

            private void execCursorOffset(CursorOffsetActivity cursor) {
                IEditorPart editorPart = editorParts.get(sharedProject.getDriverPath());
                if (editorPart != null) {
                    humbleManager.setSelection(editorPart, new TextSelection(
                        cursor.getOffset(), cursor.getLength()));
                }
            }

            private void execTextEdit(TextEditActivity textEdit) {
                humbleManager.setText(
                    sharedProject.getProject().getFile(sharedProject.getDriverPath()),
                    textEdit.offset, textEdit.replace, textEdit.text); // HACK
                
                IEditorPart editorPart = editorParts.get(sharedProject.getDriverPath());
                if (editorPart != null) {
                    humbleManager.setSelection(editorPart, new TextSelection(textEdit.offset, 0));
                }
            }
        });
    }
    
    public void partActivated(IEditorPart editorPart) {
        registerEditorPart(editorPart);
        sharedEditorActivated(editorPart);
    }
    
    private void convertOpenEditorsToSharedEditors() {
        for (IEditorPart editorPart : humbleManager.getOpenedEditors()) {
            registerEditorPart(editorPart);
        }
    }
    
    private void registerEditorPart(IEditorPart editorPart) {
        editorParts.put(humbleManager.getEditorPath(editorPart), editorPart);
    }

    private void activateOpenedEditor() {
        IEditorPart activeEditor = humbleManager.getActiveEditor();
        
        if (activeEditor != null) {
            sharedEditorActivated(activeEditor);
        }
    }
    
    private void updateEditable() {
        for (IEditorPart editorPart : editorParts.values()) {
            humbleManager.setEditable(editorPart, sharedProject.isDriver());
        }
    }

    private void sharedEditorActivated(IEditorPart editorPart) {
        boolean isDriver = sharedProject.isDriver();

        if (isDriver) {
            sharedProject.setDriverPath(humbleManager.getEditorPath(editorPart), false);
            humbleManager.connect(editorPart);

        } else {
            if (sharedProject.getDriverPath() != null && 
                sharedProject.getDriverTextSelection() != null) {

                IEditorPart part = editorParts.get(sharedProject.getDriverPath());
                if (part != null) {
                    humbleManager.setSelection(part, sharedProject.getDriverTextSelection());
                }
            }
        }
    }
}
