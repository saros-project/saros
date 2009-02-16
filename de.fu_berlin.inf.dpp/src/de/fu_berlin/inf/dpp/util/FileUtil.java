package de.fu_berlin.inf.dpp.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import de.fu_berlin.inf.dpp.Saros;

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
        InputStream contents = null;

        try {
            // Adler-32 checksum
            contents = file.getContents();
            CheckedInputStream cis = new CheckedInputStream(contents,
                new Adler32());

            byte[] tempBuf = new byte[128];
            while (cis.read(tempBuf) >= 0) {
                // continue until buffer empty
            }
            long checksum = cis.getChecksum().getValue();
            return new Long(checksum);

        } catch (IOException e) {
            e.printStackTrace();

        } catch (CoreException e) {
            e.printStackTrace();

        } finally {
            try {
                if (contents != null) {
                    contents.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new Long(-1);
    }

    /**
     * Sets or unsets the given resource as read-only in the file system.
     * 
     * @param file
     *            the resource which readonly attribute are set/unset
     * @param readOnly
     *            <code>true</code> to set it to read-only, <code>false</code>
     *            to unset
     */
    public static void setReadOnly(IFile file, boolean readonly) {
        ResourceAttributes attributes = file.getResourceAttributes();
        if (attributes == null)
            return;
        attributes.setReadOnly(readonly);
        try {
            file.setResourceAttributes(attributes);
        } catch (CoreException e) {
            // failure is not an option
        }
    }

    /**
     * Writes the given input stream to the given file.
     * 
     * @param input
     *            the input stream
     * @param file
     *            the file to create/overwrite
     */
    public static void writeFile(InputStream input, IFile file) {
        setReadOnly(file, false);

        try {
            if (file.exists()) {
                file.setContents(input, IResource.FORCE, null);
            } else {
                file.create(input, true, new NullProgressMonitor());
            }
        } catch (CoreException e) {
            log.error("Could not write file", e);
        }

        setReadOnly(file, !Saros.getDefault().getSessionManager()
            .getSharedProject().isDriver());
    }

}
