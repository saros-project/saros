package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;

public class STFBotStyledTextImp extends AbstractRmoteWidget implements
    STFBotStyledText {

    private static transient STFBotStyledTextImp self;

    private SWTBotStyledText widget;

    /**
     * {@link STFBotStyledTextImp} is a singleton, but inheritance is possible.
     */
    public static STFBotStyledTextImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotStyledTextImp();
        return self;
    }

    public STFBotStyledText setWidget(SWTBotStyledText styledText) {
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

    public STFBotMenu contextMenu(String text) throws RemoteException {
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
