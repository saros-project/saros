package de.fu_berlin.inf.dpp.exceptions;

import de.fu_berlin.inf.dpp.invitation.InvitationProcess.CancelOption;

/**
 * Exception used for signaling that the local user canceled an operation
 */
public class LocalCancellationException extends SarosCancellationException {

    private static final long serialVersionUID = 3663315740957551184L;
    protected CancelOption cancelOption;

    public LocalCancellationException() {
        super();
    }

    public LocalCancellationException(String msg, CancelOption cancelOption) {
        super(msg);
        this.cancelOption = cancelOption;
    }

    public CancelOption getCancelOption() {
        return cancelOption;
    }
}
