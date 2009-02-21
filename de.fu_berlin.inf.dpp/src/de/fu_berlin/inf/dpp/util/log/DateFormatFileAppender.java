/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package de.fu_berlin.inf.dpp.util.log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;

/**
 * DateFormatFileAppender is a log4j Appender and extends {@link FileAppender}
 * so each log is named based on a date format defined in the File property.
 * 
 * Sample File: 'logs/'yyyy/MM-MMM/dd-EEE/HH-mm-ss-S'.log' Makes a file like:
 * logs/2004/04-Apr/13-Tue/09-45-15-937.log
 * 
 * @author James Stauffer
 * 
 *         Adapted from
 *         http://stauffer.james.googlepages.com/DateFormatFileAppender.java
 */
public class DateFormatFileAppender extends FileAppender {

    public DateFormatFileAppender() {
        // Default constructor
    }

    public DateFormatFileAppender(Layout layout, String filename)
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
