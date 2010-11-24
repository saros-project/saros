package de.fu_berlin.inf.dpp.editor.internal;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.runtime.IPath;

public class AbstractFileBufferListener implements IFileBufferListener {

    public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
        // ignore
    }

    public void bufferContentReplaced(IFileBuffer buffer) {
        // ignore
    }

    public void bufferCreated(IFileBuffer buffer) {
        // ignore
    }

    public void bufferDisposed(IFileBuffer buffer) {
        // ignore
    }

    public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
        // ignore
    }

    public void stateChangeFailed(IFileBuffer buffer) {
        // ignore
    }

    public void stateChanging(IFileBuffer buffer) {
        // ignore
    }

    public void stateValidationChanged(IFileBuffer buffer,
        boolean isStateValidated) {
        // ignore
    }

    public void underlyingFileDeleted(IFileBuffer buffer) {
        // ignore
    }

    public void underlyingFileMoved(IFileBuffer buffer, IPath path) {
        // ignore
    }

}
