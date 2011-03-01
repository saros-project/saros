package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotCCombo;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotCheckBox;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotCombo;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotLabel;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotList;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotRadio;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotStyledText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotToggleButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotToolbarButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;

public interface STFBot extends Remote {

    /**********************************************
     * 
     * Widget tree
     * 
     **********************************************/
    /**
     * @see SWTBot#tree()
     */
    public STFBotTree tree() throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @return a {@link STFBotTree} with the specified <code>label</code>.
     */
    public STFBotTree treeWithLabel(String label) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotTree} with the specified <code>label</code>.
     */

    public STFBotTree treeWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link STFBotTree} with the specified <code>key/value</code>.
     */
    public STFBotTree treeWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotTree} with the specified <code>key/value</code>.
     */

    public STFBotTree treeWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link STFBotTree} with the specified <code>value</code>.
     */
    public STFBotTree treeWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotTree} with the specified <code>value</code>.
     */

    public STFBotTree treeWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotTree} with the specified <code>inGroup</code>.
     */
    public STFBotTree treeInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotTree} with the specified <code>inGroup</code>.
     */

    public STFBotTree treeInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotTree} with the specified <code>none</code>.
     */

    public STFBotTree tree(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotTree} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotTree treeWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotTree} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */

    public STFBotTree treeWithLabelInGroup(String label, String inGroup,
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
    public STFBotShell shell(String title) throws RemoteException;

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
    public void waitsUntilShellIsClosed(final String title)
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
    public STFBotButton buttonWithLabel(String label) throws RemoteException;

    /**
     * @see SWTBot#buttonWithLabelInGroup(String, String, int)
     */
    public STFBotButton buttonWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @see SWTBot#button(String)
     */
    public STFBotButton button(String mnemonicText) throws RemoteException;

    /**
     * @see SWTBot#button(String, int)
     */
    public STFBotButton button(String mnemonicText, int index)
        throws RemoteException;

    /**
     * @see SWTBot#buttonWithTooltip(String)
     */
    public STFBotButton buttonWithTooltip(String tooltip)
        throws RemoteException;

    public STFBotButton buttonWithTooltip(String tooltip, int index)
        throws RemoteException;

    /**
     * @see SWTBot#buttonWithId(String, String)
     */
    public STFBotButton buttonWithId(String key, String value)
        throws RemoteException;

    /**
     * @see SWTBot#buttonWithId(String, String, int)
     */
    public STFBotButton buttonWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @see SWTBot#buttonWithId(String)
     */
    public STFBotButton buttonWithId(String value) throws RemoteException;

    /**
     * @see SWTBot#buttonWithId(String,int)
     */
    public STFBotButton buttonWithId(String value, int index)
        throws RemoteException;

    /**
     * @see SWTBot#buttonInGroup(String)
     */
    public STFBotButton buttonInGroup(String inGroup) throws RemoteException;

    /**
     * @see SWTBot#buttonInGroup(String,int)
     */
    public STFBotButton buttonInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @see SWTBot#button()
     */
    public STFBotButton button() throws RemoteException;

    /**
     * @see SWTBot#button(int)
     */
    public STFBotButton button(int index) throws RemoteException;

    /**
     * @see SWTBot#buttonWithLabelInGroup(String, String)
     */
    public STFBotButton buttonWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @see SWTBot#buttonWithLabelInGroup(String, String,int)
     */
    public STFBotButton buttonWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException;

    /**
     * @see SWTBot#buttonInGroup(String, String)
     */
    public STFBotButton buttonInGroup(String mnemonicText, String inGroup)
        throws RemoteException;

    /**
     * @see SWTBot#buttonInGroup(String, String, int) .
     */
    public STFBotButton buttonInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException;

    /**
     * @see SWTBot#buttonWithTooltipInGroup(String, String)
     */
    public STFBotButton buttonWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException;

    /**
     * @see SWTBot#buttonWithTooltipInGroup(String, String, int)
     */
    public STFBotButton buttonWithTooltipInGroup(String tooltip,
        String inGroup, int index) throws RemoteException;

    /**********************************************
     * 
     * Widget label
     * 
     **********************************************/

    public STFBotLabel label() throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @return a {@link STFBotLabel} with the specified
     *         <code>mnemonicText</code>.
     */
    public STFBotLabel label(String mnemonicText) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotLabel} with the specified
     *         <code>mnemonicText</code>.
     */

    public STFBotLabel label(String mnemonicText, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link STFBotLabel} with the specified <code>key/value</code>.
     */
    public STFBotLabel labelWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotLabel} with the specified <code>key/value</code>.
     */

    public STFBotLabel labelWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link STFBotLabel} with the specified <code>value</code>.
     */
    public STFBotLabel labelWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotLabel} with the specified <code>value</code>.
     */

    public STFBotLabel labelWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotLabel} with the specified <code>inGroup</code>.
     */
    public STFBotLabel labelInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotLabel} with the specified <code>inGroup</code>.
     */

    public STFBotLabel labelInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotLabel} with the specified <code>none</code>.
     */

    public STFBotLabel label(int index) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotLabel} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */
    public STFBotLabel labelInGroup(String mnemonicText, String inGroup)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotLabel} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */

    public STFBotLabel labelInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException;

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
    public STFBotStyledText styledTextWithLabel(String label)
        throws RemoteException;

    /**
     * @see SWTBot#styledTextWithLabel(String, int)
     */
    public STFBotStyledText styledTextWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @see SWTBot#styledText(String)
     */
    public STFBotStyledText styledText(String text) throws RemoteException;

    /**
     * @see SWTBot#styledText(String, int)
     */
    public STFBotStyledText styledText(String text, int index)
        throws RemoteException;

    /**
     * @see SWTBot#styledTextWithId(String, String)
     */
    public STFBotStyledText styledTextWithId(String key, String value)
        throws RemoteException;

    /**
     * @see SWTBot#styledTextWithId(String, String, int)
     */
    public STFBotStyledText styledTextWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @see SWTBot#styledTextWithId(String)
     */
    public STFBotStyledText styledTextWithId(String value)
        throws RemoteException;

    /**
     * @see SWTBot#styledTextWithId(String, int)
     */
    public STFBotStyledText styledTextWithId(String value, int index)
        throws RemoteException;

    /**
     * @see SWTBot#styledTextInGroup(String)
     */
    public STFBotStyledText styledTextInGroup(String inGroup)
        throws RemoteException;

    /**
     * @see SWTBot#styledTextInGroup(String, int)
     */
    public STFBotStyledText styledTextInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @see SWTBot#styledText()
     */
    public STFBotStyledText styledText() throws RemoteException;

    /**
     * @see SWTBot#styledText(int)
     */
    public STFBotStyledText styledText(int index) throws RemoteException;

    /**
     * @see SWTBot#styledTextWithLabelInGroup(String, String)
     */
    public STFBotStyledText styledTextWithLabelInGroup(String label,
        String inGroup) throws RemoteException;

    /**
     * @see SWTBot#styledTextWithLabelInGroup(String, String, int)
     */
    public STFBotStyledText styledTextWithLabelInGroup(String label,
        String inGroup, int index) throws RemoteException;

    /**
     * @see SWTBot#styledTextInGroup(String, String)
     */
    public STFBotStyledText styledTextInGroup(String text, String inGroup)
        throws RemoteException;

    /**
     * @see SWTBot#styledTextInGroup(String, String, int)
     */
    public STFBotStyledText styledTextInGroup(String text, String inGroup,
        int index) throws RemoteException;

    /**********************************************
     * 
     * Widget comboBox
     * 
     **********************************************/
    /**
     * @param label
     *            the label on the widget.
     * @return a {@link STFBotCombo} with the specified <code>label</code>.
     */
    public STFBotCombo comboBoxWithLabel(String label) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCombo} with the specified <code>label</code>.
     */

    public STFBotCombo comboBoxWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @return a {@link STFBotCombo} with the specified <code>text</code>.
     */
    public STFBotCombo comboBox(String text) throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCombo} with the specified <code>text</code>.
     */

    public STFBotCombo comboBox(String text, int index) throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link STFBotCombo} with the specified <code>key/value</code>.
     */
    public STFBotCombo comboBoxWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCombo} with the specified <code>key/value</code>.
     */

    public STFBotCombo comboBoxWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link STFBotCombo} with the specified <code>value</code>.
     */
    public STFBotCombo comboBoxWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCombo} with the specified <code>value</code>.
     */

    public STFBotCombo comboBoxWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotCombo} with the specified <code>inGroup</code>.
     */
    public STFBotCombo comboBoxInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCombo} with the specified <code>inGroup</code>.
     */

    public STFBotCombo comboBoxInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link STFBotCombo} with the specified <code>none</code>.
     */
    public STFBotCombo comboBox() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCombo} with the specified <code>none</code>.
     */

    public STFBotCombo comboBox(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotCombo} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotCombo comboBoxWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCombo} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */

    public STFBotCombo comboBoxWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotCombo} with the specified <code>text</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotCombo comboBoxInGroup(String text, String inGroup)
        throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCombo} with the specified <code>text</code> with
     *         the specified <code>inGroup</code>.
     */

    public STFBotCombo comboBoxInGroup(String text, String inGroup, int index)
        throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @return a {@link STFBotCCombo} with the specified <code>text</code>.
     */
    public STFBotCCombo ccomboBox(String text) throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCCombo} with the specified <code>text</code>.
     */

    public STFBotCCombo ccomboBox(String text, int index)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @return a {@link STFBotCCombo} with the specified <code>label</code>.
     */
    public STFBotCCombo ccomboBoxWithLabel(String label) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCCombo} with the specified <code>label</code>.
     */
    public STFBotCCombo ccomboBoxWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link STFBotCCombo} with the specified <code>key/value</code>.
     */
    public STFBotCCombo ccomboBoxWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCCombo} with the specified <code>key/value</code>.
     */
    public STFBotCCombo ccomboBoxWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link STFBotCCombo} with the specified <code>value</code>.
     */
    public STFBotCCombo ccomboBoxWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCCombo} with the specified <code>value</code>.
     */
    public STFBotCCombo ccomboBoxWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotCCombo} with the specified <code>inGroup</code>.
     */
    public STFBotCCombo ccomboBoxInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCCombo} with the specified <code>inGroup</code>.
     */
    public STFBotCCombo ccomboBoxInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link STFBotCCombo} with the specified <code>none</code>.
     */
    public STFBotCCombo ccomboBox() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCCombo} with the specified <code>none</code>.
     */
    public STFBotCCombo ccomboBox(int index) throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotCCombo} with the specified <code>text</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotCCombo ccomboBoxInGroup(String text, String inGroup)
        throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCCombo} with the specified <code>text</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotCCombo ccomboBoxInGroup(String text, String inGroup, int index)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotCCombo} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotCCombo ccomboBoxWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCCombo} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */

    public STFBotCCombo ccomboBoxWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException;

    public STFBotToolbarButton toolbarButton() throws RemoteException;

    public STFBotToolbarButton toolbarButton(int index) throws RemoteException;

    public boolean existsToolbarButton() throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @return a {@link STFBotToolbarButton} with the specified
     *         <code>mnemonicText</code>.
     */
    public STFBotToolbarButton toolbarButton(String mnemonicText)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToolbarButton} with the specified
     *         <code>mnemonicText</code>.
     */

    public STFBotToolbarButton toolbarButton(String mnemonicText, int index)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @return a {@link STFBotToolbarButton} with the specified
     *         <code>tooltip</code>.
     */
    public STFBotToolbarButton toolbarButtonWithTooltip(String tooltip)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToolbarButton} with the specified
     *         <code>tooltip</code>.
     */

    public STFBotToolbarButton toolbarButtonWithTooltip(String tooltip,
        int index) throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link STFBotToolbarButton} with the specified
     *         <code>key/value</code>.
     */
    public STFBotToolbarButton toolbarButtonWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToolbarButton} with the specified
     *         <code>key/value</code>.
     */

    public STFBotToolbarButton toolbarButtonWithId(String key, String value,
        int index) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link STFBotToolbarButton} with the specified
     *         <code>value</code>.
     */
    public STFBotToolbarButton toolbarButtonWithId(String value)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToolbarButton} with the specified
     *         <code>value</code>.
     */

    public STFBotToolbarButton toolbarButtonWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotToolbarButton} with the specified
     *         <code>inGroup</code>.
     */
    public STFBotToolbarButton toolbarButtonInGroup(String inGroup)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToolbarButton} with the specified
     *         <code>inGroup</code>.
     */

    public STFBotToolbarButton toolbarButtonInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotToolbarButton} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */
    public STFBotToolbarButton toolbarButtonInGroup(String mnemonicText,
        String inGroup) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToolbarButton} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */

    public STFBotToolbarButton toolbarButtonInGroup(String mnemonicText,
        String inGroup, int index) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotToolbarButton} with the specified
     *         <code>tooltip</code> with the specified <code>inGroup</code>.
     */
    public STFBotToolbarButton toolbarButtonWithTooltipInGroup(String tooltip,
        String inGroup) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToolbarButton} with the specified
     *         <code>tooltip</code> with the specified <code>inGroup</code>.
     */

    public STFBotToolbarButton toolbarButtonWithTooltipInGroup(String tooltip,
        String inGroup, int index) throws RemoteException;

    /**********************************************
     * 
     * Widget text
     * 
     **********************************************/

    /**
     * @param label
     *            the label on the widget.
     * @return a {@link STFBotText} with the specified <code>label</code>.
     */
    public STFBotText textWithLabel(String label) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotText} with the specified <code>label</code>.
     */

    public STFBotText textWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @return a {@link STFBotText} with the specified <code>text</code>.
     */
    public STFBotText text(String text) throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotText} with the specified <code>text</code>.
     */

    public STFBotText text(String text, int index) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @return a {@link STFBotText} with the specified <code>tooltip</code>.
     */
    public STFBotText textWithTooltip(String tooltip) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotText} with the specified <code>tooltip</code>.
     */

    public STFBotText textWithTooltip(String tooltip, int index)
        throws RemoteException;

    /**
     * @param message
     *            the message on the widget.
     * @return a {@link STFBotText} with the specified <code>message</code>.
     */
    public STFBotText textWithMessage(String message) throws RemoteException;

    /**
     * @param message
     *            the message on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotText} with the specified <code>message</code>.
     */

    public STFBotText textWithMessage(String message, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link STFBotText} with the specified <code>key/value</code>.
     */
    public STFBotText textWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotText} with the specified <code>key/value</code>.
     */

    public STFBotText textWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link STFBotText} with the specified <code>value</code>.
     */
    public STFBotText textWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotText} with the specified <code>value</code>.
     */

    public STFBotText textWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotText} with the specified <code>inGroup</code>.
     */
    public STFBotText textInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotText} with the specified <code>inGroup</code>.
     */

    public STFBotText textInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link STFBotText} with the specified <code>none</code>.
     */
    public STFBotText text() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotText} with the specified <code>none</code>.
     */

    public STFBotText text(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotText} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotText textWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotText} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */

    public STFBotText textWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotText} with the specified <code>text</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotText textInGroup(String text, String inGroup)
        throws RemoteException;

    /**
     * @param text
     *            the text on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotText} with the specified <code>text</code> with
     *         the specified <code>inGroup</code>.
     */

    public STFBotText textInGroup(String text, String inGroup, int index)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotText} with the specified <code>tooltip</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotText textWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotText} with the specified <code>tooltip</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotText textWithTooltipInGroup(String tooltip, String inGroup,
        int index) throws RemoteException;

    /**********************************************
     * 
     * Widget table
     * 
     **********************************************/
    /**
     * @param label
     *            the label on the widget.
     * @return a {@link STFBotTable} with the specified <code>label</code>.
     */
    public STFBotTable tableWithLabel(String label) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotTable} with the specified <code>label</code>.
     */

    public STFBotTable tableWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link STFBotTable} with the specified <code>key/value</code>.
     */
    public STFBotTable tableWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotTable} with the specified <code>key/value</code>.
     */

    public STFBotTable tableWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link STFBotTable} with the specified <code>value</code>.
     */
    public STFBotTable tableWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotTable} with the specified <code>value</code>.
     */

    public STFBotTable tableWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotTable} with the specified <code>inGroup</code>.
     */
    public STFBotTable tableInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotTable} with the specified <code>inGroup</code>.
     */

    public STFBotTable tableInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link STFBotTable} with the specified <code>none</code>.
     */
    public STFBotTable table() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotTable} with the specified <code>none</code>.
     */

    public STFBotTable table(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotTable} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotTable tableWithLabelInGroup(String label, String inGroup)
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
    public STFBotMenu menu(String text) throws RemoteException;

    /**
     * @param text
     *            the text on the menu.
     * @param index
     *            the index of the menu, in case there are multiple menus with
     *            the same text.
     * @return a menu item that matches the specified text.
     */
    public STFBotMenu menu(String text, int index) throws RemoteException;

    /**
     * @param value
     *            the value of the id.
     * @return a wrapper around a @{link Menu} with the specified key/value pair
     *         for its id.
     */
    public STFBotMenu menuWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value of the id.
     * @param index
     *            the index of the menu item, in case there are multiple shells
     *            with the same text.
     * @return a wrapper around a @{link Menu} with the specified key/value pair
     *         for its id.
     */
    public STFBotMenu menuWithId(String value, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key of the id.
     * @param value
     *            the value of the id.
     * @return a wrapper around a @{link Menu} with the specified key/value pair
     *         for its id.
     */
    public STFBotMenu menuWithId(String key, String value)
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
    public STFBotMenu menuWithId(String key, String value, int index)
        throws RemoteException;

    /**********************************************
     * 
     * Widget list
     * 
     **********************************************/
    /**
     * @param label
     *            the label on the widget.
     * @return a {@link STFBotList} with the specified <code>label</code>.
     */
    public STFBotList listWithLabel(String label) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotList} with the specified <code>label</code>.
     */

    public STFBotList listWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link STFBotList} with the specified <code>key/value</code>.
     */
    public STFBotList listWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotList} with the specified <code>key/value</code>.
     */

    public STFBotList listWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link STFBotList} with the specified <code>value</code>.
     */
    public STFBotList listWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotList} with the specified <code>value</code>.
     */

    public STFBotList listWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotList} with the specified <code>inGroup</code>.
     */
    public STFBotList listInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotList} with the specified <code>inGroup</code>.
     */

    public STFBotList listInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link STFBotList} with the specified <code>none</code>.
     */
    public STFBotList list() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotList} with the specified <code>none</code>.
     */

    public STFBotList list(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotList} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotList listWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotList} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */

    public STFBotList listWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException;

    /**
     * 
     * @return the title of the active shell.
     * @throws RemoteException
     */
    public String getTextOfActiveShell() throws RemoteException;

    public STFBotShell activeShell() throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @return a {@link STFBotCheckBox} with the specified <code>label</code>.
     */
    public STFBotCheckBox checkBoxWithLabel(String label)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCheckBox} with the specified <code>label</code>.
     */

    public STFBotCheckBox checkBoxWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @return a {@link STFBotCheckBox} with the specified
     *         <code>mnemonicText</code>.
     */
    public STFBotCheckBox checkBox(String mnemonicText) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCheckBox} with the specified
     *         <code>mnemonicText</code>.
     */

    public STFBotCheckBox checkBox(String mnemonicText, int index)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @return a {@link STFBotCheckBox} with the specified <code>tooltip</code>.
     */
    public STFBotCheckBox checkBoxWithTooltip(String tooltip)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCheckBox} with the specified <code>tooltip</code>.
     */

    public STFBotCheckBox checkBoxWithTooltip(String tooltip, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link STFBotCheckBox} with the specified
     *         <code>key/value</code>.
     */
    public STFBotCheckBox checkBoxWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCheckBox} with the specified
     *         <code>key/value</code>.
     */

    public STFBotCheckBox checkBoxWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link STFBotCheckBox} with the specified <code>value</code>.
     */
    public STFBotCheckBox checkBoxWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCheckBox} with the specified <code>value</code>.
     */

    public STFBotCheckBox checkBoxWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotCheckBox} with the specified <code>inGroup</code>.
     */
    public STFBotCheckBox checkBoxInGroup(String inGroup)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCheckBox} with the specified <code>inGroup</code>.
     */

    public STFBotCheckBox checkBoxInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link STFBotCheckBox} with the specified <code>none</code>.
     */
    public STFBotCheckBox checkBox() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCheckBox} with the specified <code>none</code>.
     */

    public STFBotCheckBox checkBox(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotCheckBox} with the specified <code>label</code>
     *         with the specified <code>inGroup</code>.
     */
    public STFBotCheckBox checkBoxWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCheckBox} with the specified <code>label</code>
     *         with the specified <code>inGroup</code>.
     */

    public STFBotCheckBox checkBoxWithLabelInGroup(String label,
        String inGroup, int index) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotCheckBox} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */
    public STFBotCheckBox checkBoxInGroup(String mnemonicText, String inGroup)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCheckBox} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */

    public STFBotCheckBox checkBoxInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotCheckBox} with the specified <code>tooltip</code>
     *         with the specified <code>inGroup</code>.
     */
    public STFBotCheckBox checkBoxWithTooltipInGroup(String tooltip,
        String inGroup) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotCheckBox} with the specified <code>tooltip</code>
     *         with the specified <code>inGroup</code>.
     */

    public STFBotCheckBox checkBoxWithTooltipInGroup(String tooltip,
        String inGroup, int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @return a {@link STFBotRadio} with the specified <code>label</code>.
     */
    public STFBotRadio radioWithLabel(String label) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotRadio} with the specified <code>label</code>.
     */

    public STFBotRadio radioWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @return a {@link STFBotRadio} with the specified
     *         <code>mnemonicText</code>.
     */
    public STFBotRadio radio(String mnemonicText) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotRadio} with the specified
     *         <code>mnemonicText</code>.
     */

    public STFBotRadio radio(String mnemonicText, int index)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @return a {@link STFBotRadio} with the specified <code>tooltip</code>.
     */
    public STFBotRadio radioWithTooltip(String tooltip) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotRadio} with the specified <code>tooltip</code>.
     */

    public STFBotRadio radioWithTooltip(String tooltip, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link STFBotRadio} with the specified <code>key/value</code>.
     */
    public STFBotRadio radioWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotRadio} with the specified <code>key/value</code>.
     */

    public STFBotRadio radioWithId(String key, String value, int index)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link STFBotRadio} with the specified <code>value</code>.
     */
    public STFBotRadio radioWithId(String value) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotRadio} with the specified <code>value</code>.
     */

    public STFBotRadio radioWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotRadio} with the specified <code>inGroup</code>.
     */
    public STFBotRadio radioInGroup(String inGroup) throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotRadio} with the specified <code>inGroup</code>.
     */

    public STFBotRadio radioInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link STFBotRadio} with the specified <code>none</code>.
     */
    public STFBotRadio radio() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotRadio} with the specified <code>none</code>.
     */

    public STFBotRadio radio(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotRadio} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */
    public STFBotRadio radioWithLabelInGroup(String label, String inGroup)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotRadio} with the specified <code>label</code> with
     *         the specified <code>inGroup</code>.
     */

    public STFBotRadio radioWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotRadio} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */
    public STFBotRadio radioInGroup(String mnemonicText, String inGroup)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotRadio} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */

    public STFBotRadio radioInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotRadio} with the specified <code>tooltip</code>
     *         with the specified <code>inGroup</code>.
     */
    public STFBotRadio radioWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotRadio} with the specified <code>tooltip</code>
     *         with the specified <code>inGroup</code>.
     */

    public STFBotRadio radioWithTooltipInGroup(String tooltip, String inGroup,
        int index) throws RemoteException;

    /**********************************************
     * 
     * Widget toggleButton
     * 
     **********************************************/
    /**
     * @param label
     *            the label on the widget.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>label</code>.
     */
    public STFBotToggleButton toggleButtonWithLabel(String label)
        throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>label</code>.
     */

    public STFBotToggleButton toggleButtonWithLabel(String label, int index)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>mnemonicText</code>.
     */
    public STFBotToggleButton toggleButton(String mnemonicText)
        throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>mnemonicText</code>.
     */

    public STFBotToggleButton toggleButton(String mnemonicText, int index)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>tooltip</code>.
     */
    public STFBotToggleButton toggleButtonWithTooltip(String tooltip)
        throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>tooltip</code>.
     */

    public STFBotToggleButton toggleButtonWithTooltip(String tooltip, int index)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>key/value</code>.
     */
    public STFBotToggleButton toggleButtonWithId(String key, String value)
        throws RemoteException;

    /**
     * @param key
     *            the key set on the widget.
     * @param value
     *            the value for the key.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>key/value</code>.
     */

    public STFBotToggleButton toggleButtonWithId(String key, String value,
        int index) throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>value</code>.
     */
    public STFBotToggleButton toggleButtonWithId(String value)
        throws RemoteException;

    /**
     * @param value
     *            the value for the key
     *            {@link org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY}
     *            .
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>value</code>.
     */

    public STFBotToggleButton toggleButtonWithId(String value, int index)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>inGroup</code>.
     */
    public STFBotToggleButton toggleButtonInGroup(String inGroup)
        throws RemoteException;

    /**
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>inGroup</code>.
     */

    public STFBotToggleButton toggleButtonInGroup(String inGroup, int index)
        throws RemoteException;

    /**
     * @return a {@link STFBotToggleButton} with the specified <code>none</code>
     *         .
     */
    public STFBotToggleButton toggleButton() throws RemoteException;

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToggleButton} with the specified <code>none</code>
     *         .
     */

    public STFBotToggleButton toggleButton(int index) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>label</code> with the specified <code>inGroup</code>.
     */
    public STFBotToggleButton toggleButtonWithLabelInGroup(String label,
        String inGroup) throws RemoteException;

    /**
     * @param label
     *            the label on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>label</code> with the specified <code>inGroup</code>.
     */

    public STFBotToggleButton toggleButtonWithLabelInGroup(String label,
        String inGroup, int index) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */
    public STFBotToggleButton toggleButtonInGroup(String mnemonicText,
        String inGroup) throws RemoteException;

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>mnemonicText</code> with the specified <code>inGroup</code>
     *         .
     */

    public STFBotToggleButton toggleButtonInGroup(String mnemonicText,
        String inGroup, int index) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>tooltip</code> with the specified <code>inGroup</code>.
     */
    public STFBotToggleButton toggleButtonWithTooltipInGroup(String tooltip,
        String inGroup) throws RemoteException;

    /**
     * @param tooltip
     *            the tooltip on the widget.
     * @param inGroup
     *            the inGroup on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link STFBotToggleButton} with the specified
     *         <code>tooltip</code> with the specified <code>inGroup</code>.
     */

    public STFBotToggleButton toggleButtonWithTooltipInGroup(String tooltip,
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
