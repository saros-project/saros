package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseObject;

public class SarosObject extends EclipseObject {

    protected SarosRmiSWTWorkbenchBot sarosRmiBot;

    public SarosObject(SarosRmiSWTWorkbenchBot sarosRmiBot) {
        super(sarosRmiBot);
        this.sarosRmiBot = sarosRmiBot;
    }

}
