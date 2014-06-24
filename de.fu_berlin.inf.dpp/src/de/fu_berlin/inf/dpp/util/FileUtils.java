package de.fu_berlin.inf.dpp.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

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
            // TODO Throw a FileNotFoundException and deal with it everywhere!
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
        IProgressMonitor monitor) throws CoreException {
        if (file.exists()) {
            updateFile(input, file, monitor);
        } else {
            createFile(input, file, monitor);
        }

    }

    /**
     * Move the file to the same location, adding the file extension "BACKUP" or
     * "_BACKUP_X" on file name where X is a number that matches a not used file
     * name.
     * 
     * @param file
     *            the {@link IFile} to rename
     * @param monitor
     *            a progress monitor to show progress to user
     * @throws CoreException
     * @throws FileNotFoundException
     */
    public static void backupFile(IFile file, IProgressMonitor monitor)
        throws CoreException, FileNotFoundException {

        if (!file.exists())
            throw new FileNotFoundException();

        IProject project = file.getProject();

        IPath originalBackupPath = file.getProjectRelativePath()
            .addFileExtension("BACKUP");

        IPath backupPath = originalBackupPath;

        for (int i = 0; i < 1000; i++) {
            if (!project.exists(backupPath))
                break;

            backupPath = originalBackupPath.removeFileExtension()
                .addFileExtension("BACKUP_" + i);
        }

        file.move(
            file.getFullPath().removeLastSegments(1)
                .append(backupPath.lastSegment()), true, monitor);
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
        IProgressMonitor monitor) throws CoreException {

        IWorkspaceRunnable createFileProcedure = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                // Make sure directory exists
                mkdirs(file);

                // Make sure that parent is writable
                IContainer parent = file.getParent();
                boolean wasReadOnly = false;
                if (parent != null)
                    wasReadOnly = setReadOnly(parent, false);

                file.create(input, true, monitor);

                // Reset permissions on parent
                if (parent != null && wasReadOnly)
                    setReadOnly(parent, true);

            }
        };

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(createFileProcedure, workspace.getRoot(),
            IWorkspace.AVOID_UPDATE, null);

    }

    /**
     * Updates the data in the file with the data from the given InputStream.
     * 
     * @pre the file must exist
     */
    public static void updateFile(final InputStream input, final IFile file,
        IProgressMonitor monitor) throws CoreException {

        IWorkspaceRunnable replaceFileProcedure = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                file.setContents(input, true, true, monitor);
            }
        };

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(replaceFileProcedure, workspace.getRoot(),
            IWorkspace.AVOID_UPDATE, null);
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
            @Override
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

    public static void delete(final IResource resource) throws CoreException {
        if (!resource.exists()) {
            log.warn("File not found for deletion: " + resource,
                new StackTrace());
            return;
        }

        IWorkspaceRunnable deleteProcedure = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                if (!resource.exists())
                    return;

                if (resource.getResourceAttributes() == null)
                    return;

                setReadOnly(resource, false);

                resource.delete(IResource.FORCE | IResource.KEEP_HISTORY,
                    monitor);

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
            @Override
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

    /**
     * Calculates the total file count and size for all resources.
     * 
     * @param resources
     *            collection containing the resources that file sizes and file
     *            count should be calculated
     * @param includeMembers
     *            <code>true</code> to include the members of resources that
     *            represents a {@linkplain IContainer container}
     * @param flags
     *            additional flags on how to process the members of containers
     * @return a pair containing the {@linkplain Pair#p file size} and
     *         {@linkplain Pair#v file count} for the given resources
     */
    public static Pair<Long, Long> getFileCountAndSize(
        Collection<? extends IResource> resources, boolean includeMembers,
        int flags) {
        long totalFileSize = 0;
        long totalFileCount = 0;

        Pair<Long, Long> fileCountAndSize = new Pair<Long, Long>(0L, 0L);

        for (IResource resource : resources) {
            switch (resource.getType()) {
            case IResource.FILE:
                totalFileCount++;

                try {
                    long filesize = EFS.getStore(resource.getLocationURI())
                        .fetchInfo().getLength();

                    totalFileSize += filesize;
                } catch (Exception e) {
                    log.warn(
                        "failed to retrieve file size of file "
                            + resource.getLocationURI(), e);
                }
                break;
            case IResource.PROJECT:
            case IResource.FOLDER:
                if (!includeMembers)
                    break;

                try {
                    IContainer container = ((IContainer) resource
                        .getAdapter(IContainer.class));

                    Pair<Long, Long> subFileCountAndSize = FileUtils
                        .getFileCountAndSize(
                            Arrays.asList(container.members(flags)),
                            includeMembers, flags);

                    totalFileSize += subFileCountAndSize.p;
                    totalFileCount += subFileCountAndSize.v;

                } catch (Exception e) {
                    log.warn("failed to process container: " + resource, e);
                }
                break;
            default:
                break;
            }
        }
        fileCountAndSize.p = totalFileSize;
        fileCountAndSize.v = totalFileCount;
        return fileCountAndSize;
    }

    /**
     * Retrieves the content of a local file
     * 
     * @param localFile
     * @return Byte array of the file contents. Is <code>null</code> if the file
     *         does not exist or is out of sync, the reference points to no
     *         file, or the conversion to a byte array failed.
     */
    public static byte[] getLocalFileContent(IFile localFile) {
        InputStream in = null;
        byte[] content = null;
        try {
            in = localFile.getContents();
            content = IOUtils.toByteArray(in);
        } catch (CoreException e) {
            log.warn("could not get content of file " + localFile.getFullPath());
        } catch (IOException e) {
            log.warn("could not convert file content to byte array (file: "
                + localFile.getFullPath() + ")");
        } finally {
            IOUtils.closeQuietly(in);
        }
        return content;
    }

}
