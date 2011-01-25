package de.fu_berlin.inf.dpp.editor.internal;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * The SharedEditorFileBufferListener listens for changes of buffers relating to
 * Saros-session documents (e.g. edited .java Files). This listener watches for
 * "bufferDisposed()" events to get control over the file reverting process.
 * 
 */
public class RevertBufferListener {

    private static final Logger log = Logger
        .getLogger(RevertBufferListener.class.getName());

    protected EditorManager editorManager;

    protected ISarosSession sarosSession;

    protected FileReplacementInProgressObservable fileReplacementInProgress;

    public RevertBufferListener(EditorManager editorManager,
        ISarosSession sarosSession,
        FileReplacementInProgressObservable fileReplacementInProgress) {

        this.editorManager = editorManager;
        this.sarosSession = sarosSession;
        this.fileReplacementInProgress = fileReplacementInProgress;

        FileBuffers.getTextFileBufferManager().addFileBufferListener(
            bufferListener);
    }

    public void dispose() {
        FileBuffers.getTextFileBufferManager().removeFileBufferListener(
            bufferListener);
    }

    protected IFileBufferListener bufferListener = new SafeDelegatingFileBufferListener(
        new AbstractFileBufferListener() {
            @Override
            public void bufferDisposed(IFileBuffer buffer) {
                log.trace(".bufferDisposed invoked");

                // If file did not change; no need for revert
                if (!buffer.isDirty())
                    return;

                if (fileReplacementInProgress.isReplacementInProgress())
                    return;

                if (!sarosSession.hasWriteAccess()) {
                    // TODO Trigger consistency recovery/check
                    log.warn("User with read-only access reverted must cause inconsistencies");
                    return;
                }

                // the reverted buffer needs to implement the interface
                // ITextFileBuffer
                // only editing (and reverting) of text files is supported
                if (!(buffer instanceof ITextFileBuffer)) {
                    log.error("Trying to dispose a buffer that is NOT ITextFileBuffer");
                    return;
                }
                ITextFileBuffer tfBuffer = (ITextFileBuffer) buffer;

                // Get old content
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                IResource fileResource = root.findMember(tfBuffer.getLocation());

                if (fileResource == null)
                    return;

                // Is the revert in a shared IProject?
                if (!sarosSession.isShared(fileResource.getProject()))
                    return;

                SPath path = new SPath(fileResource);

                String oldContent = tfBuffer.getDocument().get();

                // The new content of the file must be known
                // It is going to be read using org.apache.commons.io API
                InputStream is = null;
                try {
                    is = tfBuffer.getFileStore().openInputStream(EFS.NONE,
                        new NullProgressMonitor());
                } catch (CoreException e) {
                    log.warn("Could not open InputStream of the document");
                    return;
                }

                String newContent = null;
                try {
                    newContent = IOUtils.toString(is, tfBuffer.getEncoding());
                } catch (IOException e) {
                    log.warn("Could not read from the InputStream of the document");
                    return;
                } finally {
                    IOUtils.closeQuietly(is);
                }

                editorManager.triggerRevert(path, newContent, oldContent);
            }
        });

}