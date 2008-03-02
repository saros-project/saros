/**
 * 
 */
package de.fu_berlin.inf.dpp.net.internal;

import java.io.File;
import java.io.Serializable;

import de.fu_berlin.inf.dpp.net.JID;

public class JingleFileTransferData implements Serializable{
	private static final long serialVersionUID = -4063208452619555716L;
	
	public static enum FileTransferType{
		/* file list information is transfer. */
		FILELIST_TRANSFER,
		/* single resource is transfer. */
		RESOURCE_TRANSFER,
	}
	
	public FileTransferType type;
	
	public String file_list_content;
	public JID recipient;
	public JID sender;
	public String project_name;
	public String file_project_path;
	public int timestamp;
	public long filesize;
	public byte[] content;
	
	/*for testing only */
	public File file;
	public String filePath;
}