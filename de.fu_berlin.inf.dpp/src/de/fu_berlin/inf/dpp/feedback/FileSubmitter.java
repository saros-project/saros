package de.fu_berlin.inf.dpp.feedback;

import java.io.File;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

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
    public static final String SERVER_URL = "http://projects.mi.fu-berlin.de/saros/";
    /** the name of the Servlet that is supposed to handle the upload */
    public static final String SERVLET_NAME = "SarosStatisticServer/fileupload";

    /** Value for connection timeout */
    protected static final int TIMEOUT = 5000;

    /**
     * Convenience wrapper method for
     * {@link #uploadFile(File, String, SubMonitor)}. <br>
     * The statistic file is first tried to upload to the server specified by
     * {@link #SERVER_URL} and then to our temporary server
     * {@link #SERVER_URL_TEMP}. <br>
     * <br>
     * Because the statistic file upload is supposed to take place behind the
     * scenes, no user feedback is reported back.
     * 
     * @param file
     *            the file to upload
     * @return true, if the upload was successful, false otherwise
     * 
     * @blocking
     */
    public static boolean uploadStatisticFile(File file) {
        boolean success = uploadFile(file, SERVER_URL + SERVLET_NAME,
            SubMonitor.convert(new NullProgressMonitor()));
        if (success)
            return true;
        return uploadFile(file, SERVER_URL_TEMP + SERVLET_NAME, SubMonitor
            .convert(new NullProgressMonitor()));
    }

    /**
     * Tries to upload the given file to the given server (via POST method).
     * 
     * @param file
     *            the file to upload
     * @param server
     *            the URL of the server, that is supposed to handle the file
     * @param progress
     *            a SubMonitor to report progress to
     * @return true, if the upload was successful, false otherwise
     * 
     * @blocking
     */
    public static boolean uploadFile(File file, String server,
        SubMonitor progress) {
        if (file == null || !file.exists()) {
            log.error("The file that should be uploaded was"
                + " either null or nonexistent.");
            return false;
        }

        // begin task of unknown length
        progress.beginTask("Uploading file " + file.getName(), 10000);

        PostMethod post = new PostMethod(server);

        post.getParams().setBooleanParameter(
            HttpMethodParams.USE_EXPECT_CONTINUE, true);

        /*
         * retry the method 3 times, but not if the request was send
         * successfully
         */
        post.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
            new DefaultHttpMethodRetryHandler(3, false));

        try {
            // create a multipart request for the file
            Part[] parts = { new FilePart(file.getName(), file) };
            post.setRequestEntity(new MultipartRequestEntity(parts, post
                .getParams()));

            HttpClient client = new HttpClient();
            /*
             * connection has to be established within the timeout, otherwise a
             * ConnectTimeoutException is thrown
             */
            client.getHttpConnectionManager().getParams().setConnectionTimeout(
                TIMEOUT);

            log.info("Trying to upload file " + file.getName() + " to "
                + server + "...");

            // try to upload the file
            int status = client.executeMethod(post);

            // examine status response
            if (status == HttpStatus.SC_OK) {
                log.info("Upload successfull. Server response="
                    + post.getResponseBodyAsString());
                return true;
            }

            log.error("Upload failed. Server response=" + status + " "
                + HttpStatus.getStatusText(status));
        } catch (ConnectTimeoutException e) {
            // couldn't connect within the timeout
            log.warn(e.getMessage());
        } catch (Exception e) {
            log.error("An internal error occurred while trying to upload file "
                + file.getName(), e);
        } finally {
            post.releaseConnection();
            progress.done();
        }
        // upload failed
        return false;
    }
}