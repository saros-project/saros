package de.fu_berlin.inf.dpp.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;

/**
 * This class contains static utility methods for file handling.
 * 
 * @author orieger/chjacob
 * 
 */
public class FileUtil {

    private static Logger log = Logger.getLogger(FileUtil.class);

    /**
     * calculate checksum for given file
     * 
     * @param file
     * @return checksum of file or -1 if checksum calculation has been failed.
     */
    public static Long checksum(IFile file) {

        // Adler-32 checksum
        InputStream contents;
        try {
            contents = file.getContents();
        } catch (CoreException e) {
            log.error("Failed to calculate checksum:", e);
            return -1L;
        }

        CheckedInputStream cis = new CheckedInputStream(contents, new Adler32());
        InputStream in = new BufferedInputStream(cis);
        byte[] tempBuf = new byte[8192];
        try {
            while (in.read(tempBuf) >= 0) {
                // continue until buffer empty
            }
            return Long.valueOf(cis.getChecksum().getValue());
        } catch (IOException e) {
            log.error("Failed to calculate checksum:", e);
        } finally {
            IOUtils.closeQuietly(in);
        }

        return -1L;
    }

    /**
     * Makes the given file read-only (</code>readOnly == true</code>) or
     * writable (<code>readOnly == false</code>).
     * 
     * @param file
     *            the resource whose read-only attribute is to be set or removed
     * @param readOnly
     *            <code>true</code> to set the given file to be read-only,
     *            <code>false</code> to make writable
     * @return The state before setting read-only to the given value.
     */
    public static boolean setReadOnly(IResource file, boolean readOnly) {
        ResourceAttributes attributes = file.getResourceAttributes();
        if (attributes == null) {
            // TODO Throw an FileNotFoundException and deal with it everywhere!
            log.warn("File does not exist for setting readonly == " + readOnly
                + ": " + file);
            return false;
        }
        boolean result = attributes.isReadOnly();
        attributes.setReadOnly(readOnly);
        try {
            file.setResourceAttributes(attributes);
        } catch (CoreException e) {
            // failure is not an option
            log.warn("Failed to set resource readonly == " + readOnly + ": "
                + file);
        }
        return result;
    }

    /**
     * Writes the given input stream to the given file.
     * 
     * This operation will unset a possible readOnly flag and reset if after the
     * operation.
     * 
     * @param input
     *            the input stream
     * @param file
     *            the file to create/overwrite
     */
    public static void writeFile(InputStream input, IFile file) {

        if (file.exists()) {
            replaceFileContent(input, file);
        } else {
            createFile(input, file);
        }

    }

    /**
     * Creates the given file and any missing parent directories.
     * 
     * This method will try to remove read-only settings on the parent
     * directories and reset them at the end of the operation.
     * 
     * @pre the file must not exist. Use writeFile() for getting this cases
     *      handled.
     */
    public static void createFile(InputStream input, IFile file) {

        // Make sure directory exists
        mkdirs(file);

        // Make sure that parent is writable
        IContainer parent = file.getParent();
        boolean wasReadOnly = false;
        if (parent != null) {
            ResourceAttributes attributes = parent.getResourceAttributes();
            if (attributes != null && attributes.isReadOnly()) {
                wasReadOnly = true;
                setReadOnly(parent, false);
            }
        }

        try {
            BlockingProgressMonitor monitor = new BlockingProgressMonitor();
            file.create(input, true, monitor);
            try {
                monitor.await();
            } catch (InterruptedException e) {
                log.error("Code not designed to be interruptable", e);
                Thread.currentThread().interrupt();
            }
        } catch (CoreException e) {
            log.error("Could not write file", e);
        }

        // Reset permissions on parent
        if (parent != null && wasReadOnly)
            setReadOnly(parent, wasReadOnly);
    }

    /**
     * Replace the data in the file with the data from the given InputStream.
     * 
     * @pre the file must exist
     */
    public static void replaceFileContent(InputStream input, IFile file) {
        boolean wasReadOnly = false;
        if (file.isReadOnly()) {
            wasReadOnly = true;
            setReadOnly(file, false);
        }

        try {
            BlockingProgressMonitor monitor = new BlockingProgressMonitor();
            file.setContents(input, IResource.FORCE, monitor);
            try {
                monitor.await();
            } catch (InterruptedException e) {
                log.error("Code not designed to be interruptable", e);
                Thread.currentThread().interrupt();
            }
        } catch (CoreException e) {
            log.error("Could not write file", e);
        }

        if (wasReadOnly)
            setReadOnly(file, wasReadOnly);
    }

    /**
     * Makes sure that the parent directories of the given IFile exist, possibly
     * removing write protection.
     */
    public static boolean mkdirs(IFile file) {

        if (file == null)
            return true;

        IFolder parent = getParentFolder(file);
        if (parent == null || parent.exists())
            return true;

        IContainer root = parent;
        while (!root.exists()) {
            IContainer temp = root.getParent();
            if (temp == null)
                break;
            root = temp;
        }
        boolean canWriteInParent = FileUtil.setReadOnly(root, false);

        try {
            if (!create(parent)) {
                log.error("Could not create Dir: " + parent.getFullPath());
                return false;
            } else
                return true;
        } finally {
            if (!canWriteInParent)
                FileUtil.setReadOnly(root, true);
        }
    }

    public static IFolder getParentFolder(IResource resource) {

        if (resource == null) {
            return null;
        }
        IContainer parent = resource.getParent();
        if (parent == null || parent.getType() != IResource.FOLDER) {
            return null;
        }
        return (IFolder) parent;
    }

    public static boolean create(final IFolder folder) {

        if (folder == null || folder.exists()) {
            return true;
        }
        create(getParentFolder(folder));
        try {
            folder.create(IResource.NONE, true, new NullProgressMonitor());
            return true;
        } catch (CoreException e) {
            return false;
        }
    }

    /**
     * @swt Must be called from the SWT thread
     */
    public static void setReadOnly(final IProject project,
        final boolean readonly) {
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(EditorAPI
            .getShell());
        try {
            dialog.run(true, false, new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) {
                    FileUtil.setReadOnly(project, readonly, monitor);
                }
            });
        } catch (InvocationTargetException e) {
            log.error("Could not set project read-only: ", e);
        } catch (InterruptedException e) {
            log.error("Code not designed to be interruptable", e);
            Thread.currentThread().interrupt();
        }
    }

    public static void setReadOnly(IProject project, final boolean readonly,
        final IProgressMonitor monitor) {
        monitor.beginTask("Project settings ... ", IProgressMonitor.UNKNOWN);

        try {
            project.accept(new IResourceVisitor() {
                public boolean visit(IResource resource) throws CoreException {

                    // Don't set the project and derived
                    // files read-only
                    if (resource instanceof IProject || resource.isDerived())
                        return true;

                    setReadOnly(resource, readonly);
                    monitor.worked(1);

                    return true;
                }
            });
        } catch (CoreException e) {
            log.warn("Failure to set readonly to " + readonly + ":", e);
        } finally {
            monitor.done();
        }
    }

    public static void deleteFile(IFile file) throws CoreException {
        if (!file.exists()) {
            log.warn("File not found for deletion: " + file);
            return;
        }

        setReadOnly(file, false);
        BlockingProgressMonitor monitor = new BlockingProgressMonitor();
        file.delete(false, monitor);
        try {
            monitor.await();
        } catch (InterruptedException e) {
            log.error("Code not designed to be interruptable", e);
            Thread.currentThread().interrupt();
        }

        if (monitor.isCanceled()) {
            log.warn("Removing file failed: " + file);
        }
    }

}
