/**
 * 
 */
package de.fu_berlin.inf.dpp.net.internal;

import java.io.File;
import java.io.Serializable;

import de.fu_berlin.inf.dpp.net.JID;

public class JingleFileTransferData implements Serializable {
    public static enum FileTransferType {
	/* file list information is transfer. */
	FILELIST_TRANSFER,
	/* single resource is transfer. */
	RESOURCE_TRANSFER,
    }

    private static final long serialVersionUID = -4063208452619555716L;

    public byte[] content;

    /* for testing only */
    public File file;
    public String file_list_content;
    public String file_project_path;
    public String filePath;
    public long filesize;
    public String project_name;
    public JID recipient;
    public JID sender;

    public int timestamp;
    public FileTransferType type;
}