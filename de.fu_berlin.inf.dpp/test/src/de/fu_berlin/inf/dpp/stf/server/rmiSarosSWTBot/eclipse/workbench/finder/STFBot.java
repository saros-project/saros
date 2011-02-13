package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder;

import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFLabel;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFStyledText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.EclipseComponent;

public interface STFBot extends EclipseComponent {

    public STFTree tree() throws RemoteException;

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
     * @return a {@link STFButton} with the specified <code>label</code>.
     */
    public STFButton buttonWithLabel(String label) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFButton} with the specified <code>label</code>.
     */
    public STFButton buttonWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @return a {@link STFButton} with the specified <code>mnemonicText</code>.
     */
    public STFButton button(String mnemonicText) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFButton} with the specified <code>mnemonicText</code>.
     */
    public STFButton button(String mnemonicText, int index)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @return a {@link STFButton} with the specified <code>tooltip</code>.
     */
    public STFButton buttonWithTooltip(String tooltip) throws RemoteException;

    public STFButton buttonWithTooltip(String tooltip, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link STFButton} with the specified <code>key/value</code>.
     */
    public STFButton buttonWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link STFButton} with the specified <code>key/value</code>.
     */
    public STFButton buttonWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link STFButton} with the specified <code>value</code>.
     */
    public STFButton buttonWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link STFButton} with the specified <code>value</code>.
     */
    public STFButton buttonWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFButton} with the specified <code>inGroup</code>.
     */
    public STFButton buttonInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFButton} with the specified <code>inGroup</code>.
     */
    public STFButton buttonInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link STFButton} with the specified <code>none</code>.
     */
    public STFButton button() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link STFButton} with the specified <code>none</code>.
     */
    public STFButton button(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFButton} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFButton buttonWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFButton} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFButton buttonWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFButton} with the specified <code>mnemonicText</code>
     *         with the specified <code>inGroup</code> .
     */
    public STFButton buttonInGroup(String mnemonicText, String inGroup)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFButton} with the specified <code>mnemonicText</code>
     *         with the specified <code>inGroup</code> .
     */
    public STFButton buttonInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFButton} with the specified <code>tooltip</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFButton buttonWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFButton} with the specified <code>tooltip</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFButton buttonWithTooltipInGroup(String tooltip, String inGroup,
        int index) throws RemoteException;

    public STFLabel label() throws RemoteException;

    public STFStyledText styledText() throws RemoteException;

}
