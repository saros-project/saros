package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages;

import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.RmiSWTWorkbenchBot;

public class ProgressViewObject extends EclipseObject implements
    IProgressViewObject {

    public static ProgressViewObject classVariable;

    public ProgressViewObject(RmiSWTWorkbenchBot rmiBot) {
        super(rmiBot);
    }

    public void openProgressView() throws RemoteException {
        viewObject.openViewById("org.eclipse.ui.views.ProgressView");
    }

    public void activateProgressView() throws RemoteException {
        viewObject.setFocusOnViewByTitle(PGViewName);
    }

    public boolean existPorgress() throws RemoteException {
        openProgressView();
        activateProgressView();
        SWTBotView view = bot.viewByTitle("Progress");
        view.setFocus();
        view.toolbarButton("Remove All Finished Operations").click();
        SWTBot bot = view.bot();
        try {
            bot.toolbarButton();
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

}
