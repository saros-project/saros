package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;

/**
 * This interface contains convenience API to perform a action using console
 * view widgets. You can start off as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link Tester} object in your junit-test. (How
 * to do it please look at the javadoc in class {@link TestPattern} or read the
 * user guide.</li>
 * <li>
 * then you can use the object basic initialized in {@link Tester} to access the
 * API :), e.g.
 * 
 * <pre>
 * alice.consoleV.getTextInConsole();
 * </pre>
 * 
 * </li>
 * 
 * @author lchen
 */
public interface ConsoleView extends Remote {

    /**
     * 
     * @return the first styledText in the view Console
     * @throws RemoteException
     */
    public String getTextInConsole() throws RemoteException;

    /**
     * Wait until there are text existed in the console
     * 
     * @throws RemoteException
     */
    public void waitUntilTextInViewConsoleExists() throws RemoteException;

}
