package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;

public class RemoteBotStyledTextImp extends AbstractRmoteWidget implements
    RemoteBotStyledText {

    private static transient RemoteBotStyledTextImp self;

    private SWTBotStyledText widget;

    /**
     * {@link RemoteBotStyledTextImp} is a singleton, but inheritance is possible.
     */
    public static RemoteBotStyledTextImp getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotStyledTextImp();
        return self;
    }

    public RemoteBotStyledText setWidget(SWTBotStyledText styledText) {
        this.widget = styledText;
        return this;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * finders
     * 
     **********************************************/

    public RemoteBotMenu contextMenu(String text) throws RemoteException {
        return stfBotMenu.setWidget(widget.contextMenu(text));
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public String getText() throws RemoteException {

        return widget.getText();
    }

    public String getToolTipText() throws RemoteException {
        return widget.getToolTipText();
    }

    public String getTextOnCurrentLine() throws RemoteException {
        return widget.getTextOnCurrentLine();
    }

    public String getSelection() throws RemoteException {
        return widget.getSelection();
    }

    public List<String> getLines() throws RemoteException {
        return widget.getLines();
    }

    public int getLineCount() throws RemoteException {
        return widget.getLineCount();
    }
}
