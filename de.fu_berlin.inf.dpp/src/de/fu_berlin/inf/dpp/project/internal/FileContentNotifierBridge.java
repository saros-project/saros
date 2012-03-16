package de.fu_berlin.inf.dpp.project.internal;

import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Bridge class that maps Eclipse Resource change events to unique identifiers
 * by retrieving the absolute path relative to the workspace and converting the
 * path to a unique string.
 * 
 * @author Stefan Rossbach
 */
public class FileContentNotifierBridge implements IFileContentChangedNotifier,
    IResourceChangeListener {

    private CopyOnWriteArrayList<IFileContentChangedListener> fileContentChangedListeners = new CopyOnWriteArrayList<IFileContentChangedListener>();

    public FileContentNotifierBridge() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this,
            IResourceChangeEvent.POST_CHANGE);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.POST_CHANGE
            && event.getResource() instanceof IFile) {
            IFile file = (IFile) event.getResource();

            for (IFileContentChangedListener listener : fileContentChangedListeners)
                listener.fileContentChanged(file.getFullPath()
                    .toPortableString());
        }
    }

    @Override
    public void addFileContentChangedListener(
        IFileContentChangedListener listener) {
        fileContentChangedListeners.add(listener);

    }

    @Override
    public void removeFileContentChangedListener(
        IFileContentChangedListener listener) {
        fileContentChangedListeners.remove(listener);
    }
}
