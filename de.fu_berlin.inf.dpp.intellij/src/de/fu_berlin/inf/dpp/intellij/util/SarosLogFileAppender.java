package de.fu_berlin.inf.dpp.intellij.util;

import com.intellij.openapi.application.PathManager;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.FileAppender;
import org.apache.log4j.helpers.LogLog;

/**
 * SarosLogFileAppender is used to create log files that contain information on the current date and
 * time in the file name in the JetBrains IDE log folder.
 *
 * <p>It is constructed with a filePattern, that is formatted by {@link
 * SimpleDateFormat#format(Date)}.
 *
 * <p>For example: '/SarosLogs/Saros_'yyyy-MM-dd_HH-mm-ss'.log' becomes
 * $LOG_DIR$/SarosLogs/Saros_2014-10-01-10:00:01.log
 *
 * <p>where $LOG_DIR$ is the log folder according to {@link PathManager#getLogPath()}.
 */
public class SarosLogFileAppender extends FileAppender {

  @Override
  public void setFile(String filePattern) {
    String cleanedFilePattern = filePattern;
    if (filePattern.startsWith("'")) {
      cleanedFilePattern = filePattern.substring(1);
    }
    String formattedFileName = getFormattedFileName(cleanedFilePattern);
    makeDirs(formattedFileName);

    super.setFile(formattedFileName);
  }

  private String getFormattedFileName(String filePattern) {
    String file = "'" + PathManager.getLogPath() + filePattern;
    SimpleDateFormat sdf = new SimpleDateFormat(file);
    return sdf.format(new Date());
  }

  /**
   * Ensures that all of the directories for the given path exist. Anything after the last slash is
   * assumed to be a filename.
   */
  private void makeDirs(String path) {
    int index = path.lastIndexOf("/");
    if (index > 0) {
      File logDir = new File(path.substring(0, index));
      if (!logDir.exists()) {
        if (!logDir.mkdirs()) {
          LogLog.error("Error creating directories for " + path);
        }
      }
    }
  }
}
