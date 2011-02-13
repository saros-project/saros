package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder;

import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotLabel;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotStyledText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponent;

public interface STFBot extends EclipseComponent {

    public STFBotTree tree() throws RemoteException;

    public STFBotShell shell(String title) throws RemoteException;

    public List<String> getTitlesOfOpenedShells() throws RemoteException;

    public boolean isShellOpen(String title) throws RemoteException;

    /**
     * waits until the given STFBotShell is closed.
     * 
     * @param title
     *            the title of the shell.
     * @throws RemoteException
     */
    public void waitsUntilIsShellClosed(final String title)
        throws RemoteException;

    /**
     * waits until the given STFBotShell is open.
     * 
     * @param title
     *            the title of the shell.
     * @throws RemoteException
     */
    public void waitUntilShellOpen(final String title) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @return a {@link STFBotButton} with the specified <code>label</code>.
     */
    public STFBotButton buttonWithLabel(String label) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotButton} with the specified <code>label</code>.
     */
    public STFBotButton buttonWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @return a {@link STFBotButton} with the specified <code>mnemonicText</code>.
     */
    public STFBotButton button(String mnemonicText) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotButton} with the specified <code>mnemonicText</code>.
     */
    public STFBotButton button(String mnemonicText, int index)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @return a {@link STFBotButton} with the specified <code>tooltip</code>.
     */
    public STFBotButton buttonWithTooltip(String tooltip) throws RemoteException;

    public STFBotButton buttonWithTooltip(String tooltip, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link STFBotButton} with the specified <code>key/value</code>.
     */
    public STFBotButton buttonWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotButton} with the specified <code>key/value</code>.
     */
    public STFBotButton buttonWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link STFBotButton} with the specified <code>value</code>.
     */
    public STFBotButton buttonWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotButton} with the specified <code>value</code>.
     */
    public STFBotButton buttonWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotButton} with the specified <code>inGroup</code>.
     */
    public STFBotButton buttonInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotButton} with the specified <code>inGroup</code>.
     */
    public STFBotButton buttonInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link STFBotButton} with the specified <code>none</code>.
     */
    public STFBotButton button() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotButton} with the specified <code>none</code>.
     */
    public STFBotButton button(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotButton} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotButton buttonWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotButton} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotButton buttonWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotButton} with the specified <code>mnemonicText</code>
     *         with the specified <code>inGroup</code> .
     */
    public STFBotButton buttonInGroup(String mnemonicText, String inGroup)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotButton} with the specified <code>mnemonicText</code>
     *         with the specified <code>inGroup</code> .
     */
    public STFBotButton buttonInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotButton} with the specified <code>tooltip</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotButton buttonWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotButton} with the specified <code>tooltip</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotButton buttonWithTooltipInGroup(String tooltip, String inGroup,
        int index) throws RemoteException;

    public STFBotLabel label() throws RemoteException;

    public STFBotStyledText styledText() throws RemoteException;

}
