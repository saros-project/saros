package de.fu_berlin.inf.dpp.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * This class contains method to create a zip archive out of a list of files.
 * 
 * @author orieger
 * @author oezbek
 * 
 */
public class FileZipper {

    private static final Logger log = Logger.getLogger(FileZipper.class);

    /*
     * Setting this value to high will result in cache misses either by the OS
     * or HDD / SDD controller and slow down performance !
     */
    private static final int BUFFER_SIZE = 32 * 1024;

    // this method does not create a Zip file !

    /**
     * Creates a Zip archive of all files referenced by their paths. The paths
     * must be relative to the project the files belong to. All directories that
     * are included in the path of a file will be stored too. The archive will
     * automatically be deleted if the operation fails or is canceled.
     * 
     * @param project
     *            an Eclipse project
     * @param paths
     *            the paths of the files relative to the project that should be
     *            compressed and archived
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
    public static void createProjectZipArchive(IProject project,
        List<String> paths, File archive, ZipListener listener)
        throws IOException, OperationCanceledException {

        long totalFileSizes = 0;

        List<FileWrapper> filesToZip = new ArrayList<FileWrapper>(paths.size());

        for (String path : paths) {
            IFile file = project.getFile(path);
            IPath fileSystemPath = file.getLocation();
            if (fileSystemPath != null)
                totalFileSizes += fileSystemPath.toFile().length();

            filesToZip.add(new EclipseFileWrapper(file));
        }

        internalZipFiles(filesToZip, archive, true, true, totalFileSizes,
            listener);
    }

    /**
     * Creates a Zip archive containing all files of the given list. Only files
     * are included <b>without</b> their directory names. The archive will
     * automatically be deleted if the operation fails or is canceled.
     * 
     * @param files
     *            the file that should be included in the archive
     * @param archive
     *            the archive file that will contain the content, if the archive
     *            file already exists it will be overwritten
     * @param compress
     *            <code>true</code> if the content should be compressed or
     *            <code>false</code> if it should only be stored
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
    public static void zipFiles(List<File> files, File archive,
        boolean compress, ZipListener listener) throws IOException,
        OperationCanceledException {
        List<FileWrapper> filesToZip = new ArrayList<FileWrapper>(files.size());

        for (File file : files)
            if (file.isFile()) {
                filesToZip.add(new JavaFileWrapper(file));
            }

        internalZipFiles(filesToZip, archive, compress, false, -1L, listener);
    }

    private static void internalZipFiles(List<FileWrapper> files, File archive,
        boolean compress, boolean includeDirectories, long totalSize,
        ZipListener listener) throws IOException, OperationCanceledException {

        byte[] buffer = new byte[BUFFER_SIZE];

        OutputStream outputStream = new BufferedOutputStream(
            new FileOutputStream(archive), BUFFER_SIZE);

        ZipOutputStream zipStream = new ZipOutputStream(outputStream);

        zipStream.setLevel(compress ? Deflater.DEFAULT_COMPRESSION
            : Deflater.NO_COMPRESSION);

        boolean cleanup = true;
        boolean isCanceled = false;

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        long totalRead = 0L;

        try {
            for (FileWrapper file : files) {
                String entryName = includeDirectories ? file.getPath() : file
                    .getName();

                if (listener != null)
                    isCanceled = listener.update(file.getPath());

                log.trace("compressing file: " + entryName);

                zipStream.putNextEntry(new ZipEntry(entryName));

                InputStream in = null;

                try {
                    int read = 0;
                    in = file.getInputStream();
                    while (-1 != (read = in.read(buffer))) {

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
            cleanup = false;
        } finally {
            IOUtils.closeQuietly(zipStream);
            if (cleanup && archive != null && archive.exists()
                && !archive.delete())
                log.warn("could not delete archive file: " + archive);
        }

        stopWatch.stop();

        log.debug(String.format("created archive %s I/O: [%s]",
            archive.getAbsolutePath(),
            Utils.throughput(archive.length(), stopWatch.getTime())));

    }

    interface FileWrapper {
        public boolean exists();

        public InputStream getInputStream() throws IOException;

        public String getName();

        public String getPath();
    }

    private static class JavaFileWrapper implements FileWrapper {
        protected File file;

        public JavaFileWrapper(File file) {
            this.file = file;
        }

        @Override
        public boolean exists() {
            return (file).exists();
        }

        @Override
        public InputStream getInputStream() throws FileNotFoundException {
            return new FileInputStream(file);
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public String getPath() {
            return file.getPath().replace('\\', '/');
        }
    }

    private static class EclipseFileWrapper implements FileWrapper {
        protected IFile file;

        public EclipseFileWrapper(IFile file) {
            this.file = file;
        }

        @Override
        public boolean exists() {
            return file.exists();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            try {
                return file.getContents();
            } catch (CoreException e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public String getPath() {
            return file.getProjectRelativePath().toPortableString();
        }
    }
}
