package de.fu_berlin.inf.dpp.feedback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * The FileSubmitter class provides static methods to upload a file to a server.
 * 
 * @author Lisa Dohrmann
 */
public class FileSubmitter {

    private static final Logger log = Logger.getLogger(FileSubmitter.class
        .getName());

    private static final String STATISTIC_UPLOAD_URL = System
        .getProperty("de.fu_berlin.inf.dpp.feedback.STATISTIC_UPLOAD_URL");

    private static final String ERROR_LOG_UPLOAD_URL = System
        .getProperty("de.fu_berlin.inf.dpp.feedback.ERROR_LOG_UPLOAD_URL");

    /** Value for connection timeout */
    private static final int TIMEOUT = 30000;

    /**
     * Convenience wrapper method to upload a statistic file to the server
     * 
     * @param file
     *            the file to be uploaded
     * @throws IOException
     *             is thrown if the upload failed; the exception wraps the
     *             target exception that contains the main cause for the failure
     * 
     * @blocking
     */
    public static void uploadStatisticFile(File file, IProgressMonitor monitor)
        throws IOException {

        if (STATISTIC_UPLOAD_URL == null) {
            log.warn("statistic upload url is not configured, cannot upload statistic file");
            return;
        }

        uploadFile(file, STATISTIC_UPLOAD_URL, monitor);
    }

    /**
     * Convenience wrapper method to upload an error log file to the server. To
     * save time and storage space, the log is compressed to a zip archive with
     * the given zipName. The zip is created in the given zipLocation and
     * deleted when the virtual machine terminates.
     * 
     * @param zipLocation
     *            the location where the zip file should be created
     * @param zipName
     *            a name for the zip archive, e.g. with added user ID to make it
     *            unique, zipName must be at least 3 characters long!
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void uploadErrorLog(String zipLocation, String zipName,
        File file, IProgressMonitor monitor) throws IOException {

        if (ERROR_LOG_UPLOAD_URL == null) {
            log.warn("error log upload url is not configured, cannot upload error log file");
            return;
        }

        File archive = new File(zipLocation, zipName + ".zip");

        ZipOutputStream out = null;
        FileInputStream in = null;

        byte[] buffer = new byte[8192];

        try {

            in = new FileInputStream(file);

            ZipOutputStream zipStream = new ZipOutputStream(
                new FileOutputStream(archive));

            zipStream.putNextEntry(new ZipEntry(file.getName()));

            int read;

            while ((read = in.read(buffer)) > 0)
                zipStream.write(buffer, 0, read);

            zipStream.finish();
            zipStream.close();

            uploadFile(archive, ERROR_LOG_UPLOAD_URL, monitor);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
            archive.delete();
        }
    }

    /**
     * Tries to upload the given file to the given server (via POST method). A
     * different name under which the file should be processed by the server can
     * be specified.
     * 
     * @param file
     *            the file to upload
     * @param url
     *            the URL of the server, that is supposed to handle the file
     * @param monitor
     *            a monitor to report progress to
     * @throws IOException
     *             if an I/O error occurs
     * 
     * @blocking
     * @cancelable
     */

    private static void uploadFile(File file, String url,
        IProgressMonitor monitor) throws IOException {

        final String CRLF = "\r\n";
        final String doubleDash = "--";
        final String boundary = generateBoundary();

        HttpURLConnection connection = null;
        OutputStream urlConnectionOut = null;
        FileInputStream fileIn = null;

        if (monitor == null)
            monitor = new NullProgressMonitor();

        int contentLength = (int) file.length();

        if (contentLength == 0) {
            log.warn("file size of file " + file.getAbsolutePath()
                + " is 0 or the file does not exist");
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

            connection.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);

            String contentDispositionLine = "Content-Disposition: form-data; name=\""
                + file.getName()
                + "\"; filename=\""
                + file.getName()
                + "\""
                + CRLF;

            String contentTypeLine = "Content-Type: application/octet-stream; charset=ISO-8859-1"
                + CRLF;

            String contentTransferEncoding = "Content-Transfer-Encoding: binary"
                + CRLF;

            contentLength += 2 * boundary.length()
                + contentDispositionLine.length() + contentTypeLine.length()
                + contentTransferEncoding.length() + 4 * CRLF.length() + 3
                * doubleDash.length();

            connection.setFixedLengthStreamingMode(contentLength);

            connection.connect();

            urlConnectionOut = connection.getOutputStream();

            PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                urlConnectionOut, "US-ASCII"), true);

            writer.append(doubleDash).append(boundary).append(CRLF);
            writer.append(contentDispositionLine);
            writer.append(contentTypeLine);
            writer.append(contentTransferEncoding);
            writer.append(CRLF);
            writer.flush();

            fileIn = new FileInputStream(file);
            byte[] buffer = new byte[8192];

            for (int read = 0; (read = fileIn.read(buffer)) > 0;) {
                if (monitor.isCanceled())
                    return;

                urlConnectionOut.write(buffer, 0, read);
                monitor.worked(read);
            }

            urlConnectionOut.flush();

            writer.append(CRLF);
            writer.append(doubleDash).append(boundary).append(doubleDash)
                .append(CRLF);
            writer.close();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                log.debug("uploaded file " + file.getAbsolutePath() + " to "
                    + connectionURL.getHost());
                return;
            }

            throw new IOException("failed to upload file "
                + file.getAbsolutePath() + connectionURL.getHost() + " ["
                + connection.getResponseMessage() + "]");
        } finally {
            IOUtils.closeQuietly(fileIn);
            IOUtils.closeQuietly(urlConnectionOut);

            if (connection != null)
                connection.disconnect();

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