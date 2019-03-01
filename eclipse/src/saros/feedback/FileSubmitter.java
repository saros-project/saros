package saros.feedback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/** The <code>FileSubmitter</code> provides the functionality to upload a file to a HTTP server. */
public class FileSubmitter {

  private static final Logger log = Logger.getLogger(FileSubmitter.class);

  /** Value for connection timeout */
  private static final int TIMEOUT = 30000;

  private FileSubmitter() {
    // NOP
  }

  /**
   * Tries to upload the given file to the given HTTP server (via POST method).
   *
   * @param file the file to upload
   * @param url the URL of the server, that is supposed to handle the file
   * @param monitor a monitor to report progress to
   * @throws IOException if an I/O error occurs
   */
  public static void uploadFile(final File file, final String url, IProgressMonitor monitor)
      throws IOException {

    final String CRLF = "\r\n";
    final String doubleDash = "--";
    final String boundary = generateBoundary();

    HttpURLConnection connection = null;
    OutputStream urlConnectionOut = null;
    FileInputStream fileIn = null;

    if (monitor == null) monitor = new NullProgressMonitor();

    int contentLength = (int) file.length();

    if (contentLength == 0) {
      log.warn("file size of file " + file.getAbsolutePath() + " is 0 or the file does not exist");
      return;
    }

    monitor.beginTask("Uploading file " + file.getName(), contentLength);

    try {
      URL connectionURL = new URL(url);

      if (!"http".equals(connectionURL.getProtocol()))
        throw new IOException("only HTTP protocol is supported");

      connection = (HttpURLConnection) connectionURL.openConnection();
      connection.setUseCaches(false);
      connection.setDoInput(true);
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setReadTimeout(TIMEOUT);
      connection.setConnectTimeout(TIMEOUT);

      connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

      String contentDispositionLine =
          "Content-Disposition: form-data; name=\""
              + file.getName()
              + "\"; filename=\""
              + file.getName()
              + "\""
              + CRLF;

      String contentTypeLine = "Content-Type: application/octet-stream; charset=ISO-8859-1" + CRLF;

      String contentTransferEncoding = "Content-Transfer-Encoding: binary" + CRLF;

      contentLength +=
          2 * boundary.length()
              + contentDispositionLine.length()
              + contentTypeLine.length()
              + contentTransferEncoding.length()
              + 4 * CRLF.length()
              + 3 * doubleDash.length();

      connection.setFixedLengthStreamingMode(contentLength);

      connection.connect();

      urlConnectionOut = connection.getOutputStream();

      PrintWriter writer =
          new PrintWriter(new OutputStreamWriter(urlConnectionOut, "US-ASCII"), true);

      writer.append(doubleDash).append(boundary).append(CRLF);
      writer.append(contentDispositionLine);
      writer.append(contentTypeLine);
      writer.append(contentTransferEncoding);
      writer.append(CRLF);
      writer.flush();

      fileIn = new FileInputStream(file);
      byte[] buffer = new byte[8192];

      for (int read = 0; (read = fileIn.read(buffer)) > 0; ) {
        if (monitor.isCanceled()) return;

        urlConnectionOut.write(buffer, 0, read);
        monitor.worked(read);
      }

      urlConnectionOut.flush();

      writer.append(CRLF);
      writer.append(doubleDash).append(boundary).append(doubleDash).append(CRLF);
      writer.close();

      if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
        log.debug("uploaded file " + file.getAbsolutePath() + " to " + connectionURL.getHost());
        return;
      }

      throw new IOException(
          "failed to upload file "
              + file.getAbsolutePath()
              + connectionURL.getHost()
              + " ["
              + connection.getResponseMessage()
              + "]");
    } finally {
      IOUtils.closeQuietly(fileIn);
      IOUtils.closeQuietly(urlConnectionOut);

      if (connection != null) connection.disconnect();

      monitor.done();
    }
  }

  private static String generateBoundary() {
    Random random = new Random();
    return Long.toHexString(random.nextLong())
        + Long.toHexString(random.nextLong())
        + Long.toHexString(random.nextLong());
  }
}
