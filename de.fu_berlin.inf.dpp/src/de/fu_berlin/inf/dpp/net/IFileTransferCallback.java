package de.fu_berlin.inf.dpp.net;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.TransferMode;

/**
 * This is an simple callback listener for non-blocking file transfers.
 * 
 * @author rdjemili
 */
public interface IFileTransferCallback {

    /**
     * Is fired when the file was successfully transfered.
     * 
     * @param path
     *            the project-relative path to the file.
     */
    public void fileSent(IPath path);

    /**
     * Is fired when the file transfer failed.
     * 
     * @param path
     *            the project-relative path to the file.
     * @param e
     *            the exception that caused the fail or <code>null</code>.
     */
    public void fileTransferFailed(IPath path, Exception e);

    /**
     * Is fired part of file has transfered..
     * 
     * @param transfered
     *            current transfered size.
     */
    public void transferProgress(int transfered);

    /**
     * set transfer mode. TransferMode.Jingle for Jingle transfer or
     * TransferMode.IBB for XMPP transfer.
     * 
     * @param mode
     */
    public void setTransferMode(TransferMode mode);

    /**
     * Is fired if jingle connection couldn't be established.
     */
    public void jingleFallback();
}
