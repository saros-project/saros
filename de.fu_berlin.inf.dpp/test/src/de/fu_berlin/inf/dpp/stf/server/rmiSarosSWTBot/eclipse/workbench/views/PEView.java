package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.contextMenu.SarosC;

/**
 * This interface contains convenience API to perform actions in the package
 * explorer view (API to perform the specifically defined actions for saros
 * would be separately located in the sub-interface {@link SarosC} ) , then you
 * can start off as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link Tester} object in your junit-test. (How
 * to do it please look at the javadoc in class {@link TestPattern} or read the
 * user guide in TWiki https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
 * <li>
 * then you can use the object pEV initialized in {@link Tester} to access the
 * API :), e.g.
 * 
 * <pre>
 * alice.pEV.deleteProject(...);
 * </pre>
 * 
 * </li>
 * 
 * @author Lin
 */
public interface PEView extends EclipseComponent {

    public void selectProject(String projectName) throws RemoteException;

    public void selectPkg(String projectName, String pkg)
        throws RemoteException;

    public void selectClass(String projectName, String pkg, String className)
        throws RemoteException;

    public void selectFolder(String... pathToFolder) throws RemoteException;

    public void selectFile(String... pathToFile) throws RemoteException;

}
