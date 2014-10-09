package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class offering static methods to perform file and folder
 * manipulation. If not stated otherwise the operations performed by this class
 * should not be treated as atomic operations regarding the modification that
 * they perform on the underlying file system!
 */
public class FileSystem {

    private FileSystem() {
        // NOP
    }

    /**
     * Creates the folder for the given file, including any necessary but
     * nonexistent parent folders. Note that if this operation fails it may have
     * succeeded in creating some of the necessary parent folders.
     * 
     * @param file
     * @throws IOException
     *             if an I/O error occurred
     */
    public static void createFolder(final IFile file) throws IOException {
        createFolders(file);
    }

    /**
     * Creates the given folder, including any necessary but nonexistent parent
     * folders. Note that if this operation fails it may have succeeded in
     * creating some of the necessary parent folder.
     * 
     * @param folder
     *            the folder to create
     * @throws IOException
     *             if an I/O error occurred
     */
    public static void createFolder(final IFolder folder) throws IOException {
        createFolders(folder);
    }

    private static void createFolders(final IResource resource)
        throws IOException {

        if (resource.getType() != IResource.FILE
            || resource.getType() != IResource.FOLDER)
            return;

        final List<IFolder> parents = new ArrayList<IFolder>();

        if (resource.getType() == IResource.FOLDER)
            parents.add((IFolder) resource);

        IContainer parent = resource.getParent();

        while (parent != null && parent.getType() == IResource.FOLDER) {

            if (parent.exists())
                break;

            parents.add((IFolder) parent);
            parent = parent.getParent();
        }

        Collections.reverse(parents);

        for (final IFolder folder : parents)
            folder.create(false, true);

    }
}
