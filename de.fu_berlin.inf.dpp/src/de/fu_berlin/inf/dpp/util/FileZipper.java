package de.fu_berlin.inf.dpp.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.SubMonitor;

/**
 * This class contains method to create a zip archive out of a list of files.
 * 
 * @author orieger
 * @author oezbek
 * 
 */
public class FileZipper {

    private static Logger log = Logger.getLogger(FileZipper.class);

    /**
     * To create a checksum when unzipping one could use
     * 
     * CheckedInputStream cis = new CheckedInputStream(inputStream, new
     * Adler32());
     */
    public final static boolean calculateChecksum = false;

    /**
     * Given a list of *files* belonging to the given project, this method will
     * create a zip file at the given archive location (overwriting any existing
     * content).
     * 
     * Progress is reported coarsely to the progress monitor.
     * 
     * @cancelable This operation can be canceled via the given progress
     *             monitor. If the operation was canceled the zip file is
     *             deleted prior to throwing an OperationCanceledException.
     * 
     * @throws IllegalArgumentException
     *             if the list of files contains a directory or a nonexistent
     *             file. The archive is then deleted
     * @throws IOException
     *             if an error occurred while trying to zip a file. The archive
     *             is then deleted.
     */
    public static void createProjectZipArchive(List<IPath> files, File archive,
        IProject project, SubMonitor progress) throws IOException {

        StoppWatch stoppWatch = new StoppWatch();
        stoppWatch.start();

        progress.beginTask("Creating Archive", files.size());

        OutputStream outputStream = new BufferedOutputStream(
            new FileOutputStream(archive));

        CheckedOutputStream cos = null;

        if (calculateChecksum) {
            outputStream = cos = new CheckedOutputStream(outputStream,
                new Adler32());
        }

        ZipOutputStream zipStream = new ZipOutputStream(outputStream);

        for (IPath path : files) {
            File file = project.getFile(path).getLocation().toFile();

            try {
                zipSingleFile(file, path.toPortableString(), zipStream,
                    progress.newChild(1));
            } catch (CancellationException e) {
                archive.delete();
                throw e;
            } catch (IllegalArgumentException e) {
                archive.delete();
                throw e;
            } catch (IOException e) {
                archive.delete();
                throw e;
            }
        }
        zipStream.close();

        // Checksum
        if (calculateChecksum && cos != null) {
            FileZipper.log.debug("Checksum: " + cos.getChecksum().getValue());
        }
        stoppWatch.stop();

        log.debug(String.format("Created project archive %s at %s", stoppWatch
            .throughput(archive.length()), archive.getAbsolutePath()));

        progress.done();
    }

    /**
     * Given a list of files this method will create a zip file at the given
     * archive location (overwriting any existing content). The archive will
     * contain all given files at top level, i.e. subfolders are not created. <br>
     * If the list of files contains directories or nonexistent files, they are
     * ignored. Therefore the archive might be empty at the end.
     * 
     * @blocking
     * @cancelable This operation can be canceled via the given progress
     *             monitor. If the operation was canceled, the archive file is
     *             deleted and an CancellationException is thrown
     * @throws IOException
     *             if an error occurred while trying to zip a file. The archive
     *             is then deleted.
     * @throws IllegalArgumentException
     *             if the list of files is empty. The archive is then deleted.
     */
    public static void zipFiles(List<File> files, File archive,
        SubMonitor progress) throws IOException {
        try {
            if (files.isEmpty()) {
                log.warn("The list with files to zip was empty.");
                return;
            }

            progress.beginTask("Creating Archive", files.size());

            OutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(archive));
            ZipOutputStream zipStream = new ZipOutputStream(outputStream);
            int filesZipped = 0;

            for (File file : files) {
                try {
                    zipSingleFile(file, file.getName(), zipStream, progress
                        .newChild(1));
                    ++filesZipped;
                } catch (CancellationException e) {
                    archive.delete();
                    throw e;
                } catch (IllegalArgumentException e) {
                    log.warn(e.getMessage());
                    continue;
                } catch (IOException e) {
                    archive.delete();
                    throw e;
                }
            }
            zipStream.close();
            if (filesZipped == 0) {
                log.warn("No files could be added to the archive.");
            }
        } finally {
            progress.done();
        }
    }

    /**
     * Adds the given file to the ZipStream.
     * 
     * @filename the name of the file that should be added to the archive. It
     *           might as well be a relative path name. In this case the
     *           specified subdirectories are automatically created inside the
     *           archive.
     * @cancelable
     * @throws IOException
     *             if an error occurred while trying to zip the file
     * @throws IllegalArgumentException
     *             if the file was a directory or didn't exist
     */
    protected static void zipSingleFile(File file, String filename,
        ZipOutputStream zipStream, SubMonitor progress) throws IOException {
        try {

            if (progress.isCanceled()) {
                throw new CancellationException();
            }

            progress.beginTask("Compressing: " + filename, 1);
            log.debug("Compress file: " + filename);

            if (file.isDirectory()) {
                throw new IllegalArgumentException(
                    "Zipping directories is not supported: " + filename);
            }

            if (file.exists()) {
                zipStream.putNextEntry(new ZipEntry(filename));
                IOUtils.copy(new FileInputStream(file), zipStream);
                zipStream.closeEntry();
            } else {
                throw new IllegalArgumentException(
                    "The file to zip does not exist: " + filename);
            }

        } finally {
            progress.done();
        }
    }
}
