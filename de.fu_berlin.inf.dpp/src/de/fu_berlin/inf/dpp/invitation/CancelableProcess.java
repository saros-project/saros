package de.fu_berlin.inf.dpp.invitation;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;

/**
 * Abstract base class that offers multiple methods for handling cancellation.
 * 
 * @author srossbach
 */

abstract class CancelableProcess {

    public enum Status {
        OK, CANCEL, REMOTE_CANCEL, ERROR, REMOTE_ERROR
    }

    private static final Logger log = Logger.getLogger(CancelableProcess.class);

    private IProgressMonitor monitorToObserve;

    private boolean isRemoteCancellation;

    private boolean isLocalCancellation;

    private SarosCancellationException cancellationCause;

    private String errorMessage;

    private boolean processTerminated;

    private Status exitStatus;

    /**
     * Returns the error message if the exit status of the process was either
     * {@link Status#ERROR} or {@link Status#REMOTE_ERROR}.
     * 
     * @return the error message
     */
    public synchronized String getErrorMessage() {
        return errorMessage;
    }

    /**
     * This method is called after {@link #terminateProcess} decides to perform
     * a cleanup because the execution of the process was canceled. Implementing
     * classes should try a maximum effort to revert all the changes that were
     * made before the process was aborted.
     */
    protected abstract void executeCancellation();

    /**
     * This method is called after {@link #terminateProcess} decides to cancel
     * the current process. It is up to the implementing class to forward this
     * notification.
     * 
     * @param cancellationCause
     *            the cause of the cancellation
     */
    protected abstract void notifyCancellation(
        SarosCancellationException cancellationCause);

    /**
     * Registers a monitor which should be observed to determine the status of a
     * local cancellation of the current process.
     * 
     * @param monitor
     *            the monitor to observer
     * 
     * @see #isLocalCancellation
     * @see #checkCancellation(CancelOption)
     */
    protected synchronized final void observeMonitor(IProgressMonitor monitor) {
        monitorToObserve = monitor;
    }

    /**
     * Checks the current cancellation status of this process. If a local
     * cancellation request is detected this method will invoke
     * {@link #localCancel(String errorMessage, CancelOption cancelOption)} with
     * <code>null</code> as errorMessage argument.
     * 
     * @param cancelOption
     *            the cancel option to use when a local cancellation was set
     * @throws SarosCancellationException
     *             if the current process should be cancelled
     */
    protected synchronized final void checkCancellation(
        CancelOption cancelOption) throws SarosCancellationException {

        if (isLocalCancellation())
            localCancel(null, cancelOption);
        else if (!isRemoteCancellation())
            return;

        assert (cancellationCause != null);

        throw cancellationCause;
    }

    /**
     * Informs the current process that it should cancel its operation as soon
     * as possible. Calling this method multiple times will <b>NOT</b> override
     * the error message or the cancel option. This method will also have
     * <b>NO</b> effect if a remote cancellation request was already executed.
     * 
     * @param errorMessage
     *            the reason to cancel the execution in case of an error or
     *            <code>null</code> if this is a normal cancel request
     * @param cancelOption
     *            either {@link CancelOption#NOTIFY_PEER} to inform the remote
     *            side of the cancellation or
     *            {@link CancelOption#DO_NOT_NOTIFY_PEER}
     * @return <code>true</code> if this request was the first cancel request,
     *         <code>false</code> otherwise
     * 
     * @see #remoteCancel
     * @see #checkCancellation
     * @see #notifyCancellation
     */
    public synchronized boolean localCancel(String errorMessage,
        CancelOption cancelOption) {
        if (!cancel(new LocalCancellationException(errorMessage, cancelOption)))
            return false;

        isLocalCancellation = true;

        log.debug("process " + this
            + " was cancelled by the local side, error: "
            + (errorMessage == null ? "none" : errorMessage));

        return true;
    }

    /**
     * Informs the current process that it should cancel its operation as soon
     * as possible. Calling this method multiple times will <b>NOT</b> override
     * the error message. This method will also have <b>NO</b> effect if a local
     * cancellation request was already executed.
     * 
     * @param errorMsg
     *            the reason to cancel the execution in case of an error or
     *            <code>null</code> if this is a cancel abort request
     * @return <code>true</code> if this request was the first cancel request,
     *         <code>false</code> otherwise
     * 
     * @see #localCancel
     * @see #checkCancellation
     * @see #notifyCancellation
     */

    public synchronized boolean remoteCancel(String errorMsg) {
        if (!cancel(new RemoteCancellationException(errorMsg)))
            return false;

        isRemoteCancellation = true;

        log.debug("process " + this
            + " was cancelled by the remote side, error: "
            + (errorMsg == null ? "none" : errorMsg));

        return true;
    }

    /**
     * Returns if this process should be canceled.
     * 
     * @return <code>true</code> this process should be canceled,
     *         <code>false</code> otherwise
     */
    protected final synchronized boolean isCanceled() {
        return isRemoteCancellation || isLocalCancellation;
    }

    /**
     * Returns if the current process should be cancelled because of a local
     * cancellation request
     * 
     * @return <code>true</code> if cancellation is requested on the local side,
     *         <code>false</code> otherwise
     */
    protected final synchronized boolean isLocalCancellation() {
        return !isRemoteCancellation
            && ((monitorToObserve != null && monitorToObserve.isCanceled()) || isLocalCancellation);

    }

    /**
     * Returns if the current process should be cancelled because of a remote
     * cancellation request
     * 
     * @return <code>true</code> if cancellation is requested on the remote
     *         side, <code>false</code> otherwise
     */
    protected final synchronized boolean isRemoteCancellation() {
        return isRemoteCancellation;
    }

    /**
     * Terminates the current process. This method may be called multiple times
     * but only the <b>first</b> call will be taken into account. If the process
     * was cancelled in the meantime it will invoke {@link #notifyCancellation}
     * and {@link #executeCancellation} in this order.
     * 
     * 
     * @param exception
     *            The exception to analyze or <code>null</code>. If the process
     *            had already been canceled by a {@link #localCancel} or a
     *            {@link #remoteCancel} call the exception will be ignored. If
     *            the exception is <code>null</code> then the exit status will
     *            be determined on former {@link #localCancel} or
     *            {@link #remoteCancel} calls.
     * @return the {@link Status} of the termination
     */
    protected final synchronized Status terminateProcess(Exception exception) {

        Status lastExitStatus = null;

        // allow multiple calls to log exceptions
        if (processTerminated)
            lastExitStatus = exitStatus;

        exitStatus = Status.OK;

        if (exception == null)
            exception = cancellationCause;

        String error = exception == null ? null : exception.getMessage();

        if (exception instanceof LocalCancellationException) {
            localCancel(exception.getMessage(),
                ((LocalCancellationException) exception).getCancelOption());

            exitStatus = error == null ? Status.CANCEL : Status.ERROR;
        } else if (exception instanceof RemoteCancellationException) {
            remoteCancel(exception.getMessage());
            exitStatus = error == null ? Status.REMOTE_CANCEL
                : Status.REMOTE_ERROR;

        } else if (exception instanceof IOException) {
            String errorMsg = "Unknown error: " + exception;

            if (exception.getMessage() != null)
                errorMsg = exception.getMessage();

            localCancel(errorMsg, CancelOption.NOTIFY_PEER);
            exitStatus = Status.ERROR;
        } else if (exception != null) {
            log.error(this + " unknown error", exception);
            String errorMsg = "Unknown error: " + exception;

            if (exception.getMessage() != null)
                errorMsg = exception.getMessage();

            localCancel(errorMsg, CancelOption.NOTIFY_PEER);
            exitStatus = Status.ERROR;
        }

        if (processTerminated) {
            exitStatus = lastExitStatus;
            return exitStatus;
        }

        processTerminated = true;

        if (exitStatus != Status.OK) {
            errorMessage = generateErrorMessage();
            notifyCancellation(cancellationCause);
            log.debug("executing cancellation for process " + this);
            executeCancellation();
        }

        log.debug("process " + this + " exit status: " + exitStatus);
        return exitStatus;
    }

    /**
     * @return <code>true</code> if this was the first processed cancellation,
     *         <code>false</code> otherwise
     */
    private synchronized boolean cancel(SarosCancellationException cause) {
        if (monitorToObserve != null)
            monitorToObserve.setCanceled(true);

        if (isCanceled())
            return false;

        cancellationCause = cause;

        return true;
    }

    // TODO: move to UI
    private synchronized String generateErrorMessage() {
        String errorMessage = null;

        if (cancellationCause == null)
            return null;

        assert (cancellationCause != null);

        String exceptionMessage = cancellationCause.getMessage();

        if (cancellationCause instanceof LocalCancellationException) {
            if (exceptionMessage != null) {
                errorMessage = "Invitation was cancelled locally"
                    + " because of an error: " + exceptionMessage;
                log.error("cancelled process " + this + ", error: "
                    + exceptionMessage);
            } else {
                log.debug("process " + this
                    + " was cancelled manually by the local user");
            }

        } else if (cancellationCause instanceof RemoteCancellationException) {
            if (exceptionMessage != null) {
                errorMessage = "Invitation was cancelled by the remote user "
                    + " because of an error on his/her side: "
                    + exceptionMessage;

                log.error("cancelled process " + this
                    + " because the remote side encountered an error: "
                    + exceptionMessage);

            } else {
                log.debug("process " + this
                    + " was cancelled manually by the remote side");

            }
        } else {
            log.error("unexpected exception: "
                + cancellationCause.getClass().getName(), cancellationCause);
        }

        return errorMessage;
    }
}
