package de.fu_berlin.inf.dpp.editor;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.filebuffers.manipulation.ConvertLineDelimitersOperation;
import org.eclipse.core.filebuffers.manipulation.FileBufferOperationRunner;
import org.eclipse.core.filebuffers.manipulation.TextFileBufferOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import de.fu_berlin.inf.dpp.util.FileUtils;

/**
 * Static utility methods for working with Eclipse Editors
 */
public class EditorUtils {

    /**
     * Returns the TextFileBuffer associated with the given resource OR null if
     * the resource could not be traced to a Buffer.
     */
    protected static ITextFileBuffer getTextFileBuffer(IResource resource) {

        IPath fullPath = resource.getFullPath();

        ITextFileBufferManager tfbm = FileBuffers.getTextFileBufferManager();

        ITextFileBuffer fileBuff = tfbm.getTextFileBuffer(fullPath,
            LocationKind.IFILE);
        if (fileBuff != null)
            return fileBuff;
        else {
            try {
                /*
                 * FIXME This call to connect never has a matching call to
                 * disconnect
                 */
                tfbm.connect(fullPath, LocationKind.IFILE,
                    new NullProgressMonitor());
            } catch (CoreException e) {
                EditorManager.log
                    .error("Could not connect to file " + fullPath);
                return null;
            }
            return tfbm.getTextFileBuffer(fullPath, LocationKind.IFILE);
        }
    }

    /**
     * TODO Unused since 2009-08-26.
     */
    public static void convertLineDelimiters(IFile file) {

        EditorManager.log.debug("Converting line delimiters...");

        boolean wasReadOnly = FileUtils.setReadOnly(file, false);

        // Now run the conversion operation
        IPath[] paths = new IPath[] { file.getFullPath() };

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
            // FIXME #2671663: Converting Line Delimiters causes Save
            runner.execute(paths, convertOperation, new NullProgressMonitor());
        } catch (OperationCanceledException e) {
            EditorManager.log.error("Can't convert line delimiters:", e);
        } catch (CoreException e) {
            EditorManager.log.error("Can't convert line delimiters:", e);
        }

        if (wasReadOnly) {
            FileUtils.setReadOnly(file, true);
        }
    }

}
