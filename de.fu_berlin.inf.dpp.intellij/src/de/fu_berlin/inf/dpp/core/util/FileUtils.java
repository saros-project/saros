package de.fu_berlin.inf.dpp.core.util;

import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IResourceAttributes;
import de.fu_berlin.inf.dpp.util.Pair;
import de.fu_berlin.inf.dpp.util.StackTrace;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

// TODO: as done in 3adb19a193, stop force update of read-only flags.
public class FileUtils {

    private static Logger LOG = Logger.getLogger(FileUtils.class);

    private FileUtils() {
        // no instantiation allowed
    }

    /**
     * Makes the given file read-only (</code>readOnly == true</code>) or
     * writable (<code>readOnly == false</code>).
     *
     * @param file     the resource whose read-only attribute is to be set or removed
     * @param readOnly <code>true</code> to set the given file to be read-only,
     *                 <code>false</code> to make writable
     * @return The state before setting read-only to the given value.
     */
    public static boolean setReadOnly(IResource file, boolean readOnly) {

        IResourceAttributes attributes = file.getResourceAttributes();

        if (attributes == null) {
            // TODO Throw a FileNotFoundException and deal with it everywhere!
            LOG.error("File does not exist for setting readOnly == " + readOnly
                    + ": " + file, new StackTrace());
            return false;
        }
        boolean result = attributes.isReadOnly();

        // Already in desired state
        if (result == readOnly) {
            return result;
        }

        attributes.setReadOnly(readOnly);
        try {
            file.setResourceAttributes(attributes);
        } catch (IOException e) {
            // failure is not an option
            LOG.warn("Failed to set resource readonly == " + readOnly + ": "
                    + file);
        }
        return result;
    }

    /**
     * Writes the given input stream to the given file.
     * <p/>
     * This operation will removeAll a possible readOnly flag and re-set if
     * after the operation.
     *
     * @param input the input stream to write to the file
     * @param file  the file to create/overwrite
     * @throws IOException if the file could not be written.
     */
    public static void writeFile(InputStream input, IFile file)
            throws IOException {
        if (file.exists()) {
            updateFile(input, file);
        } else {
            createFile(input, file);
        }

    }

    /**
     * Creates the given file and any missing parent directories.
     * <p/>
     * This method will try to removeAll read-only settings on the parent
     * directories and reset them at the end of the operation.
     *
     * @pre the file must not exist. Use writeFile() for getting this cases
     * handled.
     */
    public static void createFile(final InputStream input, final IFile file)
            throws IOException {

        // Make sure directory exists
        mkdirs(file);

        // Make sure that parent is writable
        IContainer parent = file.getParent();
        boolean wasReadOnly = false;
        if (parent != null) {
            wasReadOnly = setReadOnly(parent, false);
        }

        file.create(input, true);

        // Reset permissions on parent
        if (parent != null && wasReadOnly) {
            setReadOnly(parent, true);
        }

    }

    /**
     * Updates the data in the file with the data from the given InputStream.
     *
     * @pre the file must exist
     */
    public static void updateFile(final InputStream input, final IFile file)
            throws IOException {

        file.setContents(input, true, true);
    }

    /**
     * Makes sure that the parent directories of the given IResource exist,
     * possibly removing write protection.
     */

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

    public static void create(final IFolder folder) throws IOException {

        // if ((folder == null) || (folder.exists())) {
        // LOG.debug(".create() Creating folder not possible");
        // return;
        // }

        if (folder == null) {
            LOG.warn(".create() Creating folder not possible -  it is null");
            throw new IllegalArgumentException();
        }
        if (folder.exists()) {
            LOG.debug(".create() Creating folder " + folder.getName()
                    + " not possible - it already exists");
            return;
        }

        // recursively create folders until parent folder exists
        // or project root is reached
        IFolder parentFolder = getParentFolder(folder);

        if (parentFolder != null) {
            create(parentFolder);
        }

        folder.create(IResource.NONE, true);
    }

    public static void delete(final IResource resource) throws IOException {
        if (!resource.exists()) {
            LOG.warn("File not found for deletion: " + resource,
                    new StackTrace());
            return;
        }

        if (!resource.exists()) {
            return;
        }

        if (resource.getResourceAttributes() == null) {
            return;
        }

        setReadOnly(resource, false);

        resource.delete(IResource.FORCE | IResource.KEEP_HISTORY);

    }

    /**
     * Moves the given {@link IResource} to the place, that is pointed by the
     * given {@link IPath}.
     * <p/>
     * This method excepts both variables to be relative to the workspace.
     *
     * @param destination Destination of moving the given resource.
     * @param source      Resource, that is going to be moved
     */
    public static void move(final IPath destination, final IResource source)
        throws IOException {

        LOG.trace(".move(" + destination.toOSString() + " , " + source.getName()
                + ")");

        IPath absDestination = destination.makeAbsolute();

        source.move(absDestination, false);
    }

    /**
     * Makes sure that the parent directories of the given IResource exist,
     * possibly removing write protection.
     */
    public static boolean mkdirs(IResource resource) {

        if (resource == null) {
            return true;
        }

        IFolder parent = getParentFolder(resource);
        if (parent == null || parent.exists()) {
            return true;
        }

        IContainer root = parent;
        while (!root.exists()) {
            IContainer temp = root.getParent();
            if (temp == null) {
                break;
            }
            root = temp;
        }
        boolean wasReadOnly = FileUtils.setReadOnly(root, false);

        try {
            create(parent);
        } catch (IOException e) {
            LOG.error("Could not create Dir: " + parent.getFullPath());
            return false;
        } finally {
            if (wasReadOnly) {
                FileUtils.setReadOnly(root, true);
            }
        }
        return true;
    }

    /**
     * Calculates the total file count and size for all resources.
     *
     * @param resources      collection containing the resources that file sizes and file
     *                       count should be calculated
     * @param includeMembers <code>true</code> to include the members of resources that
     *                       represents a {@linkplain IContainer container}
     * @param flags          additional flags on how to process the members of containers
     * @return a pair containing the
     * {@linkplain de.fu_berlin.inf.dpp.util.Pair#p file size} and
     * {@linkplain de.fu_berlin.inf.dpp.util.Pair#v file count} for the
     * given resources
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
                    long filesize = -1; // todo //
                    // EFS.getStore(resource.getLocationURI()).fetchInfo().getLength();

                    totalFileSize += filesize;
                } catch (Exception e) {
                    LOG.warn("failed to retrieve file size of file " + resource, e);
                }
                break;
            case IResource.PROJECT:
            case IResource.FOLDER:
                if (!includeMembers) {
                    break;
                }

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
                    LOG.warn("failed to process container: " + resource, e);
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
     * does not exist or is out of sync, the reference points to no
     * file, or the conversion to a byte array failed.
     */
    public static byte[] getLocalFileContent(IFile localFile) {

        InputStream in = null;
        byte[] content = null;
        try {
            in = localFile.getContents();
        } catch (IOException e) {
            LOG.warn(
                    "could not get content of file " + localFile.getFullPath());
        }

        if (in == null) {
            return null;
        }

        try {
            content = IOUtils.toByteArray(in);
        } catch (IOException e) {
            LOG.warn("could not convert file content to byte array (file: "
                    + localFile.getFullPath() + ")");
        } finally {
            IOUtils.closeQuietly(in);
        }
        return content;
    }
}
