package de.fu_berlin.inf.dpp.net;

import org.eclipse.core.runtime.IPath;

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
     * Is fired when part of file has transfered..
     * 
     * @param transfered
     *            current transfered size.
     */
    public void transferProgress(int transfered);

}
