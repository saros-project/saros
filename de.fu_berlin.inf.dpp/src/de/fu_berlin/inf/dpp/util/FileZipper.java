package de.fu_berlin.inf.dpp.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;

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
     * @throws ZipException
     *             if empty list of files given
     */
    public static void createProjectZipArchive(List<IPath> files, File archive,
        IProject project, SubMonitor progress) throws IOException,
        SarosCancellationException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

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
            IFile file = project.getFile(path);

            try {
                zipSingleFile(new WrappedIFile(file), path.toPortableString(),
                    zipStream, progress.newChild(1));
            } catch (SarosCancellationException e) {
                cleanup(archive);
                throw e;
            } catch (IllegalArgumentException e) {
                cleanup(archive);
                throw e;
            } catch (IOException e) {
                cleanup(archive);
                throw e;
            }
        }
        zipStream.close();

        // Checksum
        if (calculateChecksum && cos != null) {
            FileZipper.log.debug("Checksum: " + cos.getChecksum().getValue());
        }
        stopWatch.stop();

        log.debug(String.format("Created project archive %s at %s", stopWatch
            .throughput(archive.length()), archive.getAbsolutePath()));

        progress.done();
    }

    public static void cleanup(File archive) {
        if (archive != null && archive.exists() && !archive.delete()) {
            log.warn("Could not delete archive file: " + archive);
        }
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
     *             deleted and an SarosCancellationException is thrown
     * @throws IOException
     *             if an error occurred while trying to zip a file. The archive
     *             is then deleted.
     * @throws IllegalArgumentException
     *             if the list of files is empty. The archive is then deleted.
     */
    public static void zipFiles(List<File> files, File archive,
        SubMonitor progress) throws IOException, SarosCancellationException {
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
                    zipSingleFile(new WrappedFile(file), file.getName(),
                        zipStream, progress.newChild(1));
                    ++filesZipped;
                } catch (SarosCancellationException e) {
                    cleanup(archive);
                    throw e;
                } catch (IllegalArgumentException e) {
                    log.warn(e.getMessage());
                    continue;
                } catch (IOException e) {
                    cleanup(archive);
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
     * Adds the given file to the ZipStream. The file might be a {@link File} or
     * an {@link IFile} which is concealed by the {@link FileWrapper} interface
     * 
     * @param filename
     *            the name of the file that should be added to the archive. It
     *            might as well be a relative path name. In this case the
     *            specified subdirectories are automatically created inside the
     *            archive.
     * @cancelable
     * @throws IOException
     *             if an error occurred while trying to zip the file
     * @throws IllegalArgumentException
     *             if the file was null or a directory or didn't exist
     */
    protected static void zipSingleFile(FileWrapper file, String filename,
        ZipOutputStream zipStream, SubMonitor progress) throws IOException,
        SarosCancellationException {

        try {

            if (progress.isCanceled()) {
                throw new SarosCancellationException();
            }

            progress.beginTask("Compressing: " + filename, 1);
            log.debug("Compress file: " + filename);

            if (file == null || !file.exists()) {
                throw new IllegalArgumentException(
                    "The file to zip does not exist: " + filename);
            }

            zipStream.putNextEntry(new ZipEntry(filename));
            writeFileToStream(file, filename, zipStream);
            zipStream.closeEntry();

        } finally {
            progress.done();
        }
    }

    protected static void writeFileToStream(FileWrapper file, String filename,
        ZipOutputStream zipStream) throws CausedIOException, IOException {
        InputStream in;
        try {
            in = file.getInputStream();
        } catch (Exception e) {
            throw new CausedIOException("Could not obtain InputStream for "
                + filename, e);
        }

        try {
            IOUtils.copy(in, zipStream);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * An interface that allows us to use different file classes.
     * 
     * @see WrappedFile
     * @see WrappedIFile
     */
    interface FileWrapper {
        public boolean exists();

        public InputStream getInputStream() throws FileNotFoundException,
            CoreException;

        public String getName();
    }

    /**
     * A class that wraps a {@link File}.
     */
    static class WrappedFile implements FileWrapper {
        protected File file;

        public WrappedFile(File file) {
            this.file = file;
        }

        public boolean exists() {
            return (file).exists();
        }

        public InputStream getInputStream() throws FileNotFoundException {
            return new BufferedInputStream(new FileInputStream(file));
        }

        public String getName() {
            return file.getName();
        }
    }

    /**
     * A class that wraps an {@link IFile}.
     */
    static class WrappedIFile implements FileWrapper {
        protected IFile file;

        public WrappedIFile(IFile file) {
            this.file = file;
        }

        public boolean exists() {
            return file.exists();
        }

        public InputStream getInputStream() throws CoreException {
            return file.getContents();
        }

        public String getName() {
            return file.getName();
        }
    }

}
