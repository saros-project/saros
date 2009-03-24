package de.fu_berlin.inf.dpp.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;

/**
 * This class contains static utility methods for file handling.
 * 
 * @author orieger/chjacob
 * 
 */
public class FileUtil {

    private static Logger log = Logger.getLogger(FileUtil.class);

    /**
     * calculate checksum for given file
     * 
     * @param file
     * @return checksum of file or -1 if checksum calculation has been failed.
     */
    public static Long checksum(IFile file) {

        // Adler-32 checksum
        InputStream contents;
        try {
            contents = file.getContents();
        } catch (CoreException e) {
            log.error("Failed to calculate checksum:", e);
            return -1L;
        }

        CheckedInputStream cis = new CheckedInputStream(contents, new Adler32());
        InputStream in = new BufferedInputStream(cis);
        byte[] tempBuf = new byte[8192];
        try {
            while (in.read(tempBuf) >= 0) {
                // continue until buffer empty
            }
            return Long.valueOf(cis.getChecksum().getValue());
        } catch (IOException e) {
            log.error("Failed to calculate checksum:", e);
        } finally {
            IOUtils.closeQuietly(in);
        }

        return -1L;
    }

    /**
     * Sets or unsets the given resource as read-only in the file system.
     * 
     * @param file
     *            the resource which readonly attribute are set/unset
     * @param readOnly
     *            <code>true</code> to set it to read-only, <code>false</code>
     *            to unset
     * @return The state before setting read-only to the given value.
     */
    public static boolean setReadOnly(IFile file, boolean readonly) {
        ResourceAttributes attributes = file.getResourceAttributes();
        if (attributes == null) {
            // TODO: Throw an FileNotFoundException and deal with it everywhere!
            log.warn("File does not exist for setting readonly == " + readonly
                + ": " + file);
            return false;
        }
        boolean result = attributes.isReadOnly();
        attributes.setReadOnly(readonly);
        try {
            file.setResourceAttributes(attributes);
        } catch (CoreException e) {
            // failure is not an option
            log.warn("Failed to set resource readonly == " + readonly + ": "
                + file);
        }
        return result;
    }

    /**
     * Writes the given input stream to the given file.
     * 
     * This operation will unset a possible readOnly flag and reset if after the
     * operation.
     * 
     * @param input
     *            the input stream
     * @param file
     *            the file to create/overwrite
     */
    public static void writeFile(InputStream input, IFile file) {

        boolean wasReadOnly = false;
        if (file.isReadOnly()) {
            wasReadOnly = true;
            setReadOnly(file, false);
        }

        try {
            BlockingProgressMonitor monitor = new BlockingProgressMonitor();
            if (file.exists()) {
                file.setContents(input, IResource.FORCE, monitor);
            } else {
                file.create(input, true, monitor);
            }
            try {
                monitor.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (CoreException e) {
            log.error("Could not write file", e);
        }

        if (wasReadOnly)
            setReadOnly(file, wasReadOnly);
    }

}
