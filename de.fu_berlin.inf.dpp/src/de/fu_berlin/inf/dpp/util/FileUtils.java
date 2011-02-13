package de.fu_berlin.inf.dpp.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;

/**
 * This class contains static utility methods for file handling.
 * 
 * @author orieger/chjacob
 * 
 */
public class FileUtils {

    private static Logger log = Logger.getLogger(FileUtils.class);

    private FileUtils() {
        // no instantiation allowed
    }

    /**
     * Calculate Adler32 checksum for given file.
     * 
     * @return checksum of file
     * 
     * @throws CausedIOException
     *             if checksum calculation has been failed.
     */
    public static long checksum(IFile file) throws IOException {

        InputStream contents;
        try {
            contents = file.getContents();
        } catch (CoreException e) {
            throw new CausedIOException("Failed to calculate checksum.", e);
        }

        CheckedInputStream in = new CheckedInputStream(contents, new Adler32());
        try {
            IOUtils.copy(in, new NullOutputStream());
        } catch (IOException e) {
            throw new CausedIOException("Failed to calculate checksum.", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return in.getChecksum().getValue();
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
            log.error("File does not exist for setting readOnly == " + readOnly
                + ": " + file, new StackTrace());
            return false;
        }
        boolean result = attributes.isReadOnly();

        // Already in desired state
        if (result == readOnly)
            return result;

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
     * This operation will remove a possible readOnly flag and re-set if after
     * the operation.
     * 
     * @blocking This operations blocks until the operation is reported as
     *           finished by Eclipse.
     * 
     * @param input
     *            the input stream to write to the file
     * @param file
     *            the file to create/overwrite
     * @throws CoreException
     *             if the file could not be written.
     */
    public static void writeFile(InputStream input, IFile file,
        SubMonitor monitor) throws CoreException {
        if (file != null && file.exists()) {
            replaceFileContent(input, file, monitor);
        } else {
            createFile(input, file, monitor);
        }

    }

    /**
     * Unzip the data in the given InputStream as a Zip archive to the given
     * IContainer
     * 
     * @cancelable This long-running operation can be canceled via the given
     *             progress monitor and will throw an LocalCancellationException
     *             in this case.
     */
    public static boolean writeArchive(InputStream input, IContainer container,
        SubMonitor monitor) throws CoreException, LocalCancellationException {

        ZipInputStream zip = new ZipInputStream(input);

        long startTime = System.currentTimeMillis();

        try {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {

                if (monitor.isCanceled())
                    throw new LocalCancellationException();

                IPath path = Path.fromPortableString(entry.getName());
                IFile file = container.getFile(path);

                writeFile(new FilterInputStream(zip) {
                    @Override
                    public void close() throws IOException {
                        // prevent the ZipInputStream from being closed
                    }
                }, file, monitor.newChild(1));

                log.debug("File written to disk: " + path);

                zip.closeEntry();
            }
            log.debug(String.format("Unpacked archive in %d s",
                (System.currentTimeMillis() - startTime) / 1000));

        } catch (IOException e) {
            log.error("Failed to unpack archive", e);
            return false;
        } finally {
            IOUtils.closeQuietly(zip);
            monitor.done();
        }
        return true;
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
    public static void createFile(final InputStream input, final IFile file,
        SubMonitor monitor) throws CoreException {

        IWorkspaceRunnable createFolderProcedure = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                // Make sure directory exists
                mkdirs(file);

                // Make sure that parent is writable
                IContainer parent = file.getParent();
                boolean wasReadOnly = false;
                if (parent != null)
                    wasReadOnly = setReadOnly(parent, false);

                BlockingProgressMonitor blockingMonitor = new BlockingProgressMonitor(
                    monitor);
                file.create(input, true, blockingMonitor);

                try {
                    blockingMonitor.await();
                } catch (InterruptedException e) {
                    log.error("Code not designed to be interruptable", e);
                    Thread.currentThread().interrupt();
                }

                // Reset permissions on parent
                if (parent != null && wasReadOnly)
                    setReadOnly(parent, true);

            }
        };

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(createFolderProcedure, workspace.getRoot(),
            IWorkspace.AVOID_UPDATE, null);

    }

    /**
     * Replace the data in the file with the data from the given InputStream.
     * 
     * @blocking This operations blocks until the operation is reported as
     *           finished by Eclipse.
     * 
     * @pre the file must exist
     */
    public static void replaceFileContent(InputStream input, IFile file,
        SubMonitor monitor) throws CoreException {

        boolean wasReadOnly = setReadOnly(file, false);

        BlockingProgressMonitor blockingMonitor = new BlockingProgressMonitor(
            monitor);
        file.setContents(input, IResource.FORCE, blockingMonitor);
        try {
            blockingMonitor.await();
        } catch (InterruptedException e) {
            log.error("Code not designed to be interruptable", e);
            Thread.currentThread().interrupt();
        }

        if (wasReadOnly)
            setReadOnly(file, true);
    }

    /**
     * Makes sure that the parent directories of the given IResource exist,
     * possibly removing write protection.
     */
    public static boolean mkdirs(IResource resource) {

        if (resource == null)
            return true;

        IFolder parent = getParentFolder(resource);
        if (parent == null || parent.exists())
            return true;

        IContainer root = parent;
        while (!root.exists()) {
            IContainer temp = root.getParent();
            if (temp == null)
                break;
            root = temp;
        }
        boolean wasReadOnly = FileUtils.setReadOnly(root, false);

        try {
            create(parent);
        } catch (CoreException e) {
            log.error("Could not create Dir: " + parent.getFullPath());
            return false;
        } finally {
            if (wasReadOnly)
                FileUtils.setReadOnly(root, true);
        }
        return true;
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

    public static void create(final IFolder folder) throws CoreException {

        // if ((folder == null) || (folder.exists())) {
        // log.debug(".create() Creating folder not possible");
        // return;
        // }

        if (folder == null) {
            log.warn(".create() Creating folder not possible -  it is null");
            throw new IllegalArgumentException();
        }
        if (folder.exists()) {
            log.debug(".create() Creating folder " + folder.getName()
                + " not possible - it already exists");
            return;
        }
        IWorkspaceRunnable createFolderProcedure = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {

                // recursively create folders until parent folder exists
                // or project root is reached
                IFolder parentFolder = getParentFolder(folder);
                if (parentFolder != null) {
                    create(parentFolder);
                }

                folder.create(IResource.NONE, true, monitor);

                if (monitor.isCanceled()) {
                    log.warn("Creating folder failed: " + folder);
                }

            }
        };

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(createFolderProcedure, workspace.getRoot(),
            IWorkspace.AVOID_UPDATE, null);

    }

    /**
     * @swt Must be called from the SWT thread
     */
    public static void setReadOnly(final IProject project,
        final boolean readonly) {
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(
            EditorAPI.getShell());
        try {
            dialog.run(true, false, new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) {
                    FileUtils.setReadOnly(project, readonly, monitor);
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

    public static void delete(final IResource resource) throws CoreException {
        if (!resource.exists()) {
            log.warn("File not found for deletion: " + resource,
                new StackTrace());
            return;
        }

        IWorkspaceRunnable deleteProcedure = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                if (!resource.exists())
                    return;

                if (resource.getResourceAttributes() == null)
                    return;

                setReadOnly(resource, false);

                resource.delete(true, monitor);

                if (monitor.isCanceled()) {
                    log.warn("Removing resource failed: " + resource);
                }
            }
        };

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(deleteProcedure, workspace.getRoot(),
            IWorkspace.AVOID_UPDATE, null);

    }

    /**
     * Moves the given {@link IResource} to the place, that is pointed by the
     * given {@link IPath}.
     * 
     * This method excepts both variables to be relative to the workspace.
     * 
     * @param destination
     *            Destination of moving the given resource.
     * @param source
     *            Resource, that is going to be moved
     * 
     * */
    public static void move(final IPath destination, final IResource source)
        throws CoreException {

        log.trace(".move(" + destination.toOSString() + " , "
            + source.getName() + ")");

        if (!source.isAccessible()) {
            log.warn(".move Source file can not be accessed  "
                + source.getFullPath());
            return;
        }

        IWorkspaceRunnable moveProcedure = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                IPath absDestination = destination.makeAbsolute();

                source.move(absDestination, false, monitor);

                if (monitor.isCanceled()) {
                    log.warn("Moving resource failed (Cancel Button pressed).");
                }
            }
        };

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(moveProcedure, workspace.getRoot(),
            IWorkspace.AVOID_UPDATE, null);

    }
}
