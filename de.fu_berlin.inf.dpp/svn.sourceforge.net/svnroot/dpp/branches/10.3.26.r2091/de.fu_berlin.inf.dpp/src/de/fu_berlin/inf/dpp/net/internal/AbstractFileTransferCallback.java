/**
 * 
 */
package de.fu_berlin.inf.dpp.net.internal;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.net.IFileTransferCallback;

/**
 * FileTransferCallback that does nothing
 */
public abstract class AbstractFileTransferCallback implements
    IFileTransferCallback {
    public void fileSent(IPath path) {
        // Do nothing
    }

    public void fileTransferFailed(IPath path, Exception e) {
        // Do nothing
    }

    public void transferProgress(int transfered) {
        // Do nothing
    }
}