package de.fu_berlin.inf.dpp.net.jingle;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData;

public abstract class JingleFileTransferTCPConnection {

    private static Logger logger = Logger
	    .getLogger(JingleFileTransferTCPConnection.class);

    protected IJingleFileTransferListener listener;

    /* transfer information */
    protected JingleFileTransferData receiveTransferData;

    protected void receiveFileListData(InputStream input) throws IOException,
	    ClassNotFoundException {
	JingleFileTransferTCPConnection.logger.debug("receive file List");
	ObjectInputStream ii = new ObjectInputStream(input);

	String fileListData = (String) ii.readObject();

	/* inform listener. */
	this.listener.incommingFileList(fileListData,
		this.receiveTransferData.sender);
    }

    protected void receiveMetaData(InputStream input) throws IOException,
	    ClassNotFoundException {
	ObjectInputStream ii = new ObjectInputStream(input);

	JingleFileTransferData meta = (JingleFileTransferData) ii.readObject();
	this.receiveTransferData = meta;

    }

    protected void sendFileListData(OutputStream output,
	    String file_list_content) throws IOException {
	ObjectOutputStream oo = new ObjectOutputStream(output);

	oo.writeObject(file_list_content);
	oo.flush();
    }

    protected void sendMetaData(OutputStream output, JingleFileTransferData data)
	    throws IOException {
	ObjectOutputStream oo = new ObjectOutputStream(output);
	oo.writeObject(data);
	oo.flush();
    }
}
