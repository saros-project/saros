package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.SWTBot;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.ChatViewImp;

public class BotImp extends EclipseComponentImp implements Bot {

    private static transient BotImp self;
    SWTBot swtBot;

    /**
     * {@link ChatViewImp} is a singleton, but inheritance is possible.
     */
    public static BotImp getInstance() {
        if (self != null)
            return self;
        self = new BotImp();
        return self;
    }

    public void setBot(SWTBot bot) {
        this.swtBot = bot;
    }

    public Tree tree() throws RemoteException {
        TreeImp treeImp = TreeImp.getInstance();
        treeImp.setSWTBotTree(swtBot.tree());
        return treeImp;
    }
}
