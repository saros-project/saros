package de.fu_berlin.inf.dpp.test.util;

import org.eclipse.core.runtime.CoreException;

import de.fu_berlin.inf.dpp.FileList;

public class FileListHelper {

	public static FileList createFileListForDefaultProject() throws CoreException{
		return new FileList(ResourceHelper.createDefaultProject());
	}
	
}
