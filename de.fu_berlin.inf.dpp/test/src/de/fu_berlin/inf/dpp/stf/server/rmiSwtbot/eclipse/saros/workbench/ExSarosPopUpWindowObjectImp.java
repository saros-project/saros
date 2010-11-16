package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipsePopUpWindowObjectImp;

public class ExSarosPopUpWindowObjectImp extends EclipsePopUpWindowObjectImp
    implements ExWindowObject {

    private static transient ExSarosPopUpWindowObjectImp self;

    /**
     * {@link ExSarosPopUpWindowObjectImp} is a singleton, but inheritance is
     * possible.
     */
    public static ExSarosPopUpWindowObjectImp getInstance() {
        if (self != null)
            return self;
        self = new ExSarosPopUpWindowObjectImp();
        return self;
    }

}
