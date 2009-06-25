package de.fu_berlin.inf.dpp.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
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
     *             if the list of files contains a directory
     */
    public static void createProjectZipArchive(List<IPath> files, File archive,
        IProject project, SubMonitor progress) throws IOException,
        OperationCanceledException {

        long time = System.currentTimeMillis();

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

            if (progress.isCanceled()) {
                archive.delete();
                throw new OperationCanceledException();
            }

            progress.subTask("Compressing: " + path);
            log.debug("Compress file: " + path);

            File file = project.getFile(path).getLocation().toFile();

            if (file.isDirectory()) {
                archive.delete();
                throw new IllegalArgumentException(
                    "Zipping directories is not supported: " + path);
            }

            if (file.exists()) {
                zipStream.putNextEntry(new ZipEntry(path.toPortableString()));

                IOUtils.copy(new FileInputStream(file), zipStream);

                zipStream.closeEntry();
            } else {
                log.warn("File given to Zip which does not exist: " + path);
            }
            progress.worked(1);
        }
        zipStream.close();

        // Checksum
        if (calculateChecksum && cos != null) {
            FileZipper.log.debug("Checksum: " + cos.getChecksum().getValue());
        }

        log.debug(String.format("Created project archive in %d s (%d KB): %s",
            (System.currentTimeMillis() - time) / 1000,
            archive.length() / 1024, archive.getAbsolutePath()));

        progress.done();
    }
}
