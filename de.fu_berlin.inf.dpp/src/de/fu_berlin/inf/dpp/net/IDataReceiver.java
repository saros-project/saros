package de.fu_berlin.inf.dpp.net;

import java.io.InputStream;

import org.eclipse.core.runtime.Path;

import de.fu_berlin.inf.dpp.net.internal.TransferDescription;

public interface IDataReceiver {

    boolean receivedArchive(TransferDescription data, InputStream input);

    boolean receivedResource(JID from, Path path, InputStream input, int time);

    boolean receivedFileList(TransferDescription data, InputStream input);
}
