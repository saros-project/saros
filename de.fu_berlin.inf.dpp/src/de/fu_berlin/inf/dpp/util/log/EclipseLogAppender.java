package de.fu_berlin.inf.dpp.util.log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;

/**
 * Appender which appends to the Eclipse log of our plug-in
 */
public class EclipseLogAppender extends FileAppender {

    public EclipseLogAppender() {
        // Default constructor
    }

    public EclipseLogAppender(Layout layout, String filename)
        throws IOException {
        super(layout, filename, true);
    }

    protected String fileBackup;

    @Override
    public void setFile(String file) {
        super.setFile(file);
        this.fileBackup = getFile();
    }

    @Override
    public synchronized void setFile(String fileName, boolean append,
        boolean bufferedIO, int bufferSize) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat(fileBackup);
        String actualFileName = sdf.format(new Date());
        makeDirs(actualFileName);
        super.setFile(actualFileName, append, bufferedIO, bufferSize);
    }

    /**
     * Ensures that all of the directories for the given path exist. Anything
     * after the last / or \ is assumed to be a filename.
     */
    protected void makeDirs(String path) {
        int indexSlash = path.lastIndexOf("/");
        int indexBackSlash = path.lastIndexOf("\\");
        int index = Math.max(indexSlash, indexBackSlash);
        if (index > 0) {
            String dirs = path.substring(0, index);
            File dir = new File(dirs);
            if (!dir.exists()) {
                boolean success = dir.mkdirs();
                if (!success) {
                    LogLog.error("Unable to create directories for " + dirs);
                }
            }
        }
    }

}
