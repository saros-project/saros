package de.fu_berlin.inf.dpp.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

// TODO move to negotiation package, no longer an util class
public class FileZipper {

    private static final Logger LOG = Logger.getLogger(FileZipper.class);

    /*
     * Setting this value to high will result in cache misses either by the OS
     * or HDD / SDD controller and slow down performance !
     */
    private static final int BUFFER_SIZE = 32 * 1024;

    /**
     * Creates a Zip archive of all files referenced by their paths. All
     * directories that are included in the path of a file will be stored too
     * unless an alias list is provided. The archive will automatically be
     * deleted if the operation fails or is canceled.
     * 
     * @param files
     *            list of files to compress
     * @param alias
     *            list of alias names/paths for each file entry in the Zip file
     *            or <code>null</code> to use the original names/paths
     * @param archive
     *            the archive file that will contain the compressed content, if
     *            the archive file already exists it will be overwritten
     * @param listener
     *            a {@link ZipListener} which will receive status updates or
     *            <code>null</code>
     * 
     * @cancelable This operation can be canceled via the given listener.
     * 
     * @throws IOException
     *             if an I/O error occurred while creating the archive
     * @throws OperationCanceledException
     *             if the user canceled the operation, see also
     *             {@link ZipListener}
     * 
     */
    public static void createProjectZipArchive(List<IFile> files,
        List<String> alias, File archive, ZipListener listener)
        throws IOException, OperationCanceledException {

        assert files.size() == alias.size();

        long totalSize = 0;

        for (IFile file : files) {

            URI uri = file.getLocationURI();

            if (uri == null)
                continue;

            try {
                totalSize += EFS.getStore(uri).fetchInfo().getLength();
            } catch (CoreException e) {
                LOG.warn("unable to retrieve file size for file: " + file, e);
                continue;
            }
        }

        byte[] buffer = new byte[BUFFER_SIZE];

        OutputStream outputStream = new BufferedOutputStream(
            new FileOutputStream(archive), BUFFER_SIZE);

        ZipOutputStream zipStream = new ZipOutputStream(outputStream);

        boolean cleanup = true;
        boolean isCanceled = false;

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        long totalRead = 0L;

        final Iterator<IFile> fileIt = files.iterator();
        final Iterator<String> aliasIt = alias == null ? null : alias
            .iterator();

        try {
            while (fileIt.hasNext()) {

                IFile file = fileIt.next();

                String entryName = null;

                final String originalEntryName = file.getFullPath().toString();

                if (aliasIt != null && aliasIt.hasNext())
                    entryName = aliasIt.next();

                if (entryName == null)
                    entryName = originalEntryName;

                if (listener != null)
                    isCanceled = listener.update(originalEntryName);

                LOG.trace("compressing file: " + originalEntryName);

                zipStream.putNextEntry(new ZipEntry(entryName));

                InputStream in = null;

                try {

                    int read = 0;

                    try {
                        in = file.getContents();
                    } catch (CoreException e) {
                        throw new IOException("failed to access file "
                            + originalEntryName, e);
                    }

                    while ((read = in.read(buffer)) > 0) {

                        if (isCanceled)
                            throw new OperationCanceledException(
                                "compressing of file '" + entryName
                                    + "' was canceled");

                        zipStream.write(buffer, 0, read);

                        totalRead += read;

                        if (listener != null)
                            listener.update(totalRead, totalSize);

                    }
                } finally {
                    IOUtils.closeQuietly(in);
                }
                zipStream.closeEntry();
            }

            zipStream.finish();
            cleanup = false;
        } finally {
            IOUtils.closeQuietly(zipStream);
            if (cleanup && archive != null && archive.exists()
                && !archive.delete())
                LOG.warn("could not delete archive file: " + archive);
        }

        stopWatch.stop();

        LOG.debug(String.format("created archive %s I/O: [%s]",
            archive.getAbsolutePath(),
            CoreUtils.throughput(archive.length(), stopWatch.getTime())));

    }

}
