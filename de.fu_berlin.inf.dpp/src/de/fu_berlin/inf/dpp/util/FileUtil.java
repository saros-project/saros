package de.fu_berlin.inf.dpp.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * this class get files or contents and check for identicalness.
 * 
 * @author orieger
 * 
 *         TODO CJ: review needed
 * 
 */
public class FileUtil {

    /**
     * calculate checksum for given file
     * 
     * @param file
     * @return checksum of file or -1 if checksum calculation has been failed.
     */
    public static Long checksum(IFile file) {
	InputStream contents = null;

	try {
	    // Adler-32 checksum
	    contents = file.getContents();
	    CheckedInputStream cis = new CheckedInputStream(contents,
		    new Adler32());

	    byte[] tempBuf = new byte[128];
	    while (cis.read(tempBuf) >= 0) {
	    }
	    long checksum = cis.getChecksum().getValue();
	    return new Long(checksum);

	} catch (IOException e) {
	    e.printStackTrace();

	} catch (CoreException e) {
	    e.printStackTrace();

	} finally {
	    try {
		if (contents != null) {
		    contents.close();
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}

	return new Long(-1);
    }

}
