package de.fu_berlin.inf.dpp.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

//TODO Changing Preferences during shared project session without restarting the session
/**
 * Contains the Communication preferences - consisting of preferences for Chat
 * (todo: and VoIP)
 * 
 */
@Component(module = "prefs")
public class CommunicationPreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

    @Inject
    protected Saros saros;

    protected StringFieldEditor chatserverField;

    public CommunicationPreferencePage() {
        super(FieldEditorPreferencePage.GRID);

        Saros.reinject(this);

        setPreferenceStore(saros.getPreferenceStore());
        setDescription("Settings for Chat and VoIP Functionality. Every Change needs a restart of the shared session!!");
    }

    @Override
    protected void createFieldEditors() {

        chatserverField = new StringFieldEditor(PreferenceConstants.CHATSERVER,
            "Chatserver (Example: conference.jabber.ccc.de)",
            getFieldEditorParent());

        addField(chatserverField);

        BooleanFieldEditor userDefinedChatroom = new BooleanFieldEditor(
            PreferenceConstants.USER_DEFINED_CHATROOM,
            "Chatroom (Default: auto generated)", getFieldEditorParent());

        userDefinedChatroom.fillIntoGrid(getFieldEditorParent(), 2);

        final StringFieldEditor chatroom = new StringFieldEditor(
            PreferenceConstants.CHATROOM, "Chatroom:", getFieldEditorParent());

        chatroom.setEnabled(false, getFieldEditorParent());

        userDefinedChatroom
            .setPropertyChangeListener(new IPropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent event) {
                    if (event.getNewValue().equals(true)) {
                        chatroom.setEnabled(true, getFieldEditorParent());
                    } else {
                        chatroom.setEnabled(false, getFieldEditorParent());
                    }

                }

            });

        BooleanFieldEditor userDefinedChatroomPassword = new BooleanFieldEditor(
            PreferenceConstants.USER_DEFINED_CHATROOM_PASSWORD,
            "Chatroom password (Default: auto generated)",
            getFieldEditorParent());

        userDefinedChatroomPassword.fillIntoGrid(getFieldEditorParent(), 2);

        final StringFieldEditor chatroomPassword = new StringFieldEditor(
            PreferenceConstants.CHATROOM_PASSWORD, "Chatroom Password:",
            getFieldEditorParent());

        chatroomPassword.setEnabled(false, getFieldEditorParent());

        userDefinedChatroomPassword
            .setPropertyChangeListener(new IPropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent event) {
                    if (event.getNewValue().equals(true)) {
                        chatroomPassword.setEnabled(true,
                            getFieldEditorParent());
                    } else {
                        chatroomPassword.setEnabled(false,
                            getFieldEditorParent());
                    }

                }

            });

    }

    public void init(IWorkbench arg0) {
        // no init necessary

    }

}
