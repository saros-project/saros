package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponent;

public interface STFBotText extends EclipseComponent {

    /**
     * set the given text into the {@link SWTBotText} with the specified
     * 
     * @param text
     *            the text which you want to insert into the text field
     * @param label
     *            the label on the widget.
     * @throws RemoteException
     */
    public void setTextInTextWithLabel(String text, String label)
        throws RemoteException;

    /**
     * 
     * @param label
     *            the label on the widget.
     * @return the text in the given {@link SWTBotText}
     * @throws RemoteException
     */
    public String getTextInTextWithLabel(String label) throws RemoteException;
}
