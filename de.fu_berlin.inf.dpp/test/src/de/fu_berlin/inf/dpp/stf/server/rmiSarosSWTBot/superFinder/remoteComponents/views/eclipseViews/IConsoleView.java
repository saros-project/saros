package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.eclipseViews;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.client.tester.AbstractTester;

/**
 * This interface contains convenience API to perform a action using console
 * view widgets. You can start off as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link AbstractTester} object in your
 * junit-test. (How to do it please look at the javadoc in class
 * {@link TestPattern} or read the user guide.</li>
 * <li>
 * then you can use the object basic initialized in {@link AbstractTester} to
 * access the API :), e.g.
 * 
 * <pre>
 * alice.consoleV.getTextInConsole();
 * </pre>
 * 
 * </li>
 * 
 * @author lchen
 */
public interface IConsoleView extends Remote {

    /**
     * 
     * @return<tt>true</tt>, if there are text existed in the console.
     * @throws RemoteException
     */
    public boolean existTextInConsole() throws RemoteException;

    /**
     * 
     * @return the first styledText in the view Console
     * @throws RemoteException
     */
    public String getFirstTextInConsole() throws RemoteException;

    /**
     * Wait until the condition {@link IConsoleView#existTextInConsole()} is true
     * 
     * @throws RemoteException
     */
    public void waitUntilExistsTextInConsole() throws RemoteException;

}
