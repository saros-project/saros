package de.fu_berlin.inf.dpp.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

/**
 * This class contains method to create a zip archive out of a list of files.
 * 
 * @author orieger
 * @author oezbek
 * 
 */
public class FileZipper {

    private static Logger logger = Logger.getLogger(FileZipper.class);

    /**
     * To create a checksum when unzipping one could use
     * 
     * CheckedInputStream cis = new CheckedInputStream(inputStream, new
     * Adler32());
     */
    public final static boolean calculateChecksum = false;

    public static void createProjectZipArchive(List<IPath> files,
        String descPath, IProject project) throws Exception {

        OutputStream outputStream = new BufferedOutputStream(
            new FileOutputStream(descPath));

        CheckedOutputStream cos = null;

        if (calculateChecksum) {
            outputStream = cos = new CheckedOutputStream(outputStream,
                new Adler32());
        }

        ZipOutputStream zipStream = new ZipOutputStream(outputStream);

        for (IPath path : files) {

            FileZipper.logger.debug("Compress file: " + path);

            File file = project.getFile(path).getLocation().toFile();
            if (file.exists()) {
                zipStream.putNextEntry(new ZipEntry(path.toPortableString()));

                IOUtils.copy(new FileInputStream(file), zipStream);

                zipStream.closeEntry();
            } else {
                FileZipper.logger
                    .warn("File given to Zip which does not exist: " + path);
            }
        }
        zipStream.close();

        // Checksum
        if (calculateChecksum && cos != null) {
            FileZipper.logger
                .debug("Checksum: " + cos.getChecksum().getValue());
        }
    }

}
