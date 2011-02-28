package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents;

import java.rmi.Remote;

/**
 * This interface contains common APIs, which are often used by other
 * components. All components which inherit this interface can use these APIs.
 * For example,
 * <ol>
 * <li>
 * After creating new file with alice.file.newFile(...) you can check if the
 * file is already created with assertTrue(alice.file.existsFile(...)).</li>
 * <li>After deleting a file with alice.edit.deleteFile(...), you can also use
 * the method to check if the file is already deleted with
 * assertFalse(alice.edit.existsFile(...))</li>
 * 
 * 
 * 
 * @author lchen
 */
public interface EclipseComponent extends Remote {

}
