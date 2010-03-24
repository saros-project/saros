package de.fu_berlin.inf.dpp.editor.internal;

import org.apache.log4j.Logger;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.util.Util;

/**
 * A delegating IFileBufferListener which makes the calls to the listener safe
 */
public class SafeDelegatingFileBufferListener implements IFileBufferListener {

    protected IFileBufferListener delegate;

    private static final Logger log = Logger
        .getLogger(SafeDelegatingFileBufferListener.class.getName());

    public SafeDelegatingFileBufferListener(IFileBufferListener delegate) {
        this.delegate = delegate;
    }

    public void bufferContentAboutToBeReplaced(final IFileBuffer buffer) {
        Util.wrapSafe(log, new Runnable() {
            public void run() {
                delegate.bufferContentReplaced(buffer);
            }
        }).run();
    }

    public void bufferContentReplaced(final IFileBuffer buffer) {
        Util.wrapSafe(log, new Runnable() {
            public void run() {
                delegate.bufferContentReplaced(buffer);
            }
        }).run();
    }

    public void bufferCreated(final IFileBuffer buffer) {
        Util.wrapSafe(log, new Runnable() {
            public void run() {
                delegate.bufferCreated(buffer);
            }
        }).run();
    }

    public void bufferDisposed(final IFileBuffer buffer) {
        Util.wrapSafe(log, new Runnable() {
            public void run() {
                delegate.bufferDisposed(buffer);

            }
        }).run();
    }

    public void dirtyStateChanged(final IFileBuffer buffer,
        final boolean isDirty) {
        Util.wrapSafe(log, new Runnable() {
            public void run() {
                delegate.dirtyStateChanged(buffer, isDirty);

            }
        }).run();
    }

    public void stateChangeFailed(final IFileBuffer buffer) {
        Util.wrapSafe(log, new Runnable() {
            public void run() {
                delegate.stateChangeFailed(buffer);

            }
        }).run();

    }

    public void stateChanging(final IFileBuffer buffer) {

        Util.wrapSafe(log, new Runnable() {
            public void run() {
                delegate.stateChanging(buffer);

            }
        }).run();
    }

    public void stateValidationChanged(final IFileBuffer buffer,
        final boolean isStateValidated) {
        Util.wrapSafe(log, new Runnable() {
            public void run() {
                delegate.stateValidationChanged(buffer, isStateValidated);

            }
        }).run();
    }

    public void underlyingFileDeleted(final IFileBuffer buffer) {
        Util.wrapSafe(log, new Runnable() {
            public void run() {
                delegate.underlyingFileDeleted(buffer);
            }
        }).run();
    }

    public void underlyingFileMoved(final IFileBuffer buffer, final IPath path) {
        Util.wrapSafe(log, new Runnable() {
            public void run() {
                delegate.underlyingFileMoved(buffer, path);
            }
        }).run();
    }
}
