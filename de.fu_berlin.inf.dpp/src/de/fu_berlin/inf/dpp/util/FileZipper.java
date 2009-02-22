package de.fu_berlin.inf.dpp.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.eclipse.core.runtime.Path;

/**
 * This class contains method to create a zip archive out of a list of files.
 * 
 * @author orieger
 * @author oezbek
 * 
 */
public class FileZipper {

    private static Logger logger = Logger.getLogger(FileZipper.class);

    protected static void addFile(String path, File file,
        ZipOutputStream zipStream) throws IOException {

        zipStream.putNextEntry(new ZipEntry(path + Path.SEPARATOR
            + file.getName()));

        IOUtils.copy(new FileInputStream(file), zipStream);

        zipStream.closeEntry();
    }

    protected static void addFolder(String path, File file, ZipOutputStream zos)
        throws Exception {

        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                FileZipper.logger.debug("Compress folder: " + file.getName());
                FileZipper.addFolder(path + Path.SEPARATOR + file.getName(), f,
                    zos);
            } else {
                FileZipper.logger.debug("Compress file : " + file.getName()
                    + " path " + path);
                FileZipper.addFile(path + Path.SEPARATOR + file.getName(), f,
                    zos);
            }
        }
    }

    /**
     * To create a checksum when unzipping one could use
     * 
     * CheckedInputStream cis = new CheckedInputStream(inputStream, new
     * Adler32());
     */
    public static boolean calculateChecksum = false;

    public static void createProjectZipArchive(List<IPath> files,
        String descPath, IProject project) throws Exception {

        OutputStream outputStream = new BufferedOutputStream(
            new FileOutputStream(descPath));

        CheckedOutputStream cos = null;

        if (calculateChecksum) {
            outputStream = cos = new CheckedOutputStream(outputStream,
                new Adler32());
        }

        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

        for (IPath path : files) {
            FileZipper.logger.debug("Compress file: " + path);

            File f = project.getFile(path).getLocation().toFile();
            if (f.exists()) {

                /* create project path in folder structure. */
                String[] structure = path.segments();
                String path_structure = "";
                for (int j = 0; j < structure.length - 1; j++) {
                    String s = structure[j];
                    path_structure += s + Path.SEPARATOR;
                }

                FileZipper.addFile(path_structure, f, zipOutputStream);
            } else {
                throw new FileNotFoundException(path.toString());
            }
        }
        zipOutputStream.close();

        // Checksum
        if (cos != null) {
            FileZipper.logger
                .debug("Checksum: " + cos.getChecksum().getValue());
        }
    }

}
