package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotCCombo;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotCheckBox;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotCombo;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotLabel;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotList;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotRadio;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotStyledText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotToggleButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotToolbarButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotTree;

public interface RemoteBot extends Remote {

    /**********************************************
     * 
     * Widget tree
     * 
     **********************************************/
    /**
     * @see SWTBot#tree()
     */
    public RemoteBotTree tree() throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @return a {@link RemoteBotTree} with the specified <code>label</code>.
     */
    public RemoteBotTree treeWithLabel(String label) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotTree} with the specified <code>label</code>.
     */

    public RemoteBotTree treeWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link RemoteBotTree} with the specified <code>key/value</code>.
     */
    public RemoteBotTree treeWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotTree} with the specified <code>key/value</code>.
     */

    public RemoteBotTree treeWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link RemoteBotTree} with the specified <code>value</code>.
     */
    public RemoteBotTree treeWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotTree} with the specified <code>value</code>.
     */

    public RemoteBotTree treeWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotTree} with the specified <code>inGroup</code>.
     */
    public RemoteBotTree treeInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotTree} with the specified <code>inGroup</code>.
     */

    public RemoteBotTree treeInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotTree} with the specified <code>none</code>.
     */

    public RemoteBotTree tree(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotTree} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public RemoteBotTree treeWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotTree} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */

    public RemoteBotTree treeWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException;

    /**********************************************
     * 
     * Widget shell
     * 
     **********************************************/

    /**
     * 
     * @see SWTBot#shell(String)
     * @throws RemoteException
     */
    public RemoteBotShell shell(String title) throws RemoteException;

    /**
     * 
     * @return list of titles of all opened shells
     * @throws RemoteException
     */
    public List<String> getTitlesOfOpenedShells() throws RemoteException;

    /**
     * 
     * @param title
     *            the title of the shell
     * @return<tt>true</tt>, if the given shell is open.
     * @throws RemoteException
     */
    public boolean isShellOpen(String title) throws RemoteException;

    /**
     * waits until the given Shell is closed.
     * 
     * @param title
     *            the title of the shell.
     * @throws RemoteException
     *             ;;
     */
    public void waitUntilShellIsClosed(final String title)
        throws RemoteException;

    /**
     * waits until the given Shell is open.
     * 
     * @param title
     *            the title of the shell.
     * @throws RemoteException
     *             ;;
     */
    public void waitUntilShellIsOpen(final String title) throws RemoteException;

    public void waitLongUntilShellIsOpen(final String title)
        throws RemoteException;

    /**********************************************
     * 
     * Widget button
     * 
     **********************************************/
    /**
     * @see SWTBot#buttonWithLabel(String)
     */
    public RemoteBotButton buttonWithLabel(String label) throws RemoteException;

    /**
     * @see SWTBot#buttonWithLabelInGroup(String, String, int)
     */
    public RemoteBotButton buttonWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @see SWTBot#button(String)
     */
    public RemoteBotButton button(String mnemonicText) throws RemoteException;

    /**
     * @see SWTBot#button(String, int)
     */
    public RemoteBotButton button(String mnemonicText, int index)
        throws RemoteException;

    /**
     * @see SWTBot#buttonWithTooltip(String)
     */
    public RemoteBotButton buttonWithTooltip(String tooltip)
        throws RemoteException;

    public RemoteBotButton buttonWithTooltip(String tooltip, int index)
        throws RemoteException;

    /**
     * @see SWTBot#buttonWithId(String, String)
     */
    public RemoteBotButton buttonWithId(String key, String value)
        throws RemoteException;

    /**
     * @see SWTBot#buttonWithId(String, String, int)
     */
    public RemoteBotButton buttonWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @see SWTBot#buttonWithId(String)
     */
    public RemoteBotButton buttonWithId(String value) throws RemoteException;

    /**
     * @see SWTBot#buttonWithId(String,int)
     */
    public RemoteBotButton buttonWithId(String value, int index)
        throws RemoteException;

    /**
     * @see SWTBot#buttonInGroup(String)
     */
    public RemoteBotButton buttonInGroup(String inGroup) throws RemoteException;

    /**
     * @see SWTBot#buttonInGroup(String,int)
     */
    public RemoteBotButton buttonInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @see SWTBot#button()
     */
    public RemoteBotButton button() throws RemoteException;

    /**
     * @see SWTBot#button(int)
     */
    public RemoteBotButton button(int index) throws RemoteException;

    /**
     * @see SWTBot#buttonWithLabelInGroup(String, String)
     */
    public RemoteBotButton buttonWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @see SWTBot#buttonWithLabelInGroup(String, String,int)
     */
    public RemoteBotButton buttonWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException;

    /**
     * @see SWTBot#buttonInGroup(String, String)
     */
    public RemoteBotButton buttonInGroup(String mnemonicText, String inGroup)
        throws RemoteException;

    /**
     * @see SWTBot#buttonInGroup(String, String, int) .
     */
    public RemoteBotButton buttonInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException;

    /**
     * @see SWTBot#buttonWithTooltipInGroup(String, String)
     */
    public RemoteBotButton buttonWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException;

    /**
     * @see SWTBot#buttonWithTooltipInGroup(String, String, int)
     */
    public RemoteBotButton buttonWithTooltipInGroup(String tooltip,
        String inGroup, int index) throws RemoteException;

    /**********************************************
     * 
     * Widget label
     * 
     **********************************************/

    public RemoteBotLabel label() throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @return a {@link RemoteBotLabel} with the specified
     *         <code>mnemonicText</code>.
     */
    public RemoteBotLabel label(String mnemonicText) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotLabel} with the specified
     *         <code>mnemonicText</code>.
     */

    public RemoteBotLabel label(String mnemonicText, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link RemoteBotLabel} with the specified <code>key/value</code>.
     */
    public RemoteBotLabel labelWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotLabel} with the specified <code>key/value</code>.
     */

    public RemoteBotLabel labelWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link RemoteBotLabel} with the specified <code>value</code>.
     */
    public RemoteBotLabel labelWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotLabel} with the specified <code>value</code>.
     */

    public RemoteBotLabel labelWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotLabel} with the specified <code>inGroup</code>.
     */
    public RemoteBotLabel labelInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotLabel} with the specified <code>inGroup</code>.
     */

    public RemoteBotLabel labelInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotLabel} with the specified <code>none</code>.
     */

    public RemoteBotLabel label(int index) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotLabel} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */
    public RemoteBotLabel labelInGroup(String mnemonicText, String inGroup)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotLabel} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */

    public RemoteBotLabel labelInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException;

    public boolean existsStyledText() throws RemoteException;

    public boolean existsLabel() throws RemoteException;

    public boolean existsLabel(String text) throws RemoteException;

    /**********************************************
     * 
     * Widget styledText
     * 
     **********************************************/
    /**
     * @see SWTBot#styledText(String)
     */
    public RemoteBotStyledText styledTextWithLabel(String label)
        throws RemoteException;

    /**
     * @see SWTBot#styledTextWithLabel(String, int)
     */
    public RemoteBotStyledText styledTextWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @see SWTBot#styledText(String)
     */
    public RemoteBotStyledText styledText(String text) throws RemoteException;

    /**
     * @see SWTBot#styledText(String, int)
     */
    public RemoteBotStyledText styledText(String text, int index)
        throws RemoteException;

    /**
     * @see SWTBot#styledTextWithId(String, String)
     */
    public RemoteBotStyledText styledTextWithId(String key, String value)
        throws RemoteException;

    /**
     * @see SWTBot#styledTextWithId(String, String, int)
     */
    public RemoteBotStyledText styledTextWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @see SWTBot#styledTextWithId(String)
     */
    public RemoteBotStyledText styledTextWithId(String value)
        throws RemoteException;

    /**
     * @see SWTBot#styledTextWithId(String, int)
     */
    public RemoteBotStyledText styledTextWithId(String value, int index)
        throws RemoteException;

    /**
     * @see SWTBot#styledTextInGroup(String)
     */
    public RemoteBotStyledText styledTextInGroup(String inGroup)
        throws RemoteException;

    /**
     * @see SWTBot#styledTextInGroup(String, int)
     */
    public RemoteBotStyledText styledTextInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @see SWTBot#styledText()
     */
    public RemoteBotStyledText styledText() throws RemoteException;

    /**
     * @see SWTBot#styledText(int)
     */
    public RemoteBotStyledText styledText(int index) throws RemoteException;

    /**
     * @see SWTBot#styledTextWithLabelInGroup(String, String)
     */
    public RemoteBotStyledText styledTextWithLabelInGroup(String label,
        String inGroup) throws RemoteException;

    /**
     * @see SWTBot#styledTextWithLabelInGroup(String, String, int)
     */
    public RemoteBotStyledText styledTextWithLabelInGroup(String label,
        String inGroup, int index) throws RemoteException;

    /**
     * @see SWTBot#styledTextInGroup(String, String)
     */
    public RemoteBotStyledText styledTextInGroup(String text, String inGroup)
        throws RemoteException;

    /**
     * @see SWTBot#styledTextInGroup(String, String, int)
     */
    public RemoteBotStyledText styledTextInGroup(String text, String inGroup,
        int index) throws RemoteException;

    /**********************************************
     * 
     * Widget comboBox
     * 
     **********************************************/
    /**
     * @param label
     *            the label on the widget.
     * @return a {@link RemoteBotCombo} with the specified <code>label</code>.
     */
    public RemoteBotCombo comboBoxWithLabel(String label) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCombo} with the specified <code>label</code>.
     */

    public RemoteBotCombo comboBoxWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @return a {@link RemoteBotCombo} with the specified <code>text</code>.
     */
    public RemoteBotCombo comboBox(String text) throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCombo} with the specified <code>text</code>.
     */

    public RemoteBotCombo comboBox(String text, int index) throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link RemoteBotCombo} with the specified <code>key/value</code>.
     */
    public RemoteBotCombo comboBoxWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCombo} with the specified <code>key/value</code>.
     */

    public RemoteBotCombo comboBoxWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link RemoteBotCombo} with the specified <code>value</code>.
     */
    public RemoteBotCombo comboBoxWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCombo} with the specified <code>value</code>.
     */

    public RemoteBotCombo comboBoxWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotCombo} with the specified <code>inGroup</code>.
     */
    public RemoteBotCombo comboBoxInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCombo} with the specified <code>inGroup</code>.
     */

    public RemoteBotCombo comboBoxInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link RemoteBotCombo} with the specified <code>none</code>.
     */
    public RemoteBotCombo comboBox() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCombo} with the specified <code>none</code>.
     */

    public RemoteBotCombo comboBox(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotCombo} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public RemoteBotCombo comboBoxWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCombo} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */

    public RemoteBotCombo comboBoxWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotCombo} with the specified <code>text</code> with
     *         the specified <code>inGroup</code>.
     */
    public RemoteBotCombo comboBoxInGroup(String text, String inGroup)
        throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCombo} with the specified <code>text</code> with
     *         the specified <code>inGroup</code>.
     */

    public RemoteBotCombo comboBoxInGroup(String text, String inGroup, int index)
        throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @return a {@link RemoteBotCCombo} with the specified <code>text</code>.
     */
    public RemoteBotCCombo ccomboBox(String text) throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCCombo} with the specified <code>text</code>.
     */

    public RemoteBotCCombo ccomboBox(String text, int index)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @return a {@link RemoteBotCCombo} with the specified <code>label</code>.
     */
    public RemoteBotCCombo ccomboBoxWithLabel(String label) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCCombo} with the specified <code>label</code>.
     */
    public RemoteBotCCombo ccomboBoxWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link RemoteBotCCombo} with the specified <code>key/value</code>.
     */
    public RemoteBotCCombo ccomboBoxWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCCombo} with the specified <code>key/value</code>.
     */
    public RemoteBotCCombo ccomboBoxWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link RemoteBotCCombo} with the specified <code>value</code>.
     */
    public RemoteBotCCombo ccomboBoxWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCCombo} with the specified <code>value</code>.
     */
    public RemoteBotCCombo ccomboBoxWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotCCombo} with the specified <code>inGroup</code>.
     */
    public RemoteBotCCombo ccomboBoxInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCCombo} with the specified <code>inGroup</code>.
     */
    public RemoteBotCCombo ccomboBoxInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link RemoteBotCCombo} with the specified <code>none</code>.
     */
    public RemoteBotCCombo ccomboBox() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCCombo} with the specified <code>none</code>.
     */
    public RemoteBotCCombo ccomboBox(int index) throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotCCombo} with the specified <code>text</code> with
     *         the specified <code>inGroup</code>.
     */
    public RemoteBotCCombo ccomboBoxInGroup(String text, String inGroup)
        throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCCombo} with the specified <code>text</code> with
     *         the specified <code>inGroup</code>.
     */
    public RemoteBotCCombo ccomboBoxInGroup(String text, String inGroup, int index)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotCCombo} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public RemoteBotCCombo ccomboBoxWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCCombo} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */

    public RemoteBotCCombo ccomboBoxWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException;

    public RemoteBotToolbarButton toolbarButton() throws RemoteException;

    public RemoteBotToolbarButton toolbarButton(int index) throws RemoteException;

    public boolean existsToolbarButton() throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @return a {@link RemoteBotToolbarButton} with the specified
     *         <code>mnemonicText</code>.
     */
    public RemoteBotToolbarButton toolbarButton(String mnemonicText)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToolbarButton} with the specified
     *         <code>mnemonicText</code>.
     */

    public RemoteBotToolbarButton toolbarButton(String mnemonicText, int index)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @return a {@link RemoteBotToolbarButton} with the specified
     *         <code>tooltip</code>.
     */
    public RemoteBotToolbarButton toolbarButtonWithTooltip(String tooltip)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToolbarButton} with the specified
     *         <code>tooltip</code>.
     */

    public RemoteBotToolbarButton toolbarButtonWithTooltip(String tooltip,
        int index) throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link RemoteBotToolbarButton} with the specified
     *         <code>key/value</code>.
     */
    public RemoteBotToolbarButton toolbarButtonWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToolbarButton} with the specified
     *         <code>key/value</code>.
     */

    public RemoteBotToolbarButton toolbarButtonWithId(String key, String value,
        int index) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link RemoteBotToolbarButton} with the specified
     *         <code>value</code>.
     */
    public RemoteBotToolbarButton toolbarButtonWithId(String value)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToolbarButton} with the specified
     *         <code>value</code>.
     */

    public RemoteBotToolbarButton toolbarButtonWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotToolbarButton} with the specified
     *         <code>inGroup</code>.
     */
    public RemoteBotToolbarButton toolbarButtonInGroup(String inGroup)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToolbarButton} with the specified
     *         <code>inGroup</code>.
     */

    public RemoteBotToolbarButton toolbarButtonInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotToolbarButton} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */
    public RemoteBotToolbarButton toolbarButtonInGroup(String mnemonicText,
        String inGroup) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToolbarButton} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */

    public RemoteBotToolbarButton toolbarButtonInGroup(String mnemonicText,
        String inGroup, int index) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotToolbarButton} with the specified
     *         <code>tooltip</code> with the specified <code>inGroup</code>.
     */
    public RemoteBotToolbarButton toolbarButtonWithTooltipInGroup(String tooltip,
        String inGroup) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToolbarButton} with the specified
     *         <code>tooltip</code> with the specified <code>inGroup</code>.
     */

    public RemoteBotToolbarButton toolbarButtonWithTooltipInGroup(String tooltip,
        String inGroup, int index) throws RemoteException;

    /**********************************************
     * 
     * Widget text
     * 
     **********************************************/

    /**
     * @param label
     *            the label on the widget.
     * @return a {@link RemoteBotText} with the specified <code>label</code>.
     */
    public RemoteBotText textWithLabel(String label) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotText} with the specified <code>label</code>.
     */

    public RemoteBotText textWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @return a {@link RemoteBotText} with the specified <code>text</code>.
     */
    public RemoteBotText text(String text) throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotText} with the specified <code>text</code>.
     */

    public RemoteBotText text(String text, int index) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @return a {@link RemoteBotText} with the specified <code>tooltip</code>.
     */
    public RemoteBotText textWithTooltip(String tooltip) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotText} with the specified <code>tooltip</code>.
     */

    public RemoteBotText textWithTooltip(String tooltip, int index)
        throws RemoteException;

    /**
     * @param message
     *            the message on the widget.
     * @return a {@link RemoteBotText} with the specified <code>message</code>.
     */
    public RemoteBotText textWithMessage(String message) throws RemoteException;

    /**
     * @param message
     *            the message on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotText} with the specified <code>message</code>.
     */

    public RemoteBotText textWithMessage(String message, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link RemoteBotText} with the specified <code>key/value</code>.
     */
    public RemoteBotText textWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotText} with the specified <code>key/value</code>.
     */

    public RemoteBotText textWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link RemoteBotText} with the specified <code>value</code>.
     */
    public RemoteBotText textWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotText} with the specified <code>value</code>.
     */

    public RemoteBotText textWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotText} with the specified <code>inGroup</code>.
     */
    public RemoteBotText textInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotText} with the specified <code>inGroup</code>.
     */

    public RemoteBotText textInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link RemoteBotText} with the specified <code>none</code>.
     */
    public RemoteBotText text() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotText} with the specified <code>none</code>.
     */

    public RemoteBotText text(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotText} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public RemoteBotText textWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotText} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */

    public RemoteBotText textWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotText} with the specified <code>text</code> with
     *         the specified <code>inGroup</code>.
     */
    public RemoteBotText textInGroup(String text, String inGroup)
        throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotText} with the specified <code>text</code> with
     *         the specified <code>inGroup</code>.
     */

    public RemoteBotText textInGroup(String text, String inGroup, int index)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotText} with the specified <code>tooltip</code> with
     *         the specified <code>inGroup</code>.
     */
    public RemoteBotText textWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotText} with the specified <code>tooltip</code> with
     *         the specified <code>inGroup</code>.
     */
    public RemoteBotText textWithTooltipInGroup(String tooltip, String inGroup,
        int index) throws RemoteException;

    /**********************************************
     * 
     * Widget table
     * 
     **********************************************/
    public boolean existsTable() throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @return a {@link RemoteBotTable} with the specified <code>label</code>.
     */
    public RemoteBotTable tableWithLabel(String label) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotTable} with the specified <code>label</code>.
     */

    public RemoteBotTable tableWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link RemoteBotTable} with the specified <code>key/value</code>.
     */
    public RemoteBotTable tableWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotTable} with the specified <code>key/value</code>.
     */

    public RemoteBotTable tableWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link RemoteBotTable} with the specified <code>value</code>.
     */
    public RemoteBotTable tableWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotTable} with the specified <code>value</code>.
     */

    public RemoteBotTable tableWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotTable} with the specified <code>inGroup</code>.
     */
    public RemoteBotTable tableInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotTable} with the specified <code>inGroup</code>.
     */

    public RemoteBotTable tableInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link RemoteBotTable} with the specified <code>none</code>.
     */
    public RemoteBotTable table() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotTable} with the specified <code>none</code>.
     */

    public RemoteBotTable table(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotTable} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public RemoteBotTable tableWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**********************************************
     * 
     * Widget menu
     * 
     **********************************************/

    /**
     * @param text
     *            the text on the menu.
     * @return a menu item that matches the specified text.
     */
    public RemoteBotMenu menu(String text) throws RemoteException;

    /**
     * @param text
     *            the text on the menu.
     * @param index
     *            the index of the menu, in case there are multiple menus with
     *            the same text.
     * @return a menu item that matches the specified text.
     */
    public RemoteBotMenu menu(String text, int index) throws RemoteException;

    /**
     * @param value
     *            the value of the id.
     * @return a wrapper around a @{link Menu} with the specified key/value pair
     *         for its id.
     */
    public RemoteBotMenu menuWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value of the id.
     * @param index
     *            the index of the menu item, in case there are multiple shells
     *            with the same text.
     * @return a wrapper around a @{link Menu} with the specified key/value pair
     *         for its id.
     */
    public RemoteBotMenu menuWithId(String value, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key of the id.
     * @param value
     *            the value of the id.
     * @return a wrapper around a @{link Menu} with the specified key/value pair
     *         for its id.
     */
    public RemoteBotMenu menuWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key of the id.
     * @param value
     *            the value of the id.
     * @param index
     *            the index of the menu item, in case there are multiple shells
     *            with the same text.
     * @return a wrapper around a @{link Menu} with the specified key/value pair
     *         for its id.
     */
    public RemoteBotMenu menuWithId(String key, String value, int index)
        throws RemoteException;

    /**********************************************
     * 
     * Widget list
     * 
     **********************************************/
    /**
     * @param label
     *            the label on the widget.
     * @return a {@link RemoteBotList} with the specified <code>label</code>.
     */
    public RemoteBotList listWithLabel(String label) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotList} with the specified <code>label</code>.
     */

    public RemoteBotList listWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link RemoteBotList} with the specified <code>key/value</code>.
     */
    public RemoteBotList listWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotList} with the specified <code>key/value</code>.
     */

    public RemoteBotList listWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link RemoteBotList} with the specified <code>value</code>.
     */
    public RemoteBotList listWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotList} with the specified <code>value</code>.
     */

    public RemoteBotList listWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotList} with the specified <code>inGroup</code>.
     */
    public RemoteBotList listInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotList} with the specified <code>inGroup</code>.
     */

    public RemoteBotList listInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link RemoteBotList} with the specified <code>none</code>.
     */
    public RemoteBotList list() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotList} with the specified <code>none</code>.
     */

    public RemoteBotList list(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotList} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public RemoteBotList listWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotList} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */

    public RemoteBotList listWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException;

    /**
     * 
     * @return the title of the active shell.
     * @throws RemoteException
     */
    public String getTextOfActiveShell() throws RemoteException;

    public RemoteBotShell activeShell() throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @return a {@link RemoteBotCheckBox} with the specified <code>label</code>.
     */
    public RemoteBotCheckBox checkBoxWithLabel(String label)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCheckBox} with the specified <code>label</code>.
     */

    public RemoteBotCheckBox checkBoxWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @return a {@link RemoteBotCheckBox} with the specified
     *         <code>mnemonicText</code>.
     */
    public RemoteBotCheckBox checkBox(String mnemonicText) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCheckBox} with the specified
     *         <code>mnemonicText</code>.
     */

    public RemoteBotCheckBox checkBox(String mnemonicText, int index)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @return a {@link RemoteBotCheckBox} with the specified <code>tooltip</code>.
     */
    public RemoteBotCheckBox checkBoxWithTooltip(String tooltip)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCheckBox} with the specified <code>tooltip</code>.
     */

    public RemoteBotCheckBox checkBoxWithTooltip(String tooltip, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link RemoteBotCheckBox} with the specified
     *         <code>key/value</code>.
     */
    public RemoteBotCheckBox checkBoxWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCheckBox} with the specified
     *         <code>key/value</code>.
     */

    public RemoteBotCheckBox checkBoxWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link RemoteBotCheckBox} with the specified <code>value</code>.
     */
    public RemoteBotCheckBox checkBoxWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCheckBox} with the specified <code>value</code>.
     */

    public RemoteBotCheckBox checkBoxWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotCheckBox} with the specified <code>inGroup</code>.
     */
    public RemoteBotCheckBox checkBoxInGroup(String inGroup)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCheckBox} with the specified <code>inGroup</code>.
     */

    public RemoteBotCheckBox checkBoxInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link RemoteBotCheckBox} with the specified <code>none</code>.
     */
    public RemoteBotCheckBox checkBox() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCheckBox} with the specified <code>none</code>.
     */

    public RemoteBotCheckBox checkBox(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotCheckBox} with the specified <code>label</code>
     *         with the specified <code>inGroup</code>.
     */
    public RemoteBotCheckBox checkBoxWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCheckBox} with the specified <code>label</code>
     *         with the specified <code>inGroup</code>.
     */

    public RemoteBotCheckBox checkBoxWithLabelInGroup(String label,
        String inGroup, int index) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotCheckBox} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */
    public RemoteBotCheckBox checkBoxInGroup(String mnemonicText, String inGroup)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCheckBox} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */

    public RemoteBotCheckBox checkBoxInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotCheckBox} with the specified <code>tooltip</code>
     *         with the specified <code>inGroup</code>.
     */
    public RemoteBotCheckBox checkBoxWithTooltipInGroup(String tooltip,
        String inGroup) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotCheckBox} with the specified <code>tooltip</code>
     *         with the specified <code>inGroup</code>.
     */

    public RemoteBotCheckBox checkBoxWithTooltipInGroup(String tooltip,
        String inGroup, int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @return a {@link RemoteBotRadio} with the specified <code>label</code>.
     */
    public RemoteBotRadio radioWithLabel(String label) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotRadio} with the specified <code>label</code>.
     */

    public RemoteBotRadio radioWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @return a {@link RemoteBotRadio} with the specified
     *         <code>mnemonicText</code>.
     */
    public RemoteBotRadio radio(String mnemonicText) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotRadio} with the specified
     *         <code>mnemonicText</code>.
     */

    public RemoteBotRadio radio(String mnemonicText, int index)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @return a {@link RemoteBotRadio} with the specified <code>tooltip</code>.
     */
    public RemoteBotRadio radioWithTooltip(String tooltip) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotRadio} with the specified <code>tooltip</code>.
     */

    public RemoteBotRadio radioWithTooltip(String tooltip, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link RemoteBotRadio} with the specified <code>key/value</code>.
     */
    public RemoteBotRadio radioWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotRadio} with the specified <code>key/value</code>.
     */

    public RemoteBotRadio radioWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link RemoteBotRadio} with the specified <code>value</code>.
     */
    public RemoteBotRadio radioWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotRadio} with the specified <code>value</code>.
     */

    public RemoteBotRadio radioWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotRadio} with the specified <code>inGroup</code>.
     */
    public RemoteBotRadio radioInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotRadio} with the specified <code>inGroup</code>.
     */

    public RemoteBotRadio radioInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link RemoteBotRadio} with the specified <code>none</code>.
     */
    public RemoteBotRadio radio() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotRadio} with the specified <code>none</code>.
     */

    public RemoteBotRadio radio(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotRadio} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public RemoteBotRadio radioWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotRadio} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */

    public RemoteBotRadio radioWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotRadio} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */
    public RemoteBotRadio radioInGroup(String mnemonicText, String inGroup)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotRadio} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */

    public RemoteBotRadio radioInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotRadio} with the specified <code>tooltip</code>
     *         with the specified <code>inGroup</code>.
     */
    public RemoteBotRadio radioWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotRadio} with the specified <code>tooltip</code>
     *         with the specified <code>inGroup</code>.
     */

    public RemoteBotRadio radioWithTooltipInGroup(String tooltip, String inGroup,
        int index) throws RemoteException;

    /**********************************************
     * 
     * Widget toggleButton
     * 
     **********************************************/
    /**
     * @param label
     *            the label on the widget.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>label</code>.
     */
    public RemoteBotToggleButton toggleButtonWithLabel(String label)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>label</code>.
     */

    public RemoteBotToggleButton toggleButtonWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>mnemonicText</code>.
     */
    public RemoteBotToggleButton toggleButton(String mnemonicText)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>mnemonicText</code>.
     */

    public RemoteBotToggleButton toggleButton(String mnemonicText, int index)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>tooltip</code>.
     */
    public RemoteBotToggleButton toggleButtonWithTooltip(String tooltip)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>tooltip</code>.
     */

    public RemoteBotToggleButton toggleButtonWithTooltip(String tooltip, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>key/value</code>.
     */
    public RemoteBotToggleButton toggleButtonWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>key/value</code>.
     */

    public RemoteBotToggleButton toggleButtonWithId(String key, String value,
        int index) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>value</code>.
     */
    public RemoteBotToggleButton toggleButtonWithId(String value)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>value</code>.
     */

    public RemoteBotToggleButton toggleButtonWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>inGroup</code>.
     */
    public RemoteBotToggleButton toggleButtonInGroup(String inGroup)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>inGroup</code>.
     */

    public RemoteBotToggleButton toggleButtonInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link RemoteBotToggleButton} with the specified <code>none</code>
     *         .
     */
    public RemoteBotToggleButton toggleButton() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToggleButton} with the specified <code>none</code>
     *         .
     */

    public RemoteBotToggleButton toggleButton(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>label</code> with the specified <code>inGroup</code>.
     */
    public RemoteBotToggleButton toggleButtonWithLabelInGroup(String label,
        String inGroup) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>label</code> with the specified <code>inGroup</code>.
     */

    public RemoteBotToggleButton toggleButtonWithLabelInGroup(String label,
        String inGroup, int index) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */
    public RemoteBotToggleButton toggleButtonInGroup(String mnemonicText,
        String inGroup) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */

    public RemoteBotToggleButton toggleButtonInGroup(String mnemonicText,
        String inGroup, int index) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>tooltip</code> with the specified <code>inGroup</code>.
     */
    public RemoteBotToggleButton toggleButtonWithTooltipInGroup(String tooltip,
        String inGroup) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link RemoteBotToggleButton} with the specified
     *         <code>tooltip</code> with the specified <code>inGroup</code>.
     */

    public RemoteBotToggleButton toggleButtonWithTooltipInGroup(String tooltip,
        String inGroup, int index) throws RemoteException;

    /**********************************************
     * 
     * Wait until
     * 
     **********************************************/
    public void waitUntil(ICondition condition) throws RemoteException;

    public void waitLongUntil(ICondition condition) throws RemoteException;

    public void waitShortUntil(ICondition condition) throws RemoteException;

    /**********************************************
     * 
     * Others
     * 
     **********************************************/

    public void sleep(long millis) throws RemoteException;

    /**
     * @see SWTBot#captureScreenshot(String)
     * @throws RemoteException
     */
    public void captureScreenshot(String fileName) throws RemoteException;

    public String getPathToScreenShot() throws RemoteException;

}
