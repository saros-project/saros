package de.fu_berlin.inf.dpp.core.util;

import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.util.Pair;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class FileUtils {

    private static Logger LOG = Logger.getLogger(FileUtils.class);

    private FileUtils() {
        // no instantiation allowed
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
                    IFile file = (IFile) resource.getAdapter(IFile.class);

                    totalFileSize += file.getSize();
                } catch (IOException e) {
                    LOG.warn("failed to retrieve size of file " + resource, e);
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
}
