package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.SWTBot;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotCCombo;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotCombo;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotLabel;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotStyledText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponent;

public interface STFBot extends EclipseComponent {

    /**********************************************
     * 
     * Widget tree
     * 
     **********************************************/
    /**
     * @see SWTBot#tree()
     */
    public STFBotTree tree() throws RemoteException;

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
    public void waitsUntilIsShellClosed(final String title)
        throws RemoteException;

    /**
     * waits until the given Shell is open.
     * 
     * @param title
     *            the title of the shell.
     * @throws RemoteException
     *             ;;
     */
    public void waitUntilShellOpen(final String title) throws RemoteException;

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

    public boolean existsLabel(String title) throws RemoteException;

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

}
