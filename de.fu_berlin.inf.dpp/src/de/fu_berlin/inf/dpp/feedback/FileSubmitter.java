package de.fu_berlin.inf.dpp.feedback;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CancellationException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.util.CausedIOException;
import de.fu_berlin.inf.dpp.util.FileZipper;

/**
 * The FileSubmitter class provides static methods to upload a file to a server.
 * 
 * @author Lisa Dohrmann
 */
public class FileSubmitter {

    protected static final Logger log = Logger.getLogger(FileSubmitter.class
        .getName());

    /** the temporary URL of our Apache Tomcat server */
    public static final String SERVER_URL_TEMP = "http://brazzaville.imp.fu-berlin.de:5900/";
    /** the URL of our Apache Tomcat server */
    public static final String SERVER_URL = "https://projects.mi.fu-berlin.de/saros/";
    /** the name of the Servlet that is supposed to handle the upload */
    public static final String SERVLET_NAME = "SarosStatisticServer/fileupload";

    protected static final String STATISTIC_ID_PARAM = "?id=1";
    protected static final String ERROR_LOG_ID_PARAM = "?id=2";

    /** Value for connection timeout */
    protected static final int TIMEOUT = 30000;

    /**
     * Convenience wrapper method for
     * {@link #uploadFile(File, String, SubMonitor)}. <br>
     * The statistic file is first tried to upload to the server specified by
     * {@link #SERVER_URL} and then to our temporary server
     * {@link #SERVER_URL_TEMP}.
     * 
     * @param file
     *            the file to upload
     * @throws IOException
     *             is thrown, if the upload failed; the exception wraps the
     *             target exception that contains the main cause for the failure
     * 
     * @blocking
     */
    public static void uploadStatisticFile(File file, SubMonitor monitor)
        throws IOException {

        monitor.beginTask("Upload statistic file...", 2);

        try {
            try {
                /*
                 * TODO this first call is expected to fail at the moment,
                 * because the tomcat server isn't yet installed on
                 * projects.mi.fu-berlin.de/saros
                 */
                uploadFile(file,
                    SERVER_URL + SERVLET_NAME + STATISTIC_ID_PARAM, monitor
                        .newChild(1));
                return;
            } catch (IOException e) {
                log.debug(String.format(
                    "Because the real server is not running right now, "
                        + "the following message is expected: %s. %s", e
                        .getMessage(), e.getCause().getMessage()));
            }
            uploadFile(file, SERVER_URL_TEMP + SERVLET_NAME
                + STATISTIC_ID_PARAM, monitor.newChild(1));
        } finally {
            monitor.done();
        }
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
     * @throws SarosCancellationException
     */
    public static void uploadErrorLog(String zipLocation, String zipName,
        File file, SubMonitor monitor) throws IOException,
        SarosCancellationException {
        monitor.beginTask("Upload error log...", 3);

        try {
            File archive = new File(zipLocation, zipName + ".zip");
            archive.deleteOnExit();

            monitor.worked(1);

            // zip the file before uploading it
            FileZipper.zipFiles(Collections.singletonList(file), archive,
                monitor.newChild(1));

            uploadFile(archive, SERVER_URL_TEMP + SERVLET_NAME
                + ERROR_LOG_ID_PARAM, monitor.newChild(1));
        } finally {
            monitor.done();
        }
    }

    /**
     * Tries to upload the given file to the given server (via POST method). A
     * different name under which the file should be processed by the server can
     * be specified.
     * 
     * @param file
     *            the file to upload
     * @param server
     *            the URL of the server, that is supposed to handle the file
     * @param monitor
     *            a SubMonitor to report progress to
     * @throws IOException
     *             is thrown, if the upload failed; the exception wraps the
     *             target exception that contains the main cause for the failure
     * 
     *             TODO Make cancellation more fine grained (if the user cancels
     *             it takes too long to react)
     * 
     * @blocking
     * @cancelable
     */
    public static void uploadFile(File file, String server, SubMonitor monitor)
        throws IOException {
        try {
            if (file == null || !file.exists()) {
                throw new CausedIOException("Upload not possible",
                    new IllegalArgumentException(
                        "The file that should be uploaded was"
                            + " either null or nonexistent"));
            }

            monitor.beginTask("Uploading file " + file.getName(), 100);

            PostMethod post = new PostMethod(server);
            // holds the status response after the method was executed
            int status = 0;

            post.getParams().setBooleanParameter(
                HttpMethodParams.USE_EXPECT_CONTINUE, true);

            /*
             * retry the method 3 times, but not if the request was send
             * successfully
             */
            post.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));

            monitor.worked(20);
            if (monitor.isCanceled())
                throw new CancellationException();

            try {
                // create a multipart request for the file
                Part[] parts = { new FilePart(file.getName(), file) };
                post.setRequestEntity(new MultipartRequestEntity(parts, post
                    .getParams()));

                HttpClient client = new HttpClient();

                /*
                 * connection has to be established within the timeout,
                 * otherwise a ConnectTimeoutException is thrown
                 */
                client.getHttpConnectionManager().getParams()
                    .setConnectionTimeout(TIMEOUT);

                log.info("Trying to upload file " + file.getName() + " to "
                    + server + " ...");

                monitor.worked(20);
                if (monitor.isCanceled())
                    throw new CancellationException();

                // try to upload the file
                status = client.executeMethod(post);

                monitor.worked(50);

                // examine status response
                if (status == HttpStatus.SC_OK) {
                    log.info("Upload successfull for " + file.getName()
                        + ".\nServer response: "
                        + IOUtils.toString(post.getResponseBodyAsStream()));
                    return;
                }

            } catch (ConnectTimeoutException e) {
                // couldn't connect within the timeout
                throw new CausedIOException("Couldn't connect to host "
                    + server, e);
            } catch (Exception e) {
                throw new CausedIOException(
                    "An internal error occurred while trying to upload file "
                        + file.getName(), e);
            } finally {
                post.releaseConnection();
            }
            // upload failed
            throw new CausedIOException("Upload failed", new RuntimeException(
                "Server response: " + status + " "
                    + HttpStatus.getStatusText(status)));
        } finally {
            monitor.done();
        }
    }
}