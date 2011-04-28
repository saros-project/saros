package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.sarosView.submenus;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;

public interface IWorkTogetherOnC extends Remote {

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void multipleProjects(String projectName, JID... baseJIDOfInvitees)
        throws RemoteException;

    public void project(String projectName) throws RemoteException;
}